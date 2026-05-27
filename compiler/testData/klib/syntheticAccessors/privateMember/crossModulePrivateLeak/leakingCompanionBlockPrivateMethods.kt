// LANGUAGE: +CompanionBlocksAndExtensions
// IGNORE_KLIB_SYNTHETIC_ACCESSORS_CHECKS: ANY
// IGNORE_BACKEND: ANY
// Should be unmuted when KT-86577 is fixed
// MODULE: lib
// FILE: A.kt
class A {
    companion {
        private fun o() = "O"
        private fun k() = "K"

        internal inline fun internalInlineStaticMethod() = o()
    }

    internal inline fun internalInlineMethod() = k()
}

// MODULE: main()(lib)
// FILE: main.kt
fun box(): String {
    return A.internalInlineStaticMethod() + A().internalInlineMethod()
}
