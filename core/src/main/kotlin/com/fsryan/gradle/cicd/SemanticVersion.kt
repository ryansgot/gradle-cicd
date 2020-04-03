package com.fsryan.gradle.cicd

/**
 * The semantic verisoning standard is much more involved than this. I picked a
 * bare-bones implementation that made sense for where we are right now. We can
 * get much more involved with this later.
 */
data class SemanticVersion(val major: Int, val minor: Int, val patch: Int) {

    override fun toString() = "${name()} (${code()})"

    fun name(includePatch: Boolean = true) = "$major.$minor${if (includePatch) ".$patch" else ""}"

    // TODO: make these configurable via extension
    fun code() = major * 100000 + minor * 1000 + patch

    companion object {
        /**
         * This parses the input string into a [SemanticVersion], but the
         * output follows our semantic versioning rules. Notably, the lowest
         * possible patch version is 1. Additionally, the highest possible . . .
         * * major version is 99
         * * minor version is 99
         * * patch version is 997 (debug builds have odd version codes)
         *
         * This reads a string of the form major.minor.patch
         * (with major being the only required number) a little naively in that
         * it assumes the string can be parsed into numbers
         */
        fun parse(fromString: String, maxMajorVersion: Int = 99, maxMinorVersion: Int = 99, maxPatchVersion: Int = 997): SemanticVersion {
            var minor = 0
            var patch = 1
            val delim = '.'
            var delimIdx = fromString.indexOf(delim)
            val major = when (delimIdx) {
                -1 -> fromString.toInt()
                else -> fromString.substring(0, delimIdx).toInt()
            }
            if (delimIdx > 0) {
                var nextDelimIdx = fromString.indexOf(delim, delimIdx + 1)
                minor = when (nextDelimIdx) {
                    -1 -> fromString.substring(delimIdx + 1).toInt()
                    else -> fromString.substring(delimIdx + 1, nextDelimIdx).toInt()
                }

                delimIdx = nextDelimIdx
                if (delimIdx > 0) {
                    val patchStr = fromString.substring(delimIdx + 1)
                            .replace(regex = Regex("[^0-9].*"), replacement = "")
                    patch = patchStr.toInt()
                }
            }

            if (major > maxMajorVersion) {
                throw IllegalStateException("Major version greater than 99 not supported")
            }
            if (minor > maxMinorVersion) {
                throw IllegalStateException("Minor version greater than 99 not supported")
            }
            if (patch > maxPatchVersion) {
                throw IllegalStateException("Patch version greater than 997 not supported")
            }
            return SemanticVersion(major, minor, patch)
        }
    }
}

fun SemanticVersion.nextPatch(increment: Int = 1) = copy(patch = patch + increment)
fun SemanticVersion.nextMinor(increment: Int = 1, newPatch: Int = 0) = copy(minor = minor + increment, patch = newPatch)
fun SemanticVersion.nextMajor(increment: Int = 1, newMinor: Int = 0, newPatch: Int = 0) = SemanticVersion(major + increment, newMinor, newPatch)