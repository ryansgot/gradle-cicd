package com.fsryan.gradle.cicd.git

import com.fsryan.gradle.cicd.GradleCICDPlugin
import com.fsryan.gradle.cicd.VersionControlSystemApi
import org.gradle.api.Project

class GradleCICDGitPlugin: GradleCICDPlugin() {
    override fun createVcsApi(project: Project): VersionControlSystemApi = GitApi(project)
    override fun log(message: String) {
        println("[GradleCICDGitPlugin] $message")
    }
}