package com.udacity.project4.locationreminders.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase
    private val reminder1 = ReminderDTO(
        "Reminder1", "Description1",
        "Location1", (-360..360).random().toDouble(),
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
    private val reminder4 = ReminderDTO(
        "Reminder4", "Description4",
        "Location4", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id4"
    )

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

    }

    @After
    fun closeDb() = database.close()

    // test getReminders
    @Test
    fun insertReminderAndGetAll() = runBlockingTest {
        // GIVEN - Insert  reminders.
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)
        database.reminderDao().saveReminder(reminder4)

        // WHEN - Get the reminders from database
        val reminders = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat(reminders[0].id, `is`(reminder1.id))
        assertThat(reminders[0].title, `is`(reminder1.title))
        assertThat(reminders[0].latitude, `is`(reminder1.latitude))
        assertThat(reminders[0].description, `is`(reminder1.description))

        assertThat(reminders[1].id, `is`(reminder2.id))
        assertThat(reminders[1].title, `is`(reminder2.title))
        assertThat(reminders[1].latitude, `is`(reminder2.latitude))
        assertThat(reminders[1].description, `is`(reminder2.description))

        assertThat(reminders[2].id, `is`(reminder3.id))
        assertThat(reminders[2].title, `is`(reminder3.title))
        assertThat(reminders[2].latitude, `is`(reminder3.latitude))
        assertThat(reminders[2].description, `is`(reminder3.description))

        assertThat(reminders[3].id, `is`(reminder4.id))
        assertThat(reminders[3].title, `is`(reminder4.title))
        assertThat(reminders[3].latitude, `is`(reminder4.latitude))
        assertThat(reminders[3].description, `is`(reminder4.description))

        //check for the size
        assertThat(reminders.size, `is`(4))

    }

    // test getReminderById
    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        database.reminderDao().saveReminder(reminder1)
        // WHEN - Get the task by id from the database.
        val reminder = database.reminderDao().getReminderById(reminder1.id)

        // THEN - the result is expected date.
        assertThat<ReminderDTO>(reminder as ReminderDTO, notNullValue())
        assertThat(reminder.id, `is`(reminder1.id))
        assertThat(reminder.title, `is`(reminder1.title))
        assertThat(reminder.description, `is`(reminder1.description))
        assertThat(reminder.longitude, `is`(reminder1.longitude))
        assertThat(reminder.latitude, `is`(reminder1.latitude))
        assertThat(reminder.location, `is`(reminder1.location))

    }

    @Test
    fun SaveReminder_ThenDeleteAllOfThem() = runBlockingTest {
        // GIVEN - Save  reminders.
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)
        database.reminderDao().saveReminder(reminder4)


        // WHEN - Delete all of reminder
        database.reminderDao().deleteAllReminders()


        // THEN - the expected the size should be equal Zero
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.size, `is`(0))
    }

    @Test
    fun getReminder_returnError() = runBlocking {
        //GIVEN - start with data base is empty
        database.reminderDao().deleteAllReminders()
        //WHEN - get reminder1
        val value = database.reminderDao().getReminderById(reminder1.id)
        // THEN - check value equal null
        assertThat(value, `is`(nullValue()))
    }

}
