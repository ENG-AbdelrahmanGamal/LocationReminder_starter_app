package com.udacity.project4.data.Source

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.withContext


class FakeDataSource (val reminders: MutableList<ReminderDTO>? = mutableListOf()):ReminderDataSource{
    private var _returnError = false
    fun returnError(result: Boolean){
        _returnError = result

    }
    /**
     * Get the reminders list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //   TODO("Not yet implemented"

        // make datasource return error even if it's not empty to test error.
        if (_returnError) {
            return Result.Error("There is Exception Error!")
        }

        // when no reminders are found, returns >>> an empty list and the data source >>> returns Result.success
        if (reminders?.isEmpty()!!) {
            return Result.Success(reminders!!)
        } else {
            return Result.Success(reminders!!)
        }
    }
    override suspend fun saveReminder(reminder: ReminderDTO) {
 //       TODO("Not yet implemented")
        reminders?.add(reminder)
    }
    override suspend fun getReminder(id: String): Result<ReminderDTO> {
      //  TODO("Not yet implemented")
        if(_returnError) {
            return Result.Error("There is Exception Error ")
        }
        reminders?.firstOrNull { it.id == id }?.let { return Result.Success(it) }
        return Result.Error("Not Found The Reminder Have Id = $id")
    }

    override suspend fun deleteAllReminders() {
     //   TODO("Not yet implemented")
        reminders?.clear()
    }
}