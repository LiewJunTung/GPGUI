package com.liewjuntung.screens

import com.lordcodes.turtle.shellRun
import java.io.File


interface GpgHelper {
    fun checkGpg(): String
    fun loadUsers(): List<User>
    fun encrypt(inputFile: File, outputFile: File, vararg users: String)
    fun decrypt(inputFile: File, outputFile: File, passphrase: String)
}

class GpgHelperImpl : GpgHelper {
    override fun checkGpg(): String {
        return when (operatingSystem) {
            OS.MAC -> {
                shellRun("/opt/homebrew/bin/gpg", listOf("--version"))
            }
            OS.LINUX -> {
                shellRun("/usr/bin/gpg", listOf("--version"))
            }
            else -> throw Exception("$operatingSystem not supported")
        }
    }


    override fun loadUsers(): List<User> {
        val process = ProcessBuilder("bash", "-l", "-c", "/opt/homebrew/bin/gpg --list-keys").start()
        val output = process.inputStream.readAllBytes().decodeToString()
        File("/Users/liewjuntung/Dev/gpgui/output.txt").writeText(output)
        val userRegex = Regex(".*\\[(.*)] ([A-Za-z_\\s]+)( \\(.*\\) )?<(.*)>")
        return output
            .lines()
            .drop(2)
            .filter { it.contains("uid") }
            .zip(
                output
                    .lines()
                    .drop(2)
                    .filterNot { it.isBlank() ||
                            it.contains("sub") ||
                            it.contains("pub") ||
                            it.contains("uid") }
            ).filterNot { pair ->
                val matches = userRegex.find(pair.first)!!.groups
                matches[1]?.value?.contains("expired") ?: false
            }.map { pair ->
                val matches = userRegex.find(pair.first)!!.groups
                val username = matches[2]!!.value
                val email = if (matches.size == 5) matches[4]!!.value else matches[3]!!.value
                User(pair.second.trim(), username, email)
            }.distinctBy {
                it.email
            }
    }

    override fun encrypt(inputFile: File, outputFile: File, vararg users: String) {
        val options = arrayListOf(
            "--output",
            outputFile.path,
            "--encrypt",
            "--armor",
            "--batch",
            "--yes",
        ).apply {
            users.forEach {
                addAll(listOf("-r", it))
            }
            add(inputFile.path)
        }
        println(options.joinToString(" "))
        shellRun("/opt/homebrew/bin/gpg", options)
    }

    override fun decrypt(inputFile: File, outputFile: File, passphrase: String) {
        shellRun(
            "/opt/homebrew/bin/gpg",
            listOf(
                "--output",
                outputFile.path,
                "--passphrase",
                passphrase,
                "--pinentry-mode",
                "loopback",
                "--batch",
                "--yes",
                "--decrypt",
                inputFile.path
            )
        )
        shellRun("/opt/homebrew/bin/gpg-connect-agent", listOf("reloadagent", "/bye"))

    }
}