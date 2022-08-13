package com.liewjuntung.screens

import com.liewjuntung.gpgui.core.User
import java.io.File


data class GpgState(
    val userList: List<User>,
    val files: List<File>,
    val selectedUser: List<User>
)