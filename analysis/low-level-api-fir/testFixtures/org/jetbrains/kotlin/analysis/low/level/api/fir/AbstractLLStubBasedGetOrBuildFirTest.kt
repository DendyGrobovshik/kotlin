/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLResolutionFacade
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFir
import org.jetbrains.kotlin.analysis.low.level.api.fir.services.firRenderingOptions
import org.jetbrains.kotlin.analysis.low.level.api.fir.test.configurators.LLSourceLikeTestConfigurator
import org.jetbrains.kotlin.analysis.test.framework.projectStructure.KtTestModule
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclarationContainer
import org.jetbrains.kotlin.psi.KtDestructuringDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractLLStubBasedGetOrBuildFirTest : AbstractLLStubBasedTest<String>() {
    context(facade: LLResolutionFacade)
    override fun doStubBasedTest(
        stubBasedFile: KtFile,
        mainModule: KtTestModule,
        testServices: TestServices,
    ): String {
        return renderDestructuringDeclarations(stubBasedFile, testServices).also { actual ->
            testServices.assertions.assertEqualsToTestOutputFile(actual)
        }
    }

    context(facade: LLResolutionFacade)
    override fun doAstBasedValidation(
        stubBasedOutput: String,
        astBasedFile: KtFile,
        mainModule: KtTestModule,
        testServices: TestServices,
    ) {
        val astBasedOutput = renderDestructuringDeclarations(astBasedFile, testServices)
        testServices.assertions.assertEquals(stubBasedOutput, astBasedOutput) {
            "AST-based and stub-based outputs are different"
        }
    }

    context(facade: LLResolutionFacade)
    private fun renderDestructuringDeclarations(
        file: KtFile,
        testServices: TestServices,
    ): String {
        val declarations = file.collectDestructuringDeclarations()
        testServices.assertions.assertTrue(declarations.isNotEmpty()) {
            "Expected at least one destructuring declaration"
        }

        return declarations.withIndex().joinToString(separator = "\n\n=====\n\n") { (index, declaration) ->
            val fir = declaration.getOrBuildFir(facade)
            buildString {
                appendLine("Analysis attempt #$index")
                append(renderActualFir(fir, declaration, testServices.firRenderingOptions).trimEnd())
            }
        }
    }

    private fun KtDeclarationContainer.collectDestructuringDeclarations(): List<KtDestructuringDeclaration> = buildList {
        for (declaration in declarations) {
            when (declaration) {
                is KtDestructuringDeclaration -> add(declaration)
                is KtClassOrObject -> addAll(declaration.collectDestructuringDeclarations())
            }
        }
    }
}

abstract class AbstractSourceLikeStubBasedGetOrBuildFirTest : AbstractLLStubBasedGetOrBuildFirTest() {
    override val configurator = LLSourceLikeTestConfigurator()
}
