package com.liewjuntung.screens

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

class GpgRepository {
    private val gpgHelper = GpgHelperImpl()

    private suspend fun getUserList(): List<User> {
        return withContext(IO) {
            gpgHelper.loadUsers()
        }
    }

    suspend fun checkGpg(): String = withContext(IO) {
        gpgHelper.checkGpg()
    }

    suspend fun loadUserList(inputFiles: List<File>): Lce<GpgState> {
        return try {
            val result = getUserList()
            Lce.Content(GpgState(result, inputFiles, listOf()))
        } catch (e: Exception) {
            e.printStackTrace()
            Lce.Error(e)
        }
    }

    suspend fun encryptFiles(files: List<File>, users: List<User>) {
        withContext(IO) {
            val emails = users.map { it.email }.toTypedArray()
            files.forEach {
                gpgHelper.encrypt(it, File("${it.path}.asc"), *emails)
            }
        }
    }


    suspend fun decryptFiles(files: List<File>, passphrase: String) {
        withContext(IO) {
            files.forEach {
                gpgHelper.decrypt(it, File(it.path.removeSuffix(".asc")), passphrase)
            }
        }
    }
}