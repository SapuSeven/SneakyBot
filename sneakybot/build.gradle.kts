import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.sapuseven.sneakybot"
version = "1.0"

plugins {
    kotlin("jvm") version "1.3.41"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":sneakybot-sdk"))
    implementation("org.slf4j:slf4j-simple:1.7.2")
    implementation("com.github.theholywaffle:teamspeak3-api:1.2.0")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Main-Class"] = "com.sapuseven.sneakybot.SneakyBot" + "Kt"
    }
    from(configurations.runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}