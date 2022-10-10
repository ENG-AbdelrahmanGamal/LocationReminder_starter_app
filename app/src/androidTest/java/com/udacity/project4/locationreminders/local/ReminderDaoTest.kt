package com.udacity.project4.locationreminders.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.runner.RunWith
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO pretty Much synonymous with this small test
@SmallTest
class ReminderDaoTest {

//because we're testing architecture components
     @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    //making a closed database
    @After
    fun closeDb() = database.close()


    // using runblocking because DAO use suspend function
    @Test
    fun getReminders() = runBlockingTest {
        // GIVEN >>>> insert a reminder
        val reminder = ReminderDTO("title", "description", "location",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble()
        )
        database.reminderDao().saveReminder(reminder)

        // WHEN >>>> Get reminders from the database
        val reminders = database.reminderDao().getReminders()

        // THEN >>>> EXPECTED VALUES
        Assert.assertThat(reminders.size, CoreMatchers.`is`(1))
        Assert.assertThat(reminders[0].id, CoreMatchers.`is`(reminder.id))
        Assert.assertThat(reminders[0].title, CoreMatchers.`is`(reminder.title))
        Assert.assertThat(reminders[0].description, CoreMatchers.`is`(reminder.description))
        Assert.assertThat(reminders[0].location, CoreMatchers.`is`(reminder.location))
        Assert.assertThat(reminders[0].latitude, CoreMatchers.`is`(reminder.latitude))
        Assert.assertThat(reminders[0].longitude, CoreMatchers.`is`(reminder.longitude))

    }
    @Test
    fun getReminderdDataBase_whenId_NotFound() = runBlockingTest {
        // GIVEN - a random reminder id
        val reminderId = UUID.randomUUID().toString()
        // WHEN - Get the reminder from the database where id =reminderId
        val loaded = database.reminderDao().getReminderById(reminderId)
        // THEN - The loaded data should be  null.
        Assert.assertNull(loaded)
    }


    @Test
    fun deleteReminders() = runBlockingTest {
        // Given - reminders inserted
        val remindersList = listOf<ReminderDTO>(ReminderDTO("title", "description","location",(-360..360).random().toDouble(),(-360..360).random().toDouble(),"id"),
            ReminderDTO("title", "description","location",(-360..360).random().toDouble(),(-360..360).random().toDouble(),"id"),
            ReminderDTO("title", "description","location",(-360..360).random().toDouble(),(-360..360).random().toDouble(),"id"),
            ReminderDTO("title", "description","location",(-360..360).random().toDouble(),(-360..360).random().toDouble(),"id"))

        remindersList.forEach {
            database.reminderDao().saveReminder(it)
        }

        // WHEN - Clear reminders
        database.reminderDao().deleteAllReminders()

        // THEN -the expected values is empty reminder
        val reminders = database.reminderDao().getReminders()
        Assert.assertThat(reminders.isEmpty(), CoreMatchers.`is`(true))
    }

}