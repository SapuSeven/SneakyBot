import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.sapuseven.sneakybot"
version = "1.0"

plugins {
    kotlin("jvm") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("com.github.theholywaffle:teamspeak3-api:1.2.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}
