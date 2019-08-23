import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.sapuseven.sneakybot.plugin"
version = "1.0"

plugins {
    kotlin("jvm") version "1.3.41"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(project(":sneakybot-sdk"))
    compileOnly("com.github.theholywaffle:teamspeak3-api:1.2.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

val copyPluginJar = task("copyPluginJar", type = Copy::class) {
    from(tasks["jar"].outputs)
    into("../plugins")
}

val deletePluginJar = task("deletePluginJar", type = Delete::class) {
    delete(fileTree("../plugins").matching {
        include("${project.name}-*.jar")
    })
}

tasks {
    "build" {
        dependsOn(copyPluginJar)
    }
    "clean" {
        dependsOn(deletePluginJar)
    }
}
