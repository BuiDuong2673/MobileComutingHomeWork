package com.codemave.mobilecomputing.ui.reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.ui.login.getProfile
import com.codemave.mobilecomputing.ui.signup.SignUpViewModel
import com.google.accompanist.insets.systemBarsPadding
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.data.entity.Account
import com.codemave.mobilecomputing.data.entity.Category
import kotlinx.coroutines.launch
import java.util.*
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.ui.searchByLocation.isLocationNear
import com.codemave.mobilecomputing.ui.theme.Green
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

@Composable
fun Reminder (
    onBackPress: () -> Unit,
    viewModel: ReminderViewModel = viewModel(),
    navController: NavController
) {
    val viewState by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentTimeInMillis = System.currentTimeMillis()
    val title = rememberSaveable{ mutableStateOf("") }
    val category = rememberSaveable{ mutableStateOf("") }
    val creator = rememberSaveable { mutableStateOf(getProfile().name) }
    val remindDay = rememberSaveable{ mutableStateOf("") }
    val remindMonth = rememberSaveable { mutableStateOf("") }
    val remindYear = rememberSaveable{ mutableStateOf("") }
    val remindHour = rememberSaveable{ mutableStateOf("") }
    val remindMin = rememberSaveable{ mutableStateOf("") }
    val timeSystem = rememberSaveable{ mutableStateOf("") }
    val reminderMessage = rememberSaveable{ mutableStateOf("") }
    val sendNotification = rememberSaveable { mutableStateOf(true)}

    //location
    // get the data from the long click location
    val latlng = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<LatLng>("location_data")
        ?.value
    // for permission request activity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    val context = LocalContext.current
    // for the creator information
    val viewModelSignUp: SignUpViewModel = viewModel()
    val viewStateSignUp by viewModelSignUp.state.collectAsState()

    Surface {
        Column(modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()) {
            TopAppBar  {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
                Text(text = "Add Reminder", modifier = Modifier.padding(start = 4.dp))
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
                    label = { Text(text = "Reminder Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                CategoryListDropDown(
                    viewState = viewState,
                    category = category,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = creator.value,
                    onValueChange = {creator.value = it},
                    label = { Text (text = "Creator Name")},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                Spacer (modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    //TODO make this button in the equal row with Latitude and Longitude
                    OutlinedButton(
                        //onClick = {navController.navigate("map")},
                        onClick = {
                            requestPermission(
                                context = context,
                                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                                requestPermission = {launcher.launch (Manifest.permission.ACCESS_FINE_LOCATION)}
                            ).apply {
                                navController.navigate("map")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(fraction = 0.35f)
                    ) {
                        Text(text = "Reminder Location", textAlign = TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = if (latlng == null) {
                                    ""
                                } else {
                                    String.format(Locale.getDefault(),
                                    "%1$.2f",
                                    latlng.latitude)
                                },
                        onValueChange = {},
                        label = { Text(text = "Latitude", textAlign = TextAlign.Center)},
                        modifier = Modifier.fillMaxWidth(fraction = 0.5f),//fraction is the ratio of the space compare with max width of the left elements in the row
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = if (latlng == null) {
                                    ""
                                } else {
                                       String.format(Locale.getDefault(),
                                       "%1$.2f",
                                       latlng.longitude)
                               },
                        onValueChange = {},
                        label = { Text (text = "Longitude", textAlign = TextAlign.Center)},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Choose the remind date",
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
                    value = reminderMessage.value,
                    onValueChange = {reminderMessage.value = it},
                    label = {Text(text = "Message")},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (title.value != "" && category.value.isNotEmpty() && remindDay.value != "" && remindMonth.value != "" && remindYear.value != "" && remindHour.value != "" && remindMin.value != "" && timeSystem.value != "") {
                        //if (title.value != "" && category.value.isNotEmpty()) {
                            coroutineScope.launch {
                                if (Build.VERSION.SDK_INT >= 33) {
                                    requestPermission(
                                        context = context,
                                        permission = Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                                        requestPermission = {launcher.launch (Manifest.permission.ACCESS_NOTIFICATION_POLICY)}
                                    )
                                }
                                val reminder = Reminder(
                                        reminderTitle = title.value,
                                        reminderCategoryId = getCategoryId(viewState.categories, category.value),
                                        creatorId = getCreatorId(viewStateSignUp.accounts, creator.value),
                                        locationX = latlng?.latitude?:65.06,
                                        locationY = latlng?.longitude?:25.47,
                                        reminderTime = dateToString(remindDay.value, remindMonth.value, remindYear.value, remindHour.value, remindMin.value, timeSystem.value),
                                        creationTime = getCurrentTime(),//currentTimeInMillis.toDateTimeString(),
                                        reminderMessage = reminderMessage.value,
                                        sendNotification = sendNotification.value
                                    )
                                viewModel.saveReminder(
                                    reminder
                                )
                                reminderIsNear(reminder)
                            }
                            onBackPress()
                        } else {
                            Toast.makeText(Graph.appContext, "Please enter all of the value to save the reminder", Toast.LENGTH_LONG).show()
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

@Composable
fun CategoryListDropDown(
    viewState: ReminderViewState,
    category: MutableState<String>,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded) {
        Icons.Filled.ArrowDropUp
    } else {
        Icons.Filled.ArrowDropDown
    }
    Column (modifier = modifier) {
        OutlinedTextField(
            value = category.value,
            onValueChange = {category.value = it},
            modifier = Modifier.fillMaxWidth(),
            label = {Text("Category")},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            viewState.categories.forEach { dropDownOption ->
                DropdownMenuItem(
                    onClick = {
                        category.value = dropDownOption.name
                        expanded = false
                    }
                ) {
                    Text(dropDownOption.name)
                }
            }
        }
    }
}

@Composable
fun MonthListDropDown(
    month: MutableState<String>,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded) {
        Icons.Filled.ArrowDropUp
    } else {
        Icons.Filled.ArrowDropDown
    }
    Column (modifier = modifier) {
        OutlinedTextField(
            value = month.value,
            onValueChange = {month.value = it},
            modifier = Modifier.fillMaxWidth(),
            label = {Text("Month")},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded}
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownMenuItem(
                onClick = {
                    month.value = "January"
                    expanded = false
                }
            ) {
                Text(text = "January")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "February"
                    expanded = false
                }
            ) {
                Text(text = "February")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "March"
                    expanded = false
                }
            ) {
                Text(text = "March")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "April"
                    expanded = false
                }
            ) {
                Text(text = "April")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "May"
                    expanded = false
                }
            ) {
                Text(text = "May")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "June"
                    expanded = false
                }
            ) {
                Text(text = "June")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "July"
                    expanded = false
                }
            ) {
                Text(text = "July")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "August"
                    expanded = false
                }
            ) {
                Text(text = "August")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "September"
                    expanded = false
                }
            ) {
                Text(text = "September")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "October"
                    expanded = false
                }
            ) {
                Text(text = "October")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "November"
                    expanded = false
                }
            ) {
                Text(text = "November")
            }
            DropdownMenuItem(
                onClick = {
                    month.value = "December"
                    expanded = false
                }
            ) {
                Text(text = "December")
            }
        }
    }
}

@Composable
fun TimeKeepingSystem(
    timeSystem: MutableState<String>,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded) {
        Icons.Filled.ArrowDropUp
    } else {
        Icons.Filled.ArrowDropDown
    }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = timeSystem.value,
            onValueChange = {timeSystem.value = it},
            modifier = Modifier.fillMaxWidth(),
            label = {Text("AM/PM")},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded}
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownMenuItem(
                onClick = {
                    timeSystem.value = "AM"
                    expanded = false
                }
            ) {
                Text("AM")
            }
            DropdownMenuItem(
                onClick = {
                    timeSystem.value = "PM"
                    expanded = false
                }
            ) {
                Text("PM")
            }
        }
    }
}

fun dateToString(day: String, month: String, year: String, hour: String, min: String, timeSystem: String): String {
    return "$month $day, $year ${hour}:${min} $timeSystem"
}

fun Long.toDateTimeString(): String {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
    return dateFormat.format(Date(this))
}

fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
    return dateFormat.format(Date(System.currentTimeMillis()))
}

/**
 * Check if it is a previous reminder or upcoming reminder
 */
fun isPrevious(timeString: String): Boolean {
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
    val date = dateFormat.parse(timeString)
    val currentDate = Date(System.currentTimeMillis())
    val compareResult = date.compareTo(currentDate)
    return compareResult <= 0
}

fun getCategoryId(categories: List<Category>, categoryName: String): Long {
    val category: Category? = categories.first { category -> category.name == categoryName}
    return category?.id?:-1
}

fun getCreatorId(accounts: List<Account>, userName: String): Long {
    val account: Account? = accounts.first { account -> account.name == userName}
    return account?.id?:-1
}

fun requestPermission(
    context: Context,
    permission: String,
    requestPermission: () -> Unit
) {
    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
        requestPermission()
    }
}

fun reminderIsNear(reminder: Reminder) {
    var userLocation: Location = Location("")
    var fusedLocationClient = LocationServices.getFusedLocationProviderClient(Graph.appContext)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                userLocation = location
            }
        }
    var reminderLocation = LatLng(reminder.locationX, reminder.locationY)
    var userLocationInLatLng = LatLng(userLocation.latitude, userLocation.longitude)
    if (isLocationNear(reminderLocation, userLocationInLatLng)) {
        reminderIsNearNotification(reminder)
    }
}
