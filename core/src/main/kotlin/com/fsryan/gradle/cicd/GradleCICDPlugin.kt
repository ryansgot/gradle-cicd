package com.fsryan.gradle.cicd

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

abstract class GradleCICDPlugin : Plugin<Project> {

    private lateinit var ext: GradleCICDExt

    private lateinit var vcsApi: VersionControlSystemApi
    lateinit var branch: String
    private lateinit var semanticVersion: SemanticVersion

    val versionName: String
        get() = when {
            // TODO: make the increment configurable via extension
            branch == ext.releaseBranchName -> when (semanticVersion.code() % 2) {
                0 -> semanticVersion.name()
                else -> semanticVersion.nextPatch().name()
            }
            else -> semanticVersion.name()
        }

    val versionCode: Int
        get() = when {
            // TODO: make the increment configurable via extension
            branch == ext.releaseBranchName -> when (semanticVersion.code() % 2) {
                0 -> semanticVersion.code()
                else -> semanticVersion.code() + 1
            }
            else -> when (semanticVersion.code() % 2) {
                0 -> throw IllegalStateException("Expected odd version code for this build but was: ${semanticVersion.code()}")
                else -> semanticVersion.code()
            }
        }

    val releaseNotes: String by lazy { vcsApi.commitsContainingMessageText(inclusionFilter = "Related work items", untilMatches = "bump version to") }

    abstract fun createVcsApi(project: Project): VersionControlSystemApi

    override fun apply(project: Project) {
        ext = project.extensions.create(GradleCICDExt.NAME, GradleCICDExt::class.java)
        vcsApi = createVcsApi(project)

        branch = (project.findProperty("cicd.branch.override") ?: vcsApi.abbreviatedRevParse()).toString()
        log("current branch is: $branch")

        val lastVersion = findLastVersion()
        semanticVersion = SemanticVersion.parse(lastVersion)
        log("last version string: '$lastVersion' -> semantic version: '$semanticVersion'")

        val description = createCICDTaskDescription()
        log("CI/CD task will: $description")
        val cicdTask: Task = project.createPerformCICDTask(description)

        if (branch == ext.developBranchName) {
            project.tasks.whenTaskAdded {
                // TODO: make this less specific to App Center by means of an extension
                if (ext.developBranchTaskDependencyPaths.contains(it.path)) {
                    cicdTask.dependsOn(it.path)
                    log("set CI/CD task dependent upon ${it.path}")
                }
            }
        }

        if (branch == ext.releaseBranchName) {
            project.tasks.whenTaskAdded {
                if (ext.releaseBranchTaskDependencyPaths.contains(it.path)) {
                    cicdTask.dependsOn(it.path)
                    log("set CI/CD task dependent upon ${it.path}")
                }
            }
        }

        project.createPrintReleaseNotestTask()
    }

    private fun createCICDTaskDescription() = when (branch) {
        ext.developBranchName -> ext.developBranchTaskDescription
        ext.releaseBranchName -> ext.releaseBranchTaskDescription
        ext.devLaunchBranch -> "Create next dev/release branches for version: ${semanticVersion.nextMinor(newPatch = 1)}"
        else -> "Do nothing because the branch has no special behavior triggers: $branch"
    }

    private fun Project.createPerformCICDTask(taskDescription: String) = tasks.create("performCICD") {
        it.description = taskDescription
        it.group = "CI/CD"
        it.doLast {
            when (branch) {
                ext.developBranchName -> bumpPatchVersion()
                ext.releaseBranchName -> createReleaseTag()
                ext.devLaunchBranch -> bumpMinorVersion()
                else -> log("There is no configuration for CI/CD from branch: $branch")
            }

            log("Perform CI/CD task completed")
        }
    }

    private fun Project.createPrintReleaseNotestTask() = tasks.create("printReleaseNotes") {
        it.description = "Print the release notes since the last version bump to the console"
        it.group = "CI/CD"
        it.doLast {
            println("Release Notes:")
            println(releaseNotes)
        }
    }

    private fun bumpPatchVersion() {
        // TODO: make the increment configurable via extension
        if (semanticVersion.code() % 2 == 0) {
            throw IllegalStateException("Not expecting version code: ${semanticVersion.code()}")
        }

        val nextVersion = semanticVersion.nextPatch(increment = 2)
        val tmpBranchName = "tmp-${nextVersion.name()}"
        log("creating temporary branch: $tmpBranchName")
        vcsApi.createLocalBranch(tmpBranchName).also { log(it) }
        log("Ensuring $branch is checked out and up-to-date: first deleting local branch: $branch")
        vcsApi.deleteLocalBranch(branch).also { log(it) }
        log("Ensuring $branch is checked out and up-to-date: next checking out remote branch: $branch")
        vcsApi.checkout(branch).also { log(it) }
        log("deleting temporary branch: $tmpBranchName")
        vcsApi.deleteLocalBranch(tmpBranchName).also { log(it) }
        log("Committing new version bump: ${nextVersion.name()}")
        commitEmptyVersionBumpCommit(nextVersion.name())
        log("Pushing new version bump: ${nextVersion.name()}")
        vcsApi.push("origin", branch).also { log(it) }
    }

    private fun bumpMinorVersion() = with(ext) {
        val nextVersion = semanticVersion.nextMinor(newPatch = 1)
        log("Ensuring $developBranchName is checked out and up-to-date: first deleting local branch: $developBranchName")
        vcsApi.deleteLocalBranch(developBranchName).also { log(it) }
        log("Ensuring $developBranchName is checked out and up-to-date: next checking out remote branch: $developBranchName")
        vcsApi.checkout(developBranchName).also { log(it) }
        log("Committing new version bump: $nextVersion")
        commitEmptyVersionBumpCommit(nextVersion.name())
        log("pushing develop branch: $developBranchName")
        vcsApi.push("origin", developBranchName).also { log(it) }
    }

    private fun createReleaseTag() {
        val tagName = "v$versionName"
        log("creating tag: $tagName")
        vcsApi.tag(tagName).also { log(it) }
        log("pushing tag '$tagName' to origin")
        vcsApi.push(to = "origin", branch = tagName).also { log(it) }
    }

    private fun commitEmptyVersionBumpCommit(versionString: String) = vcsApi.commitEmptyWithSkipCI(
            subject = "bump version to $versionString",
            authorName = "CI/CD"
    ).also { log(it) }

    private fun findLastVersion(): String {
        val commit = vcsApi.findLastPrettyCommitIncludingTextInSubject("bump version to")
        val delim = commit.lastIndexOf(" ")
        return commit.substring(delim + 1).replace(Regex("[^0-9.]"), "")
    }

    abstract fun log(message: String)
}