buildscript {
    fun Project.propOrEnvVar(prop: String, envVar: String): String {
        return checkNotNull(findProperty(prop) ?: System.getenv()[envVar]) {
            "Required either gradle property '$prop' or environment variable: '$envVar'"
        }.toString()
    }
    repositories {
        jcenter()
        maven {
            url = uri("s3://repo.fsryan.com/release")
            credentials(AwsCredentials::class) {
                @Suppress("UsePropertyAccessSyntax")
                setAccessKey(project.propOrEnvVar("awsMavenAccessKey", "AWS_ACCES_KEY_ID"))
                @Suppress("UsePropertyAccessSyntax")
                setSecretKey(project.propOrEnvVar("awsMavenSecretKey", "AWS_SECRET_KEY"))
            }
        }
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:${project.property("version.bintray.publishing")}")
        classpath("com.fsryan.gradle:fsryan-gradle-publishing:${project.property("version.fsryan.publishing")}")
    }
}

afterEvaluate {
    fun Project.propOrEnvVar(prop: String, envVar: String): String {
        return checkNotNull(findProperty(prop) ?: System.getenv()[envVar]) {
            "Required either gradle property '$prop' or environment variable: '$envVar'"
        }.toString()
    }
    allprojects {
        repositories {
            jcenter()
            maven {
                url = uri("s3://repo.fsryan.com/release")
                credentials(AwsCredentials::class) {
                    @Suppress("UsePropertyAccessSyntax")
                    setAccessKey(project.propOrEnvVar("awsMavenAccessKey", "AWS_ACCES_KEY_ID"))
                    @Suppress("UsePropertyAccessSyntax")
                    setSecretKey(project.propOrEnvVar("awsMavenSecretKey", "AWS_SECRET_KEY"))
                }
            }
        }
    }
}

tasks.register("clean").configure {
    delete("build", "buildSrc/build")
}