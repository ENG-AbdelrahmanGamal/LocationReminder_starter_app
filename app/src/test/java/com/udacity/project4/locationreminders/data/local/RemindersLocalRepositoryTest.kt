package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.jupiter.api.Assertions.*

internal class RemindersLocalRepositoryTest{

    private val reminder1 = ReminderDTO(
        "Reminder1", "Description1",
        "Location", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id1"
    )
    private val reminder2 = ReminderDTO(
        "Reminder2", "Description2",
        "Location2", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id2"
    )
    private val reminder3 = ReminderDTO(
        "Reminder3", "Description3",
        "Location3", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id3"
    )
    private val remoteTasks = listOf(reminder1, reminder2).sortedBy { it.id }
    private val localTasks = listOf(reminder3).sortedBy { it.id }
    private val newTasks = listOf(reminder3).sortedBy { it.id }
}