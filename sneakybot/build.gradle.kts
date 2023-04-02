import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.sapuseven.sneakybot"
version = "1.0"

plugins {
	kotlin("jvm") version "1.6.10"
	application
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
	implementation("org.ini4j:ini4j:0.5.4")
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions.jvmTarget = "1.8"
}

application {
	mainClass.set("com.sapuseven.sneakybot.SneakyBot" + "Kt")
}
