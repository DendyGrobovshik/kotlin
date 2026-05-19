plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":compiler:cli"))
    implementation(project(":compiler:frontend.common-psi"))
    implementation(project(":compiler:ir.backend.common"))
    implementation(project(":compiler:ir.tree"))
    compileOnly(intellijCore())
}

optInToExperimentalCompilerApi()
optInToUnsafeDuringIrConstructionAPI()

sourceSets {
    "main" { projectDefault() }
    "test" { none() }
}
