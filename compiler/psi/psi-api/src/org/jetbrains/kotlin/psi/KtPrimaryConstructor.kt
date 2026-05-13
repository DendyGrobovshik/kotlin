/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(KtNonPublicApi::class)

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.KtStubBasedElementTypes
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.stubs.KotlinConstructorStub

/**
 * Represents a primary constructor explicitly declared in a class header.
 *
 * ### Example:
 *
 * ```kotlin
 * class Person constructor(val name: String)
 * //           ^___________________________^
 * ```
 */
class KtPrimaryConstructor : KtConstructor<KtPrimaryConstructor> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: KotlinConstructorStub<KtPrimaryConstructor>) : super(stub, KtStubBasedElementTypes.PRIMARY_CONSTRUCTOR)

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D) = visitor.visitPrimaryConstructor(this, data)

    override fun getContainingClassOrObject() = parent as KtClassOrObject

    @Deprecated(
        "Use KtPsiMutationService.getInstance().removeRedundantConstructorKeywordAndSpace(this) instead",
        ReplaceWith("KtPsiMutationService.getInstance().removeRedundantConstructorKeywordAndSpace(this)"),
    )
    fun removeRedundantConstructorKeywordAndSpace() {
        KtPsiMutationService.getInstance().removeRedundantConstructorKeywordAndSpace(this)
    }

    @Deprecated(
        "Use KtPsiMutationService.getInstance().addConstructorModifier(this, modifier) instead",
        ReplaceWith("KtPsiMutationService.getInstance().addConstructorModifier(this, modifier)"),
    )
    override fun addModifier(modifier: KtModifierKeywordToken) {
        KtPsiMutationService.getInstance().addConstructorModifier(this, modifier)
    }

    @Deprecated(
        "Use KtPsiMutationService.getInstance().removeConstructorModifier(this, modifier) instead",
        ReplaceWith("KtPsiMutationService.getInstance().removeConstructorModifier(this, modifier)"),
    )
    override fun removeModifier(modifier: KtModifierKeywordToken) {
        KtPsiMutationService.getInstance().removeConstructorModifier(this, modifier)
    }

    @Deprecated(
        "Use KtPsiMutationService.getInstance().addConstructorAnnotationEntry(this, annotationEntry) instead",
        ReplaceWith("KtPsiMutationService.getInstance().addConstructorAnnotationEntry(this, annotationEntry)"),
    )
    override fun addAnnotationEntry(annotationEntry: KtAnnotationEntry): KtAnnotationEntry =
        KtPsiMutationService.getInstance().addConstructorAnnotationEntry(this, annotationEntry)

    override fun mayHaveContract(): Boolean = false
}
