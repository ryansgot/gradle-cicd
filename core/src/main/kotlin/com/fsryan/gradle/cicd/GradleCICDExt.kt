package com.fsryan.gradle.cicd

class GradleCICDExt {

    lateinit var developBranchName: String
    lateinit var releaseBranchName: String
    lateinit var devLaunchBranch: String
    lateinit var developBranchTaskDescription: String
    lateinit var releaseBranchTaskDescription: String
    val developBranchTaskDependencyPaths = mutableSetOf<String>()
    val releaseBranchTaskDependencyPaths = mutableSetOf<String>()

    companion object {
        const val NAME = "gradleCICD"
    }
}