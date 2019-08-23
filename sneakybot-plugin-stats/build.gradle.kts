plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.3.41'
}

group 'com.sapuseven.sneakybot.plugin'
version '1.0'

sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    compileOnly "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    compileOnly "com.github.theholywaffle:teamspeak3-api:1.2.0"
    compileOnly project(":sneakybot-sdk")
}

compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

build.doLast {
    copy {
        from jar
        into '../plugins'
    }
}
