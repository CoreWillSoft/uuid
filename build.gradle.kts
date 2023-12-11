import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform") version "1.9.20"
    id("org.jetbrains.dokka") version "1.8.20"
//    id("maven-publish")
//    id("signing")
}

repositories {
    mavenCentral()
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            samples.from("src/commonTest/kotlin")
        }
    }
}

kotlin {
    targets {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                    apiVersion = "1.7"
                    languageVersion = "1.9"
                }
            }
        }
        if (HostManager.hostIsMac) {
            iosX64()
            iosArm64()
            iosSimulatorArm64()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val nonJvmMain by creating { dependsOn(commonMain) }
        val nonJvmTest by creating { dependsOn(commonTest) }
        val nativeMain by creating { dependsOn(nonJvmMain) }
        val nativeTest by creating { dependsOn(nonJvmTest) }
        val nix64Main by creating { dependsOn(nativeMain) }
        val nix64Test by creating { dependsOn(nativeTest) }

        if (HostManager.hostIsMac) {
            val appleMain by creating { dependsOn(nativeMain) }
            val appleTest by creating { dependsOn(nativeTest) }
            val apple64Main by creating {
                dependsOn(appleMain)
                dependsOn(nix64Main)
            }
            val apple64Test by creating {
                dependsOn(appleTest)
                dependsOn(nix64Test)
            }
            val iosX64Main by getting { dependsOn(apple64Main) }
            val iosX64Test by getting { dependsOn(apple64Test) }
            val iosArm64Main by getting { dependsOn(apple64Main) }
            val iosArm64Test by getting { dependsOn(apple64Test) }
            val iosSimulatorArm64Main by getting { dependsOn(apple64Main) }
            val iosSimulatorArm64Test by getting { dependsOn(apple64Test) }
        }
    }
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}

val ktlintConfig by configurations.creating

dependencies {
    ktlintConfig("com.pinterest:ktlint:0.50.0")
}

val ktlint by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlintConfig
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("src/**/*.kt")
}

val ktlintformat by tasks.registering(JavaExec::class) {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = ktlintConfig
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("-F", "src/**/*.kt", "*.kts")
}

val checkTask = tasks.named("check")
checkTask.configure {
    dependsOn(ktlint)
}

//apply(from = "publish.gradle")

// Generate PROJECT_DIR_ROOT for referencing local mocks in tests

val projectDirGenRoot = "$buildDir/generated/projectdir/kotlin"
val generateProjDirValTask = tasks.register("generateProjectDirectoryVal") {
    doLast {
        mkdir(projectDirGenRoot)
        val projDirFile = File("$projectDirGenRoot/projdir.kt")
        projDirFile.writeText("")
        projDirFile.appendText(
            """
            |package com.benasher44.uuid
            |
            |import kotlin.native.concurrent.SharedImmutable
            |
            |@SharedImmutable
            |internal const val PROJECT_DIR_ROOT = ""${'"'}${projectDir.absolutePath}""${'"'}
            |
            """.trimMargin(),
        )
    }
}

kotlin.sourceSets.named("commonTest") {
    this.kotlin.srcDir(projectDirGenRoot)
}

// Ensure this runs before any test compile task
tasks.withType<AbstractCompile>().configureEach {
    if (name.lowercase().contains("test")) {
        dependsOn(generateProjDirValTask)
    }
}

tasks.withType<AbstractKotlinCompileTool<*>>().configureEach {
    if (name.lowercase().contains("test")) {
        dependsOn(generateProjDirValTask)
    }
}
task("testClasses").doLast {
    println("This is a dummy testClasses task")
}
