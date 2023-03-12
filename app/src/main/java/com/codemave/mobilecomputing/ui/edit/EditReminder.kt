package com.codemave.mobilecomputing.ui.edit

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.entity.Category
import com.codemave.mobilecomputing.ui.home.categoryReminder.getCategory
import com.codemave.mobilecomputing.ui.home.categoryReminder.getReminder
import com.codemave.mobilecomputing.ui.login.getProfile
import com.google.accompanist.insets.systemBarsPadding
import java.util.*
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.ui.home.categoryReminder.setReminder
import com.codemave.mobilecomputing.ui.reminder.*
import com.codemave.mobilecomputing.ui.theme.Green
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@Composable
fun EditReminder (
    navController: NavController,
    viewModel: ReminderViewModel = viewModel(),
    onBackPress: () -> Unit
) {
    val viewState by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val reminder = getReminder()
    val title = rememberSaveable { mutableStateOf(reminder.reminderTitle)}
    // TODO sometime crash because of null
    // val category = rememberSaveable { mutableStateOf(getCategoryName(viewState.categories, reminder.reminderCategoryId)) }
    val category = rememberSaveable { mutableStateOf(getCategory().name)}
    // show the creator name (from log in account)
    val user = getProfile()
    val creator = rememberSaveable { mutableStateOf(user.name)}
    // show and allow editing the reminder time
    val remindTime = splitTime(reminder.reminderTime)
    val remindMonth = rememberSaveable { mutableStateOf(remindTime[0]) }
    val remindDay = rememberSaveable { mutableStateOf(remindTime[1]) }
    val remindYear = rememberSaveable { mutableStateOf(remindTime[2]) }
    val remindHour = rememberSaveable { mutableStateOf(remindTime[3]) }
    val remindMin= rememberSaveable { mutableStateOf(remindTime[4]) }
    val timeSystem = rememberSaveable { mutableStateOf(remindTime[5]) }
    val newRemindTime = dateToString(day = remindDay.value, month = remindMonth.value, year = remindYear.value, hour = remindHour.value, min = remindMin.value, timeSystem = timeSystem.value)

    val createTime = rememberSaveable { mutableStateOf(reminder.creationTime) }
    val reminderMessage = rememberSaveable { mutableStateOf(reminder.reminderMessage) }
    val sendNotification = rememberSaveable { mutableStateOf(reminder.sendNotification)}

    //location
    val locationX = rememberSaveable { mutableStateOf(reminder.locationX)}
    val locationY = rememberSaveable { mutableStateOf(reminder.locationY)}

    // get the dat from the long click location
    val latlng = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<LatLng>("location_data")
        ?.value

    Surface {
        Column(modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()) {
            TopAppBar {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
                Text(text = "Reminder")
                Switch(
                    checked = sendNotification.value,
                    onCheckedChange = { sendNotification.value = it },
                    modifier = Modifier.padding(start = 180.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green,
                        checkedTrackColor = Green
                    )
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = {title.value = it},
                    label = { Text(text = "Reminder Title")},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CategoryListDropDown(
                        viewState = viewState,
                        category = category,
                        modifier = Modifier.fillMaxWidth(fraction = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = creator.value,
                        onValueChange = {creator.value = it},
                        label = {Text(text = "Creator Name")},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {navController.navigate("map") },
                        modifier = Modifier.fillMaxWidth(fraction = 0.35f)
                    ) {
                        Text(text = "Reminder Location", textAlign = TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = if (latlng == null) {
                                    String.format(
                                        Locale.getDefault(),
                                        "%1$.2f",
                                        reminder.locationX
                                    )
                                } else {
                                       String.format(Locale.getDefault(),
                                       "%1$.2f",
                                       latlng.latitude)
                                },
                        onValueChange = {},
                        label = {Text(text = "Latitude", textAlign = TextAlign.Center)},
                        modifier = Modifier.fillMaxWidth(fraction = 0.5f),
                        enabled = false
                    )
                    Spacer (modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = if (latlng == null) {
                                    String.format(
                                        Locale.getDefault(),
                                        "%1$.2f",
                                        reminder.locationY
                                    )
                                } else {
                                    String.format(Locale.getDefault(),
                                    "%1$.2f",
                                    latlng.longitude)
                                },
                        onValueChange = {},
                        label = {Text(text = "Longitude", textAlign = TextAlign.Center)},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Remind Date",
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = remindDay.value,
                        onValueChange = {remindDay.value = it},
                        label = { Text(text = "Day") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(end = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MonthListDropDown(
                        month = remindMonth,
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(end = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = remindYear.value,
                        onValueChange = {remindYear.value = it},
                        label = {Text(text = "Year")},
                        keyboardOptions = KeyboardOptions (
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.weight(0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Divider()
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Choose the reminder time",
                    maxLines = 1,
                    style = MaterialTheme.typography.subtitle1
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = remindHour.value,
                        onValueChange = {remindHour.value = it},
                        label = {Text(text = "Hour")},
                        keyboardOptions = KeyboardOptions (
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(end = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = remindMin.value,
                        onValueChange = {remindMin.value = it},
                        label = {Text(text = "Minute")},
                        keyboardOptions = KeyboardOptions (
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(end = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TimeKeepingSystem(
                        timeSystem = timeSystem,
                        modifier = Modifier.weight(0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = createTime.value,
                    onValueChange = {createTime.value },
                    label = { Text(text = "Create Time")},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = reminderMessage.value,
                    onValueChange = {reminderMessage.value = it},
                    label = {Text(text = "Message")},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if ((title.value != reminder.reminderTitle)
                            || (getCategoryId(viewState.categories, category.value) != reminder.reminderCategoryId)
                            || newRemindTime != reminder.reminderTime
                            || reminderMessage.value != reminder.reminderMessage
                            || sendNotification.value != reminder.sendNotification
                            || latlng != null
                        ) {
                            val editedReminder = Reminder(
                                reminderId = reminder.reminderId,
                                reminderTitle = title.value,
                                reminderCategoryId = getCategoryId(
                                    viewState.categories,
                                    category.value
                                ),
                                creatorId = user.id,
                                locationX = latlng?.latitude?: reminder.locationX,
                                locationY = latlng?.longitude?: reminder.locationY,
                                reminderTime = newRemindTime,
                                creationTime = reminder.creationTime,
                                reminderMessage = reminderMessage.value,
                                sendNotification = sendNotification.value
                            )
                            coroutineScope.launch {
                                viewModel.editReminder(
                                    editedReminder
                                )
                                setReminder(editedReminder)
                            }
                            onBackPress()
                        } else {
                            Toast.makeText(Graph.appContext, "Please modify some value or press Back button if you do not want to change.", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(55.dp)
                ) {
                    Text("Save Reminder")
                }
            }
        }
    }
}

fun getCategoryName(categories: List<Category>, categoryId: Long): String {
    val category: Category? = categories.first { category -> category.id == categoryId}
    return category?.name?:""
}

fun splitTime(timeString: String): Array<String> {
    val timeFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.ENGLISH)
    val dateTime = timeFormat.parse(timeString)
    val calendar = Calendar.getInstance().apply { time = dateTime}

    val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
    val year = calendar.get(Calendar.YEAR).toString()
    val hour = calendar.get(Calendar.HOUR).toString()
    val minute = calendar.get(Calendar.MINUTE).toString()
    val timeSystem = calendar.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.ENGLISH)
    return arrayOf(month, day, year, hour, minute, timeSystem)
}



