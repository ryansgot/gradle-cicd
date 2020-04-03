package com.fsryan.gradle.cicd

interface VersionControlSystemApi {
    /**
     * Returns the current branch name as a string
     */
    fun abbreviatedRevParse(abbrevRef: String = "HEAD"): String

    /**
     * Finds the last commit from HEAD whose subject contains the input text
     * (case sensitive). Commits are looked at 10-at-a-time down to the root.
     * If no matching commit is found, then an [IllegalStateException] is
     * thrown.
     */
    fun findLastPrettyCommitIncludingTextInSubject(text: String): String

    /**
     * Return the last [count] commits from the [commitOffset] as pretty,
     * one-line commits.
     */
    fun lastPrettyAbbrevCommitsFromOffset(commitOffset: Int, count: Int): List<String>

    /**
     * Return the number of commits from the HEAD revision to the root.
     */
    fun commitCount(): Int

    /**
     * Run an arbitrary vcs command and return the result as a String.
     */
    fun runCommand(vararg arguments: String): String

    /**
     * Make an empty (no alterations) commit with a commit name. You can supply
     * the author details. Note that if there are actually staged changes, then
     * those changes _WILL_ be committed.
     */
    fun commitEmptyWithSkipCI(
            subject: String,
            authorName: String = fetchGlobalConfigUser(),
            authorEmail: String = fetchGlobalConfigEmail()
    ): String

    /**
     * Make a commit, optionally allowing no alterations.
     */
    fun commit(allowEmpty: Boolean,
               subject: String,
               authorName: String = fetchGlobalConfigUser(),
               authorEmail: String = fetchGlobalConfigEmail()
    ): String

    fun fetchGlobalConfigEmail(): String
    fun fetchGlobalConfigUser(): String
    fun fetchConfig(parameter: String): String
    fun push(to: String, branch: String): String
    fun createLocalBranch(name: String): String
    fun checkout(branch: String): String
    fun deleteLocalBranch(name: String): String
    fun tag(tagName: String): String
    fun commitsContainingMessageText(inclusionFilter: String, untilMatches: String? = null): String
}