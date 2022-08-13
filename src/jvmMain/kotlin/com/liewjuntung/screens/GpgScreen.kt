@file:OptIn(ExperimentalMaterialApi::class)

package com.liewjuntung.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import compose.icons.AllIcons
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Eye
import compose.icons.fontawesomeicons.regular.EyeSlash

enum class GpgOptions {
    ENCRYPT, DECRYPT, NONE
}

@Composable
fun SwitchButton(selected: Boolean, onClick: () -> Unit, content: @Composable (RowScope.() -> Unit)) {
    if (selected) {
        Button(
            onClick = onClick,
            content = content
        )
    } else {
        OutlinedButton(
            onClick = onClick,
            content = content
        )
    }

}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun GpgScreen(
    gpgState: Lce<GpgState>?,
    gpgOptions: GpgOptions,
    version: String,
    password: String,
    onPasswordTextChange: (String) -> Unit,
    userSelectableCallback: (Boolean, User) -> Unit,
    fileAction: () -> Unit,
    filePickerButton: (GpgOptions) -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    Box(modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight()) {

        Text(
            version.lines().take(4).joinToString("\n"),
            modifier = Modifier.align(Alignment.BottomEnd),
            fontSize = TextUnit(8f, TextUnitType.Sp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SwitchButton(
                    selected = gpgOptions == GpgOptions.ENCRYPT,
                    onClick = { filePickerButton(GpgOptions.ENCRYPT) }) {
                    Text("Select Files To Encrypt")
                }
                SwitchButton(
                    selected = gpgOptions == GpgOptions.DECRYPT,
                    onClick = { filePickerButton(GpgOptions.DECRYPT) }) {
                    Text("Select Files To Decrypt")
                }
                // Button will go here
            }

            when (gpgState) {
                is Lce.Content ->
                    Column {
                        Row(modifier = Modifier.weight(1f)) {
                            LazyColumn(
                                modifier =
                                Modifier.weight(1f)
                            ) {
                                items(gpgState.data.files) { file ->
                                    ListItem(
                                        modifier = Modifier.clickable {
                                        },
                                        text = { Text(file.name) },
                                        secondaryText = { Text(file.path) },

                                        )
                                }
                            }
                            if (gpgOptions == GpgOptions.ENCRYPT) {
                                EncryptWidget(
                                    gpgState = gpgState,
                                    modifier = Modifier.weight(1f),
                                    userSelectableCallback = userSelectableCallback
                                )
                            } else if (gpgOptions == GpgOptions.DECRYPT) {
                                TextField(
                                    value = password,
                                    onValueChange = onPasswordTextChange,
                                    label = { Text("Passphrase") },
                                    singleLine = true,
                                    modifier =
                                    Modifier.weight(1f),
                                    placeholder = { Text("Your secret passphrase") },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    trailingIcon = {
                                        val image = if (passwordVisible)
                                            FontAwesomeIcons.Regular.Eye
                                        else {
                                            FontAwesomeIcons.Regular.EyeSlash
                                        }

                                        // Please provide localized description for accessibility services
                                        val description = if (passwordVisible) "Hide password" else "Show password"

                                        IconButton(
                                            onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(imageVector = image, description, modifier = Modifier.size(24.dp))
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Filled.Lock, "Passphrase Text") },
                                )
                            }
                        }
                        if (!gpgState.success.isNullOrEmpty()) {
                            Text(color = Color.Green, text = "Success!")
                        } else if (!gpgState.error.isNullOrEmpty()) {
                            Text(color = Color.Red, text = "error: " + gpgState.error)
                        }
                        Row {
                            Button(
                                modifier = Modifier.weight(1f).padding(16.dp),
                                enabled = gpgOptions == GpgOptions.ENCRYPT && gpgState.data.selectedUser.isNotEmpty()
                                        || gpgOptions == GpgOptions.DECRYPT && password.isNotEmpty(),
                                onClick = { fileAction() }
                            ) {
                                Text(
                                    when (gpgOptions) {
                                        GpgOptions.ENCRYPT -> "Encrypt"
                                        GpgOptions.DECRYPT -> "Decrypt"
                                        else -> ""
                                    }
                                )
                            }
                        }
                    }

                is Lce.Error -> Text(color = Color.Red, text = "error: " + gpgState.error.message)
                Lce.Loading -> LoadingUI()
                null -> Unit
            }
        }
    }
}

@Composable
fun EncryptWidget(
    gpgState: Lce.Content<GpgState>,
    modifier: Modifier,
    userSelectableCallback: (Boolean, User) -> Unit
) {
    var userQuery by remember { mutableStateOf("") }
    Column(modifier = modifier) {
        Row {
            TextField(
                value = userQuery,
                onValueChange = { txt -> userQuery = txt },
                label = { Text("Search") },
                singleLine = true,
                modifier =
                Modifier.weight(1f),
                placeholder = { Text("Search for user name or email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Filled.Search, "Search user") },
            )
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(gpgState.data.userList.filter { it.name.contains(userQuery) || it.email.contains(userQuery) }) { user ->
                ListItem(
                    text = { Text(user.name) },
                    secondaryText = { Text(user.email) },
                    overlineText = { Text(user.id) },
                    trailing = {
                        Checkbox(
                            checked = gpgState.data.selectedUser.any { user.email == it.email },
                            onCheckedChange = null // null recommended for accessibility with screenreaders
                        )
                    },
                    modifier = Modifier.toggleable(
                        value = gpgState.data.selectedUser.any { user.email == it.email },
                        onValueChange = {
                            userSelectableCallback(it, user)
                        }
                    )
                )
            }
        }
    }

}

@Composable
fun LoadingUI() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .defaultMinSize(minWidth = 96.dp, minHeight = 96.dp)
        )
    }
}


@Composable
fun ContentUI(data: GpgState) {
    var userState by remember { mutableStateOf<Lce<GpgState>?>(null) }
    val scope = rememberCoroutineScope()

}