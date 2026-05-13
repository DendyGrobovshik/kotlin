/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(KtNonPublicApi::class)

package org.jetbrains.kotlin.psi.addRemoveModifier

import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens.*
import org.jetbrains.kotlin.psi.*

@Deprecated(
    "Use KtPsiMutationService.getInstance().setModifierList(this, newModifierList) instead",
    ReplaceWith("KtPsiMutationService.getInstance().setModifierList(this, newModifierList)"),
)
fun KtModifierListOwner.setModifierList(newModifierList: KtModifierList) {
    KtPsiMutationService.getInstance().setModifierList(this, newModifierList)
}

@Deprecated(
    "Use KtPsiMutationService.getInstance().addModifier(owner, modifier) instead",
    ReplaceWith("KtPsiMutationService.getInstance().addModifier(owner, modifier)"),
)
fun addModifier(owner: KtModifierListOwner, modifier: KtModifierKeywordToken) {
    KtPsiMutationService.getInstance().addModifier(owner, modifier)
}

@Deprecated(
    "Use KtPsiMutationService.getInstance().addAnnotationEntry(owner, annotationEntry) instead",
    ReplaceWith("KtPsiMutationService.getInstance().addAnnotationEntry(owner, annotationEntry)"),
)
fun addAnnotationEntry(owner: KtModifierListOwner, annotationEntry: KtAnnotationEntry): KtAnnotationEntry =
    KtPsiMutationService.getInstance().addAnnotationEntry(owner, annotationEntry)

@Deprecated(
    "Use KtPsiMutationService.getInstance().removeModifier(owner, modifier) instead",
    ReplaceWith("KtPsiMutationService.getInstance().removeModifier(owner, modifier)"),
)
fun removeModifier(owner: KtModifierListOwner, modifier: KtModifierKeywordToken) {
    KtPsiMutationService.getInstance().removeModifier(owner, modifier)
}

fun sortModifiers(modifiers: List<KtModifierKeywordToken>): List<KtModifierKeywordToken> {
    return modifiers.sortedBy {
        val index = MODIFIER_KEYWORDS_ARRAY.indexOf(it)
        if (index == -1) Int.MAX_VALUE else index
    }
}

@Deprecated(
    "Use `KtTokens.MODIFIER_KEYWORDS_ARRAY` directly",
    ReplaceWith("KtTokens.MODIFIER_KEYWORDS_ARRAY", "org.jetbrains.kotlin.lexer.KtTokens"),
)
val MODIFIERS_ORDER: List<KtModifierKeywordToken> get() = MODIFIER_KEYWORDS_ARRAY.asList()
