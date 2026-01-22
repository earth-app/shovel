import com.android.build.api.dsl.androidLibrary
import dev.petuska.npm.publish.task.NpmPublishTask
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn

plugins {
    kotlin("multiplatform") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    kotlin("native.cocoapods") version "2.3.0"
    id("org.jetbrains.dokka") version "2.1.0"
    id("com.android.kotlin.multiplatform.library") version "8.13.2"
    id("com.vanniktech.maven.publish") version "0.36.0"
    id("dev.petuska.npm.publish") version "3.5.3"

    `maven-publish`
    jacoco
    signing
}

val v = "1.1.1"

group = "com.earth-app.shovel"
version = "${if (project.hasProperty("snapshot")) "$v-SNAPSHOT" else v}${project.findProperty("suffix")?.toString()?.run { "-${this}" } ?: ""}"
val desc = "Kotlin Multiplatform Web Scraping Framework"
description = desc

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    configureSourceSets()
    applyDefaultHierarchyTemplate()
    withSourcesJar()

    jvm()
    js {
        nodejs {
            testTask {
                useMocha {
                    timeout = "10m"
                }
            }
        }

        yarn.yarnLockAutoReplace = true
        binaries.library()
        generateTypeScriptDefinitions()
    }

    mingwX64()
    macosX64()
    macosArm64()
    linuxX64()

    iosX64()
    iosArm64()
    androidLibrary {
        namespace = "com.earthapp.shovel"
        compileSdk = 36

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()

    val ktorVersion = "3.3.3"
    val ksoupVersion = "0.2.0"

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
            api("io.ktor:ktor-client-core:$ktorVersion")
        }

        commonTest.dependencies {
            api(kotlin("test"))
            api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }

        jvmMain.dependencies {
            api("org.jsoup:jsoup:1.22.1")
            api("io.ktor:ktor-client-java:$ktorVersion")
            api("ch.qos.logback:logback-classic:1.5.25")
        }

        androidMain.dependencies {
            api("org.jsoup:jsoup:1.22.1")
            api("io.ktor:ktor-client-okhttp:$ktorVersion")
        }

        nativeMain.dependencies {
            api("com.fleeksoft.ksoup:ksoup-lite:$ksoupVersion")
        }

        mingwMain.dependencies {
            api("io.ktor:ktor-client-winhttp:$ktorVersion")
        }

        appleMain.dependencies {
            api("io.ktor:ktor-client-darwin:$ktorVersion")
        }

        linuxMain.dependencies {
            api("io.ktor:ktor-client-curl:$ktorVersion")
        }

        jsMain.dependencies {
            api("io.ktor:ktor-client-js:$ktorVersion")
            api("com.fleeksoft.ksoup:ksoup-lite:$ksoupVersion")
        }
    }
    
    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
        target.binaries {
            staticLib(listOf(if (hasProperty("snapshot")) NativeBuildType.DEBUG else NativeBuildType.RELEASE)) { 
                baseName = project.name
                export("com.fleeksoft.ksoup:ksoup-lite:$ksoupVersion")
                export("io.ktor:ktor-client-core:$ktorVersion")

                if ("mingw" in target.name)
                    export("io.ktor:ktor-client-winhttp:$ktorVersion")
                else if ("os" in target.name)
                    export("io.ktor:ktor-client-darwin:${ktorVersion}")
                else
                    export("io.ktor:ktor-client-curl:${ktorVersion}")
            }

            sharedLib {
                baseName = project.name
                export("com.fleeksoft.ksoup:ksoup-lite:$ksoupVersion")
                export("io.ktor:ktor-client-core:$ktorVersion")

                if ("mingw" in target.name)
                    export("io.ktor:ktor-client-winhttp:$ktorVersion")
                else if ("os" in target.name)
                    export("io.ktor:ktor-client-darwin:${ktorVersion}")
                else
                    export("io.ktor:ktor-client-curl:${ktorVersion}")
            }

            if ("os" in target.name) // macos, ios, tvos, watchos
                framework(listOf(if (hasProperty("snapshot")) NativeBuildType.DEBUG else NativeBuildType.RELEASE)) {
                    baseName = project.name

                    export("com.fleeksoft.ksoup:ksoup-lite:$ksoupVersion")
                    export("io.ktor:ktor-client-core:$ktorVersion")
                    export("io.ktor:ktor-client-darwin:$ktorVersion")

                    binaryOption("bundleId", "com.earth-app")
                    binaryOption("bundleVersion", "2")
                }
        }
    }

    cocoapods {
        version = project.version.toString()
        summary = desc
        homepage = "https://github.com/earth-app/shovel"
        name = "shovel"

        framework {
            baseName = "shovel"
            isStatic = false
        }
    }
}

fun KotlinMultiplatformExtension.configureSourceSets() {
    sourceSets
        .matching { it.name !in listOf("main", "test") }
        .all {
            val srcDir = if ("Test" in name) "test" else "main"
            val resourcesPrefix = if (name.endsWith("Test")) "test-" else ""
            val platform = when {
                (name.endsWith("Main") || name.endsWith("Test")) && "android" !in name -> name.dropLast(4)
                else -> name.substringBefore(name.first { it.isUpperCase() })
            }

            kotlin.srcDir("src/$platform/$srcDir")
            resources.srcDir("src/$platform/${resourcesPrefix}resources")

            languageSettings.apply {
                progressiveMode = true
            }
        }
}

tasks {
    clean {
        delete("kotlin-js-store")
    }

    withType<NpmPublishTask> {
        tag = when {
            project.hasProperty("snapshot") -> "next"
            project.hasProperty("suffix") -> "beta"
            else -> "latest"
        }
    }

    register("jvmJacocoTestReport", JacocoReport::class) {
        dependsOn("jvmTest")

        classDirectories.setFrom(layout.buildDirectory.file("classes/kotlin/jvm/"))
        sourceDirectories.setFrom("src/common/main/", "src/jvm/main/")
        executionData.setFrom(layout.buildDirectory.files("jacoco/jvmTest.exec"))

        reports {
            xml.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("jacoco.xml"))

            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }

    if ("windows" in System.getProperty("os.name").lowercase()) {
        named("linkDebugTestLinuxX64") { enabled = false }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null && signingPassword != null)
        useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)
}

publishing {
    publications {
        filterIsInstance<MavenPublication>().forEach {
            it.apply {
                pom {
                    name = "shovel"

                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://opensource.org/licenses/MIT"
                        }
                    }

                    developers {
                        developer {
                            id = "gmitch215"
                            name = "Gregory Mitchell"
                            email = "me@gmitch215.xyz"
                        }
                    }

                    scm {
                        connection = "scm:git:git://github.com/earth-app/shovel.git"
                        developerConnection = "scm:git:ssh://github.com/earth-app/shovel.git"
                        url = "https://github.com/earth-app/shovel"
                    }
                }
            }
        }
    }

    repositories {
        if (!version.toString().endsWith("SNAPSHOT")) {
            maven {
                name = "GithubPackages"
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }

                url = uri("https://maven.pkg.github.com/earth-app/shovel")
            }
        }
    }
}

mavenPublishing {
    coordinates(project.group.toString(), project.name, project.version.toString())

    pom {
        name.set("shovel")
        description.set(desc)
        url.set("https://github.com/earth-app/shovel")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id = "gmitch215"
                name = "Gregory Mitchell"
                email = "me@gmitch215.xyz"
            }
        }

        scm {
            connection = "scm:git:git://github.com/earth-app/shovel.git"
            developerConnection = "scm:git:ssh://github.com/earth-app/shovel.git"
            url = "https://github.com/earth-app/shovel"
        }
    }

    publishToMavenCentral(true)
    signAllPublications()
}

npmPublish {
    readme = file("README.md")

    packages.forEach {
        it.packageJson {
            name = "@earth-app/${project.name}"
            version = project.version.toString()
            description = desc
            license = "MIT"
            homepage = "https://github.com/earth-app/shovel"

            types = "${project.name}.d.ts"

            author {
                name = "Gregory Mitchell"
                email = "me@gmitch215.xyz"
            }

            repository {
                type = "git"
                url = "git+https://github.com/earth-app/shovel.git"
            }

            keywords = listOf("earth-app", "web", "scraping", "kotlin", "multiplatform")
        }
    }

    registries {
        register("npmjs") {
            uri.set("https://registry.npmjs.org")
            authToken.set(System.getenv("NPM_TOKEN"))
        }

        register("GithubPackages") {
            uri.set("https://npm.pkg.github.com/earth-app")
            authToken.set(System.getenv("GITHUB_TOKEN"))
        }
    }
}
