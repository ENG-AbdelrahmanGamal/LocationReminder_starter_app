package com.udacity.project4.data.Source

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


class FakeDataSource (val reminders: MutableList<ReminderDTO>? = mutableListOf()):ReminderDataSource{
    private var _returnError = false
    fun returnError(result: Boolean){
        _returnError = result

    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //   TODO("Not yet implemented"
        if (_returnError) {
            return Result.Error("There is Exception Error" )
        }
            reminders?.let { return@let Result.Success(it.toList()) }
            return Result.Success(emptyList<ReminderDTO>())

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