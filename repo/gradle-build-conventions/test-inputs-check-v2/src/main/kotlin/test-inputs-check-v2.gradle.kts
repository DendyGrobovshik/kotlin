plugins {
    id("java-flight-recorder")
}

val pluginBuildDir = "test-inputs-check-v2"
val disableInputsCheck = project.providers.gradleProperty("kotlin.test.instrumentation.disable.inputs.check").orNull?.toBoolean() == true

if (!disableInputsCheck) {
    tasks {
        val checkUndeclaredInputs by registering(CheckUndeclaredInputs::class) {
            outputDirectory = layout.buildDirectory.dir("$pluginBuildDir/undeclared-inputs")
        }
        withType<Test>().configureEach {
            configureTestInstrumenter()
            registerForCheckingInputs(checkUndeclaredInputs)
        }
    }
}

fun Test.configureTestInstrumenter() {
    val declaredInputsFile = layout.buildDirectory.file("$pluginBuildDir/declared-inputs/$name.txt")

    doFirst {
        declaredInputsFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(inputs.files.asFileTree.joinToString(separator = "\n"))
        }
    }

    systemProperty("test.instrumenter.inputs.check.enabled", "true")
    addAbsoluteFileProperty(declaredInputsFile, "test.instrumenter.declared.inputs.file")
    addAbsoluteDirectoryProperty(layout.settingsDirectory, "test.instrumenter.root.dir")
    addAbsoluteDirectoryProperty(layout.buildDirectory, "test.instrumenter.build.dir")
}

fun Test.registerForCheckingInputs(checkUndeclaredInputs: TaskProvider<CheckUndeclaredInputs>) {
    val testTask = this

    // We eagerly call Provider.get() because we're in a lazy context, so we can't use TaskProvider.configure().
    // It's not a big deal since there is always a single CheckUndeclaredInputs task that must be configured anyway,
    // so configuration avoidance would save us nothing
    checkUndeclaredInputs.get().apply {
        jfrFilesToCheck.from(testTask.javaFlightRecorder.jfrFile)
        testTask.finalizedBy(this)
    }
}
