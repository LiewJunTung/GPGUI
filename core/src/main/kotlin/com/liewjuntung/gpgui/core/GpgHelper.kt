package com.liewjuntung.gpgui.core

import com.lordcodes.turtle.shellRun
import java.io.File

interface GpgHelper {
    fun checkGpg(): String
    fun loadUsers(): List<User>
    fun encrypt(inputFile: File, outputFile: File, vararg users: String)
    fun decrypt(inputFile: File, outputFile: File, passphrase: String)
}
