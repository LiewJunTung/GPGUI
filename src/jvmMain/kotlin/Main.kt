// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.liewjuntung.screens.*
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File


fun main() = application {
    val icon = painterResource("black_lock.png")
    Window(
        title = "GPGUI",
        icon = icon,
        onCloseRequest = ::exitApplication
    ) {
        val repository = GpgRepository()
        var gpgState by remember { mutableStateOf<Lce<GpgState>?>(null) }
        val scope = rememberCoroutineScope()

        var gpgOptions by remember { mutableStateOf(GpgOptions.NONE) }
        var dialogOpened by remember { mutableStateOf(false) }
        var password by rememberSaveable { mutableStateOf("") }
        var version by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            try {
                gpgState = Lce.Loading
                version = repository.checkGpg()
                gpgState = null
                println("success!")
            } catch (e: Throwable) {

                gpgState = Lce.Error(e)
            }
        }
        if (dialogOpened) {
            FileDialog(
                gpgOptions = gpgOptions,
                onOpen = {
                    gpgState = Lce.Loading
                },
                onCloseRequest = { files ->

                    if (!files.isNullOrEmpty()) {
                        scope.launch {
                            gpgState = try {
                                repository.loadUserList(files)
                            } catch (e: Exception) {
                                Lce.Error(e)
                            }
                        }
                    } else {
                        gpgState = null
                        gpgOptions = GpgOptions.NONE
                    }
                    dialogOpened = false
                }
            )
        }
        MaterialTheme {

            if (gpgState is Lce.Error) {
                val state = gpgState as Lce.Error
                Column {
                    Text(color = Color.Red, text = "error: " + state.error)
                    Text(color = Color.Red, text = "error: Please check if gpg is installed correctly")
                }
            } else {

                GpgScreen(gpgState,
                    version = version,
                    gpgOptions = gpgOptions,
                    password = password,
                    onPasswordTextChange = {
                        password = it
                    },
                    fileAction = {
                        val state = gpgState
                        val data = if (state is Lce.Content<GpgState>) {
                            state.data
                        } else {
                            gpgState = Lce.Error(Throwable("files not found in state"))
                            return@GpgScreen
                        }
                        gpgState = state.copy(success = null, error = null)
                        gpgState = Lce.Loading
                        scope.launch {
                            try {
                                when (gpgOptions) {
                                    GpgOptions.ENCRYPT -> {
                                        repository.encryptFiles(data.files, data.selectedUser)
                                    }

                                    GpgOptions.DECRYPT -> {
                                        repository.decryptFiles(data.files, password)
                                    }

                                    else -> {}
                                }
                                gpgState = state.copy(success = "Success!")
                            } catch (e: Throwable) {
                                gpgState = state.copy(error = e.message)
                            }

                        }
                    },
                    userSelectableCallback = { selected, user ->
                        val state = gpgState
                        if (state is Lce.Content<GpgState>) {
                            val newUserList = if (selected) {
                                listOf(*state.data.selectedUser.toTypedArray(), user)
                            } else {
                                state.data.selectedUser.filterNot { user.email == it.email }
                            }
                            gpgState = Lce.Content(state.data.copy(selectedUser = newUserList))
                        }
                    }) {
                    gpgOptions = it
                    dialogOpened = true
                }
            }

        }
    }
}

@Composable
private fun FileDialog(
    gpgOptions: GpgOptions,
    parent: Frame? = null,
    onOpen: () -> Unit,
    onCloseRequest: (result: List<File>?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a file", LOAD) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    onCloseRequest(files.toList())
                }
            }
        }.apply {
            isMultipleMode = true
            if (gpgOptions == GpgOptions.DECRYPT) {
                this.setFilenameFilter { _, name -> name.endsWith(".asc") }
            } else if (gpgOptions == GpgOptions.ENCRYPT) {
                this.setFilenameFilter { _, name -> !name.endsWith(".asc") }
            }
        }
    },
    dispose = FileDialog::dispose
)