// RUN_PIPELINE_TILL: FRONTEND
// OPT_IN: kotlin.js.ExperimentalJsExport

@file:JsExport

interface ExportedInterfaceWithNestedClasses {
    companion object {
        fun foo() = 42
    }

    fun createNested(): Nested = Nested()

    class Nested {
        class NestedInNested

        fun bar() = foo()
    }

    abstract class AbstractNested

    private class PrivateNested

    class ConstructorWithDefaultsAndVarargs(val prefix: String = "", vararg val parts: String)

    value class NestedValue(val value: Int)
}

fun interface ExportedFunInterfaceWithNestedClass {
    fun run(): String

    class Nested
}
