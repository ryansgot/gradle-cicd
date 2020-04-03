package com.fsryan.gradle.cicd

import org.gradle.api.tasks.Internal

open class GradleCICDExt {
    var developBranchName: String = "develop"
    var releaseBranchName: String = "release"
    var devLaunchBranch: String = "master"
    var developBranchTaskDescription: String = "$developBranchName branch CI/CD tasks"
    var releaseBranchTaskDescription: String = "$releaseBranchName branch CI/CD tasks"
    var developBranchTaskDependencyPaths: Set<String> = mutableSetOf()
    var releaseBranchTaskDependencyPaths: Set<String> = mutableSetOf()

    companion object {
        const val NAME = "gradleCICD"
    }
}