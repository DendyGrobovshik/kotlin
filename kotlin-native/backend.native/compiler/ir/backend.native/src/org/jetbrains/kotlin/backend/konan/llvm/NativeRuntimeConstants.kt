/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

object NativeRuntimeConstants {
    const val NEED_DEBUG_INFO: String = "Kotlin_needDebugInfo"
    const val RUNTIME_ASSERTS_MODE: String = "Kotlin_runtimeAssertsMode"
    const val DISABLE_MMAP: String = "Kotlin_disableMmap"
    const val RUNTIME_LOGS_ENABLED: String = "Kotlin_runtimeLogsEnabled"
    const val CONCURRENT_WEAK_SWEEP: String = "Kotlin_concurrentWeakSweep"
    const val GC_MARK_SINGLE_THREADED: String = "Kotlin_gcMarkSingleThreaded"
    const val FIXED_BLOCK_PAGE_SIZE: String = "Kotlin_fixedBlockPageSize"
    const val PAGED_ALLOCATOR: String = "Kotlin_pagedAllocator"
}
