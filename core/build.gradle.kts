plugins {
    groovy
    kotlin("jvm") version "1.3.71"
}

group = "com.fsryan.gradle.cicd"
version = "0.0.1"

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}