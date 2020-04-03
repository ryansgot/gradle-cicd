import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.3.71"
    id("com.jfrog.bintray")
    id("maven-publish")
    id("fsryan-gradle-publishing")
}

group = "com.fsryan.gradle.cicd"
version = "0.0.1"
val artifactId = "core"

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

fun Project.propOrEnvVar(prop: String, envVar: String): String {
    return checkNotNull(findProperty(prop) ?: System.getenv()[envVar]) {
        "Required either gradle property '$prop' or environment variable: '$envVar'"
    }.toString()
}

fsPublishingConfig {
    developerName = "Ryan Scott"
    developerId = "fsryan"
    developerEmail = "fsryan.developer@gmail.com"
    siteUrl = "https://github.com/ryansgot/gradle-cicd"
    baseArtifactId = artifactId
    groupId = project.group.toString()
    versionName = project.version.toString()
    releaseRepoUrl = "s3://repo.fsryan.com/release"
    snapshotRepoUrl = "s3://repo.fsryan.com/snapshot"
    description = "Core library for all concrete GradleCICD implementations"
    awsAccessKeyId = project.propOrEnvVar(prop = "awsMavenAccessKey", envVar = "AWS_ACCES_KEY_ID")
    awsSecretKey = project.propOrEnvVar(prop = "awsMavenSecretKey", envVar = "AWS_SECRET_KEY")
    additionalPublications.add("bintray")
}

bintray {
    user = (project.findProperty("bintrayUser") ?: "").toString()
    key = (project.findProperty("bintrayApiKey") ?: "").toString()
    setPublications("mavenToBintray")
    publish = false

    pkg.apply {
        repo = "maven"
        name = artifactId
        desc = "Core library for all concrete GradleCICD implementations"
        websiteUrl = "https://github.com/ryansgot/gradle-cicd"
        issueTrackerUrl = "https://github.com/ryansgot/gradle-cicd/issues"
        vcsUrl = "https://github.com/ryansgot/gradle-cicd.git"
        publicDownloadNumbers = true
        setLicenses("Apache-2.0")
        setLabels("gradle", "plugin", "continuous integration", "continuous deployment")
        version.apply {
            name = project.version.toString()
            released = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").format(Date())
            vcsTag = "v${project.version}"
        }
    }
}

project.afterEvaluate {
    checkNotNull(project.tasks.findByName("release"))
            .dependsOn(checkNotNull(project.tasks.findByName("bintrayUpload")))
}