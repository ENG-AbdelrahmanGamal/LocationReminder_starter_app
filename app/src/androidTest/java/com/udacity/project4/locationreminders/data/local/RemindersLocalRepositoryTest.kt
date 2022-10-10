package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.*
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
internal class RemindersLocalRepositoryTest {
    // Class under test
    private lateinit var database: RemindersDatabase
    private lateinit var remindersDAO: RemindersDao
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        remindersDAO = database.reminderDao()
        repository = RemindersLocalRepository(
                remindersDAO
            )
       // database=RemindersDatabase(database.reminderDao(),Dispatchers.Main)
    }
   // cleaning up your database using database.close()
    @After
    fun closeDb() = database.close()

    //there a few Reminders example insertion to the database, Get all the reminders from the database,  loaded data equal to  the expected values.
    @Test
    fun insertThreeReminders_getAllThreeFromDatabase() = runBlocking {
        // GIVEN - create 3 reminders content all  field for the Reminder Object to the database
        val reminder_1 = ReminderDTO("title1", "description1", "location1",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble(),
            "id1"
        )
        val reminder_2 = ReminderDTO(
            "title2",
            "description2",
            "location2",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble(),
            "id2"
        )
        val reminder_3 = ReminderDTO(
            "title3",
            "description3",
            "location3",
            (-360..360).random().toDouble(),
            (-360..360).random().toDouble(),
            "id3"
        )
        database.reminderDao().saveReminder(reminder_1)
        database.reminderDao().saveReminder(reminder_2)
        database.reminderDao().saveReminder(reminder_3)
        val remindersList = listOf(reminder_1, reminder_2, reminder_3).sortedBy { it.id }

        // WHEN - Get all the reminders from the database
        val loadedRemindersList = database.reminderDao().getReminders()
        val sortedLoadedRemindersList = loadedRemindersList.sortedBy { it.id }
        val reminder = repository.getReminder("fake") as Result.Error

        // THEN - same reminder returned( expected values)
        assertThat(reminder.message, CoreMatchers.`is`("Reminder not found!"))
        assertThat(sortedLoadedRemindersList[0].id, CoreMatchers.`is`(remindersList[0].id))
        assertThat(sortedLoadedRemindersList[0].title, CoreMatchers.`is`(remindersList[0].title))
        assertThat(sortedLoadedRemindersList[0].location, CoreMatchers.`is`(remindersList[0].location))
        assertThat(sortedLoadedRemindersList[0].description, CoreMatchers.`is`(remindersList[0].description))

        assertThat(sortedLoadedRemindersList[1].id, CoreMatchers.`is`(remindersList[1].id))
        assertThat(sortedLoadedRemindersList[1].title, CoreMatchers.`is`(remindersList[1].title))
        assertThat(sortedLoadedRemindersList[1].location, CoreMatchers.`is`(remindersList[1].location))
        assertThat(sortedLoadedRemindersList[1].description, CoreMatchers.`is`(remindersList[1].description))

        assertThat(sortedLoadedRemindersList[2].id, CoreMatchers.`is`(remindersList[2].id))
        assertThat(sortedLoadedRemindersList[2].title, CoreMatchers.`is`(remindersList[2].title))
        assertThat(sortedLoadedRemindersList[2].location, CoreMatchers.`is`(remindersList[2].location))
        assertThat(sortedLoadedRemindersList[2].description, CoreMatchers.`is`(remindersList[2].description))

    }
}