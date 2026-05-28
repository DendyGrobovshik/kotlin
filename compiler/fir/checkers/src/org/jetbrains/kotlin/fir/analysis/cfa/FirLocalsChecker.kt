/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.cfa

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.cfa.FirControlFlowChecker
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.contracts.description.ConeLocalEffectDeclaration
import org.jetbrains.kotlin.fir.contracts.effects
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirContractDescriptionOwner
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.utils.contextParametersForFunctionOrContainingProperty
import org.jetbrains.kotlin.fir.resolve.dfa.Domain
import org.jetbrains.kotlin.fir.resolve.dfa.DomainReference
import org.jetbrains.kotlin.fir.resolve.dfa.RealVariable
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.ControlFlowGraph
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.VariableDeclarationExitNode
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol

object FirLocalsChecker : FirControlFlowChecker(MppCheckerKind.Common) {
    context(reporter: DiagnosticReporter, context: CheckerContext)
    override fun analyze(graph: ControlFlowGraph) {
        check(graph, listOf(Scope.Empty), allowTopMostReturn = false)
    }

    data class Scope(
        val localDomains: Map<Domain, RealVariable>,
        val localVariables: Set<RealVariable>,
    ) {
        companion object {
            val Empty: Scope = Scope(emptyMap(), emptySet())
        }
    }

    context(reporter: DiagnosticReporter, context: CheckerContext)
    fun check(
        graph: ControlFlowGraph,
        scopes: List<Scope>,
        allowTopMostReturn: Boolean,
    ) {
        val function = graph.declaration as? FirFunction

        val parametersWithLocalContract =
            function?.valueParameters.orEmpty().filter { it.hasLocalContract == true }.map { it.symbol}
        val parametersFromLocallyScoped = when {
            function?.isLocallyScoped == true -> function.valueParameters.map { it.symbol }.toSet()
            else -> emptySet()
        }
        val localParameters = (parametersWithLocalContract + parametersFromLocallyScoped)
            .map { it.realVariable() }.associateBy { graph.enterNode.flow.getDomains(it).single() }

        val topScope = scopes.first()
        val localDomains = topScope.localDomains + localParameters
        val localVariables = (topScope.localVariables + localParameters.values).toMutableSet()
        val updatedScopes = listOf(Scope(localDomains, localVariables)) + scopes.drop(1)

        for (node in graph.nodes) {
            if (node is VariableDeclarationExitNode) {
                localVariables.add(node.fir.symbol.realVariable())
            }

            var isTopMost = true
            for ((scopeDomains, scopeVariables) in updatedScopes) {
                for ((parameterDomain, parameterVariable) in scopeDomains) {
                    val parameterReferences = node.flow.getReferences(parameterDomain).filterIsInstance<DomainReference.WithStatement>()
                    for (reference in parameterReferences) {
                        // if (reference !is DomainReference.WithStatement) continue
                        if (node.fir != reference.statement) continue
                        if (reference is DomainReference.WithVariable && reference.variable in scopeVariables) continue
                        if (reference is DomainReference.Result && isTopMost && allowTopMostReturn) continue

                        var statementToReport = reference.statement
                        var referenceToReport: DomainReference.WithStatement = reference
                        while (referenceToReport is DomainReference.Result) {
                            statementToReport = referenceToReport.original ?: break
                            referenceToReport = parameterReferences.find { it.statement == referenceToReport.original } ?: break
                        }

                        if (referenceToReport is DomainReference.Potential) {
                            reporter.reportOn(
                                (referenceToReport.argument ?: statementToReport).source,
                                FirErrors.LEAKED_LOCAL_THROUGH_CALL,
                                parameterVariable.symbol
                            )
                        } else {
                            reporter.reportOn(statementToReport.source, FirErrors.LEAKED_LOCAL, parameterVariable.symbol)
                        }
                    }
                }
                isTopMost = false
            }
        }

        for (subGraph in graph.subGraphs) {
            if (subGraph.declaration?.evaluatedInPlace != true || subGraph.declaration?.isLocallyScoped == true) {
                check(subGraph, listOf(Scope.Empty) + updatedScopes, allowTopMostReturn = false)
            } else {
                check(subGraph, updatedScopes, allowTopMostReturn = true)
            }
        }
    }
}

private fun FirCallableSymbol<*>.realVariable(): RealVariable =
    RealVariable(this, false, null, null, resolvedReturnType)

private val FirDeclaration.isLocallyScoped: Boolean
    get() = this is FirAnonymousFunction && isLocallyScoped == true
