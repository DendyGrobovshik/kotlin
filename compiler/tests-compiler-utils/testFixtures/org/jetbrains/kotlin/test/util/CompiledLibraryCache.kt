/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class CompiledLibraryCache : Disposable {
    private val cache = ConcurrentHashMap<String, File>()
    private val locks = ConcurrentHashMap<String, Any>()

    init {
        try {
            val disposerClass = Class.forName("org.jetbrains.kotlin.test.ApplicationEnvironmentDisposer")
            val rootDisposableField = disposerClass.getField("ROOT_DISPOSABLE")
            val rootDisposable = rootDisposableField.get(null) as Disposable
            Disposer.register(rootDisposable, this)
        } catch (e: Throwable) {
            // Ignore if ApplicationEnvironmentDisposer is not on the classpath
        }
    }

    fun getOrCompile(key: String, compile: () -> File): File {
        val cached = cache[key]
        if (cached != null) return cached

        val lock = locks.computeIfAbsent(key) { Any() }

        synchronized(lock) {
            val doubleCheck = cache[key]
            if (doubleCheck != null) return doubleCheck

            val compiled = compile()
            cache[key] = compiled
            return compiled
        }
    }

    override fun dispose() {
        cache.clear()
        locks.clear()
    }
}
