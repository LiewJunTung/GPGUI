import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.liewjuntung"
version = "1.0-SNAPSHOT"


tasks {
    withType<KotlinCompile> { kotlinOptions { jvmTarget = "11" } }
}

dependencies {
    implementation("com.lordcodes.turtle:turtle:0.7.0")
}