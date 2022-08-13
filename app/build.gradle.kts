import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.liewjuntung"
version = "1.0-SNAPSHOT"

tasks {
    withType<KotlinCompile> { kotlinOptions { jvmTarget = "11" } }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("br.com.devsrsouza.compose.icons.jetbrains:font-awesome:1.0.0")
    implementation(project(":core"))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            macOS {
                iconFile.set(file("${project.rootDir}/app/src/main/resources/launcher.webp"))
            }
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "gpgui"
            packageVersion = "1.0.0"
        }
    }
}
