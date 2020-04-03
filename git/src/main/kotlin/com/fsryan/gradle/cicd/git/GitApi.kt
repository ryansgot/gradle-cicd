package com.fsryan.gradle.cicd.git

import com.fsryan.gradle.cicd.VersionControlSystemApi
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

/**
 * Provides abstractions to perform git operations in the project.
 */
class GitApi(private val project: Project): VersionControlSystemApi {

    /**
     * Returns the current branch name as a string
     */
    override fun abbreviatedRevParse(abbrevRef: String) = runCommand("rev-parse", "--abbrev-ref", abbrevRef)

    /**
     * Finds the last commit from HEAD whose subject contains the input text
     * (case sensitive). Commits are looked at 10-at-a-time down to the root.
     * If no matching commit is found, then an [IllegalStateException] is
     * thrown.
     */
    override fun findLastPrettyCommitIncludingTextInSubject(text: String): String {
        var commitOffset = 0
        val totalCount = commitCount()
        while (commitOffset < totalCount) {
            val ret = lastPrettyAbbrevCommitsFromOffset(commitOffset, 10)
                    .firstOrNull { it.contains(text) }
            if (ret != null) {
                return ret
            }
            commitOffset += 10
        }
        throw IllegalStateException("No commit with text '$text' found")
    }

    /**
     * Return the last [count] commits from the [commitOffset] as pretty,
     * one-line commits.
     */
    override fun lastPrettyAbbrevCommitsFromOffset(commitOffset: Int, count: Int) = runCommand(
            "log",
            "--abbrev-commit",
            "--pretty=oneline",
            "--skip=$commitOffset",
            "--max-count=$count"
    ).split("\n")

    /**
     * Return the number of commits from the HEAD revision to the root.
     */
    override fun commitCount() = runCommand("rev-list", "--count", "HEAD").toInt()

    /**
     * Run an arbitrary git command and return the result as a String.
     */
    override fun runCommand(vararg arguments: String): String = ByteArrayOutputStream().use { output ->
        project.exec {
            it.executable = "git"
            it.args = arguments.asList()
            it.standardOutput = output
            it.isIgnoreExitValue = true
        }.rethrowFailure()
        output.toString().trim()
    }

    /**
     * Make an empty (no alterations) commit with a commit name. You can supply
     * the author details. Note that if there are actually staged changes, then
     * those changes _WILL_ be committed.
     */
    override fun commitEmptyWithSkipCI(
            subject: String,
            authorName: String,
            authorEmail: String
    ) = commit(
            allowEmpty = true,
            // [skip ci] is one of the HEAD revision strings that DevOps
            // Pipelines and other CI systems use to filter out builds,
            // allowing you to push to a monitored branch without triggering an
            // infinite loop of CI triggers.
            subject = "[skip ci] $subject",
            authorName = authorName,
            authorEmail = authorEmail
    )

    /**
     * Make a commit, optionally allowing no alterations.
     */
    override fun commit(allowEmpty: Boolean,
               subject: String,
               authorName: String,
               authorEmail: String
    ): String {
        val arguments = when (allowEmpty) {
            true -> arrayOf("commit", "--allow-empty", "--author=$authorName <$authorEmail>", "-m", subject)
            false -> arrayOf("commit", "--author=$authorName <$authorEmail>", "-m", subject)
        }
        return runCommand(*arguments)
    }

    override fun fetchGlobalConfigEmail() = fetchConfig("user.email")
    override fun fetchGlobalConfigUser() = fetchConfig("user.name")
    override fun fetchConfig(parameter: String) = runCommand("config", parameter)

    // TODO: check that the current branch matches . . . if not, use : syntax
    //  to push onto different remote branch
    override fun push(to: String, branch: String) = runCommand("push", to, branch)
    override fun createLocalBranch(name: String) = runCommand("checkout", "-b", name)
    override fun checkout(branch: String) = runCommand("checkout", branch)
    override fun deleteLocalBranch(name: String) = runCommand("branch", "-D", name)
    override fun tag(tagName: String) = runCommand("tag", tagName)
    override fun commitsContainingMessageText(inclusionFilter: String, untilMatches: String?): String {
        val divider = "\n-------\n"
        var commitOffset = 0
        val totalCount = commitCount()
        val buf = StringBuilder()
        while (commitOffset < totalCount) {
            val commit: String = runCommand("log", "--skip=$commitOffset", "--max-count=1", "--pretty=full")
            if (untilMatches != null && commit.contains(untilMatches)) {
                break
            }
            if (commit.contains(inclusionFilter)) {
                buf.append(commit).append(divider)
            }
            commitOffset++
        }
        return when (buf.length >= divider.length) {
            true -> buf.delete(buf.length - divider.length, buf.length)
            false -> buf
        }.toString()
    }
}