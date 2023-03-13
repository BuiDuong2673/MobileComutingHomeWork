package com.codemave.mobilecomputing.ui.home.categoryReminder

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.data.entity.Category
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.ui.reminder.ReminderViewModel
import com.codemave.mobilecomputing.ui.reminder.getCurrentTime
import com.codemave.mobilecomputing.util.viewModelProviderFactoryOf
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.data.room.ReminderToCategory
import com.codemave.mobilecomputing.ui.reminder.isPrevious
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

/**
 * Make the list of reminders in the Past
 */
@Composable
fun CategoryReminder (
    categoryId: Long,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val viewModel: CategoryReminderViewModel = viewModel (
        key = "category_list $categoryId",
        factory = viewModelProviderFactoryOf { CategoryReminderViewModel(categoryId) }
    )
    val viewState by viewModel.state.collectAsState()

    Column(modifier = modifier) {
        ReminderListPrevious(
            list = viewState.reminders,
            navController = navController
        )
    }
}

/**
 * List all the reminders that happened in the PAST
 */
@Composable
private fun ReminderListPrevious (
    list: List<ReminderToCategory>,
    navController: NavController
) {
    LazyColumn(
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.Center
    ) {
        items(list) { item ->
            if (isPrevious(item.reminder.reminderTime)) {
                ReminderListItem(
                    reminder = item.reminder,
                    category = item.category,
                    onClick = {},
                    modifier = Modifier.fillParentMaxWidth(),
                    navController = navController
                )
            }
        }
    }
}
/**
 * Make the list of all reminder in the home screen
 */
@Composable
fun CategoryReminderAll(
    categoryId: Long,
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: CategoryReminderViewModel = viewModel(
        key = "category_list_all $categoryId",
        factory = viewModelProviderFactoryOf { CategoryReminderViewModel(categoryId) }
    )
    val viewState by viewModel.state.collectAsState()

    Column(modifier = modifier) {
        ReminderListAll(
            list = viewState.reminders,
            navController = navController
        )
    }
}

@Composable
private fun ReminderListAll(
    list: List<ReminderToCategory>,
    navController: NavController
) {
    LazyColumn(
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.Center
    ) {
        items(list) { item ->
            ReminderListItem(
                reminder = item.reminder,
                category = item.category,
                onClick = {},
                modifier = Modifier.fillParentMaxWidth(),
                navController = navController
            )
        }
    }
}

/**
 * Design for each reminder's appearance in the home screen
 */
@Composable
private fun ReminderListItem(
    reminder: Reminder,
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderViewModel = viewModel(),
    navController: NavController
) {
    ConstraintLayout(modifier = modifier.clickable { onClick() }) {
        val coroutineScope = rememberCoroutineScope()
        val (divider, reminderTitle, reminderCategory, iconDelete, iconMore, iconCheck, date) = createRefs()
        Divider(
            Modifier.constrainAs(divider) {
                top.linkTo(parent.top)
                centerHorizontallyTo(parent)
                width = Dimension.fillToConstraints
            }
        )
        // Title
        Text(
            text = reminder.reminderTitle,
            maxLines = 1,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.constrainAs(reminderTitle) {
                linkTo(
                    start = parent.start,
                    end = iconDelete.start,
                    startMargin = 24.dp,
                    endMargin = 16.dp,
                    bias = 0f
                )
                top.linkTo(parent.top, margin = 10.dp)
                width = Dimension.preferredWrapContent
            }
        )
        // Category
        Text(
            text = category.name,
            maxLines = 1,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.constrainAs(reminderCategory) {
                linkTo(
                    start = parent.start,
                    end = iconDelete.start,
                    startMargin = 24.dp,
                    endMargin = 8.dp,
                    bias = 0f //float towards the start
                )
                top.linkTo(reminderTitle.bottom, margin = 6.dp)
                bottom.linkTo(parent.bottom, 10.dp)
                width = Dimension.preferredWrapContent
            }
        )
        // Date
        Text(
            text = reminder.reminderTime,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.constrainAs(date) {
                linkTo(
                    start = reminderCategory.end,
                    end = iconDelete.start,
                    startMargin = 8.dp,
                    endMargin = 16.dp,
                    bias = 0f
                )
                centerVerticallyTo(reminderCategory)
                top.linkTo(reminderTitle.bottom, 6.dp)
                bottom.linkTo(parent.bottom, 10.dp)
            }
        )
        // Icon
        var expanded by remember { mutableStateOf(false)}
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .size(50.dp)
                .padding(6.dp)
                .constrainAs(iconMore) {
                    top.linkTo(parent.top, 10.dp)
                    bottom.linkTo(parent.bottom, 10.dp)
                    end.linkTo(iconCheck.start)
                }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.more_vertical)
            )
        }
        var checkTint by remember { mutableStateOf(
            if (reminder.reminderSeen) {
                Color.Green
            } else {
                Color.Black
            }
        )}
        IconButton(
            onClick = { 
                reminder.reminderSeen = !reminder.reminderSeen
                checkTint = if (reminder.reminderSeen) {
                    Color(0xFF008000)
                } else {
                    Color.Black
                }
            },
            modifier = Modifier
                .size(50.dp)
                .padding(6.dp)
                .constrainAs(iconCheck) {
                    top.linkTo(parent.top, 10.dp)
                    bottom.linkTo(parent.bottom, 10.dp)
                    end.linkTo(parent.end)
                },
            enabled = true
        ) {
            Icon(
                imageVector = Icons.Filled.Check, 
                contentDescription = stringResource(id = R.string.check),
                tint = checkTint
            )
        }
        DropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
           Column(
               modifier = Modifier.fillMaxWidth(),
               horizontalAlignment = Alignment.CenterHorizontally
           ) {
               IconButton(
                   onClick = { 
                       coroutineScope.launch { 
                           viewModel.deleteReminder(reminder)
                       }
                   },
                   modifier = Modifier
                       .size(50.dp)
                       .padding(6.dp),
                   enabled = true
               ) {
                   Icon(
                       imageVector = Icons.Filled.Delete,
                       contentDescription = stringResource(id = R.string.delete)
                   )
               }
               Spacer(modifier = Modifier.height(10.dp))
               IconButton(
                   onClick = {
                       setReminder(reminder)
                       setCategory(category) //TODO how to get the category from reminder_category_id
                       navController.navigate(route = "edit")
                   },
                   modifier = Modifier
                       .size(50.dp)
                       .padding(6.dp),
                   enabled = true
               ) {
                   Icon(
                       imageVector = Icons.Filled.Edit,
                       contentDescription = stringResource(id = R.string.edit)
                   )
               }
           } 
        }
    }
}

var aReminder: Reminder = Reminder(reminderTitle = "DefaultReminder", reminderCategoryId = 1, creatorId = 1, reminderTime = getCurrentTime(), creationTime = getCurrentTime(), reminderMessage = "Default Note")
fun setReminder(reminder: Reminder) {
    aReminder = reminder
}
fun getReminder(): Reminder {
    return aReminder
}
//TODO how to get category from reminder_category_id
var aCategory: Category = Category(name = "Food")
fun setCategory(category: Category) {
    aCategory = category
}
fun getCategory(): Category {
    return aCategory
}