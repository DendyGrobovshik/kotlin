/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.IntegrateEmbedAndSignIntoXcodeProject
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.IntegrateLinkagePackageIntoXcodeProject
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.swiftimport.XCODEPROJ_PATH_ENV
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.kotlin
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IntegrateLinkagePackageMissingEnvTests {

    @Test
    fun `integrateLinkagePackage - action throws actionable error when XCODEPROJ_PATH is not set`() {
        val project = buildProjectWithMPP {
            kotlin { iosArm64() }
        }.evaluate()

        val task = project.tasks.getByName(IntegrateLinkagePackageIntoXcodeProject.TASK_NAME)
                as IntegrateLinkagePackageIntoXcodeProject

        val failure = assertFailsWith<IllegalStateException> { task.integrate() }

        assertTrue(
            failure.message!!.contains(XCODEPROJ_PATH_ENV),
            "Error must mention $XCODEPROJ_PATH_ENV, was: ${failure.message}"
        )
        assertTrue(
            failure.message!!.contains("Xcode project"),
            "Error must explain that the user has to point the task at an Xcode project, was: ${failure.message}"
        )
        assertTrue(
            failure.message!!.contains("./gradlew ${task.path}"),
            "Error must include copy-pasteable command with task path '${task.path}', was: ${failure.message}"
        )
    }

    @Test
    fun `integrateEmbedAndSign - action throws actionable error when XCODEPROJ_PATH is not set`() {
        val project = buildProjectWithMPP {
            kotlin { iosArm64() }
        }.evaluate()

        val task = project.tasks.getByName(IntegrateEmbedAndSignIntoXcodeProject.TASK_NAME)
                as IntegrateEmbedAndSignIntoXcodeProject

        val failure = assertFailsWith<IllegalStateException> { task.integrate() }

        assertTrue(
            failure.message!!.contains(XCODEPROJ_PATH_ENV),
            "Error must mention $XCODEPROJ_PATH_ENV, was: ${failure.message}"
        )
        assertTrue(
            failure.message!!.contains("./gradlew ${task.path}"),
            "Error must include copy-pasteable command with task path '${task.path}', was: ${failure.message}"
        )
    }
}
