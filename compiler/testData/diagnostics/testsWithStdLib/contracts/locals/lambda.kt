// RUN_PIPELINE_TILL: BACKEND
// RENDER_DIAGNOSTIC_ARGUMENTS
// LANGUAGE: +ImprovedAliasTracking

@file:OptIn(ExperimentalContracts::class)

import kotlin.contracts.*

fun foo(block: (Int) -> Unit) { }

fun fooInPlace(block: (Int) -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    block(1)
}

fun bar(n: Int) {
    contract { local(n) }
    foo {
        <!LEAKED_LOCAL("n: Int")!>val x = n<!>
    }
    fooInPlace {
        val x = n
    }
    foo {
        <!LEAKED_LOCAL("n: Int")!>n<!>
    }
    fooInPlace { n }
}

/* GENERATED_FIR_TAGS: annotationUseSiteTargetFile, classReference, contracts, functionDeclaration, functionalType,
lambdaLiteral, localProperty, propertyDeclaration */
