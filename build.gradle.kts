import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.IR
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.LEGACY


val mdCompiler = System.getProperties().getProperty("mdCompiler").orEmpty() == "LEGACY"
val jsCompiler = if (mdCompiler) LEGACY else IR

plugins {
    kotlin("multiplatform") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
}

group = "pro.jako"
version = "1.0-SNAPSHOT"

val ktor_version = "1.6.0"
repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useTestNG()
        }
    }
    js(jsCompiler) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
                implementation("io.ktor:ktor-websockets:$ktor_version")
                implementation("org.slf4j:slf4j-simple:1.7.30")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-testng"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.2")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}


tasks.register<Jar>("buildFatJar3") {
    val main = kotlin.jvm().compilations.getByName("main")
    group = "application"
    dependsOn(tasks.assemble) //assemble build
    manifest {
        attributes["Main-Class"] = "JvmMainKt"
    }
    doFirst {
        from(
            configurations.getByName("runtimeClasspath")
                .map { if (it.isDirectory) it else zipTree(it) }, main.output.classesDirs
        )

    }
    archiveBaseName.set("${project.name}-fat3")
}

tasks.register("ctDebug") {
    group = "customTasks"
    doLast {
        println("-".repeat(50))
        val c = configurations
        c.toList().filter {
            it.isCanBeResolved && it.name.startsWith("jvm")
                    && !it.name.startsWith("jvmTest")
                    && it.count() > 0
        }.forEach {
            val len = it.count()
            println("=".repeat(50))
            println("${it.name} -- $len")
            it.forEach {
                println("   ${it.name}")
            }

            println(" done")
            println("")
            println("")
        }


    }
}

//tasks {
//    register<Jar>("buildFatJar2") {
//        group = "application"
//        dependsOn(build)
//        manifest {
//            attributes["Main-Class"] = "com.app.BackendAppKt"
//        }
//        val main = getByName("main")
//        from(configurations.getByName("runtimeClasspath")
//            .map { if (it.isDirectory) it else zipTree(it) }, main.output.classesDirs)
//        archiveBaseName.set("${project.name}-fat2")
//    }
//}
