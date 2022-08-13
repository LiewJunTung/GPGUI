package com.liewjuntung.screens

import java.io.File

data class User(val id: String, val name: String, val email: String)

data class GpgState(
    val userList: List<User>,
    val files: List<File>,
    val selectedUser: List<User>
)