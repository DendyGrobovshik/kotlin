/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

import jdk.jfr.consumer.RecordingFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.min

@CacheableTask
abstract class CheckUndeclaredInputsTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val jfrFilesToCheck: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    val verificationTasksDisabled: Property<Boolean> = project.objects.property<Boolean>()
        .value(project.kotlinBuildProperties.verificationTasksDisabled)
        .apply { finalizeValue() }

    @TaskAction
    fun execute() {
        if (verificationTasksDisabled.get()) {
            println("Skipping undeclared inputs checking because `kotlin.build.disable.verification.tasks` is true")
            return
        }

        val undeclaredInputs = mutableMapOf<String, Set<Path>>()

        for (jfrFile in jfrFilesToCheck.files) {
            val taskName = jfrFile.nameWithoutExtension

            undeclaredInputs[taskName] = buildSet {
                RecordingFile(jfrFile.toPath()).use { recording ->
                    while (recording.hasMoreEvents()) {
                        val event = recording.readEvent()
                        if (event.eventType.name !in listOf("jetbrains.UndeclaredInput")) continue
                        val path = event.getString("path")?.let(Paths::get) ?: continue
                        add(path)
                    }
                }
            }
            outputDirectory.file("$taskName.txt").get().asFile
                .writeText(undeclaredInputs[taskName]?.joinToString("\n").orEmpty())
        }

        val allUndeclaredInputs = undeclaredInputs.values.flatten().distinct()

        if (allUndeclaredInputs.isNotEmpty()) {
            error(buildString {
                appendLine("Undeclared inputs found! (${allUndeclaredInputs.size})")
                appendLine("Open the JFR snapshot in IDEA, then go to: Events | Uncategorized | jetbrains.UndeclaredInput")
                appendLine("You can find it here -> ${jfrFilesToCheck.first().parentFile.absolutePath}")
                appendLine("Displaying ${min(allUndeclaredInputs.size, 100)}/${allUndeclaredInputs.size} undeclared inputs:")
                if (allUndeclaredInputs.size > 100) {
                    appendLine("See the full list here -> ${outputDirectory.get().asFile.absolutePath}")
                }
                allUndeclaredInputs.take(100).forEach { appendLine(it) }
            })
        }
    }
}
