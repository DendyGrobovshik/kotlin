// RUN_PIPELINE_TILL: BACKEND
// RENDER_DIAGNOSTIC_ARGUMENTS
// LANGUAGE: +ImprovedAliasTracking

@file:OptIn(ExperimentalContracts::class)

import kotlin.contracts.*

inline fun <E, R> foldBad(iterator: Iterator<E>, init: R, folder: (R, E) -> R): R {
    contract {
        local(iterator)
        local(init)
    }
    var result = init
    for (element in iterator)
        result = folder(result, element)
    <!LEAKED_LOCAL("init: R")!>return result<!>
}

inline fun <E, R> foldOk(iterator: Iterator<E>, init: R, folder: (R, E) -> R): R {
    contract {
        local(iterator)
    }
    var result = init
    for (element in iterator)
        result = folder(result, element)
    return result
}

fun sum(iterator: Iterator<Int?>): Int? {
    contract {
        local(iterator)
    }
    return foldOk(iterator, 0) { result, element -> result + (element ?: return null) }
}

/* GENERATED_FIR_TAGS: assignment, forLoop, funWithExtensionReceiver, functionDeclaration, functionalType, lambdaLiteral,
localProperty, nullableType, propertyDeclaration, thisExpression, typeParameter */
