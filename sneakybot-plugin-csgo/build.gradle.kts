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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(project(":sneakybot-sdk"))
    compileOnly("org.slf4j:slf4j-simple:1.7.2")
    compileOnly("com.github.theholywaffle:teamspeak3-api:1.2.0")
    testRuntimeOnly(project(":sneakybot"))
    testCompileOnly(project(":sneakybot-sdk"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.1")
    testImplementation("io.mockk:mockk:1.9.3")
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

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    from(configurations.runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

val copyPluginJar = task("copyPluginJar", type = Copy::class) {
    from(tasks["fatJar"].outputs)
    into("../plugins")
}

val deletePluginJar = task("deletePluginJar", type = Delete::class) {
    delete(fileTree("../plugins").matching {
        include("${project.name}-*.jar")
    })
}

tasks {
    "build" {
        dependsOn(fatJar)
        dependsOn(copyPluginJar)
    }
    "clean" {
        dependsOn(deletePluginJar)
    }
}
