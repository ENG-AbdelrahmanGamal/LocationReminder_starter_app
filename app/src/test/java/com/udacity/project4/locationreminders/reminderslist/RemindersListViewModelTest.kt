package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.Source.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Suppress("DEPRECATION")
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
internal class RemindersListViewModelTest{
    private lateinit var reminderListViewModel: RemindersListViewModel
    // use fakeData Source to be injected in view model
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder = ReminderDTO(
        "Reminder", "Description",
        "Location", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id"
    )
      // executes each  task synchronously using architecture components
    @get:Rule
    var instantExectuterRule = InstantTaskExecutorRule()
    // set main coroutine dispatchers for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext()
            ,fakeDataSource)
    }
    @After
    fun clearDataSource() = runBlockingTest{
        fakeDataSource.deleteAllReminders()
    }
    @Test
    fun invalidateShowNoData_showNoData_isTrue() = runBlockingTest{
        //GIVEN - delete all reminders
        fakeDataSource.deleteAllReminders()

        //WHEN -  load Reminders
        reminderListViewModel.loadReminders()

        //THEN - checked if reminder size empty and show no data
        MatcherAssert.assertThat(reminderListViewModel.remindersList.getOrAwaitValue().size, CoreMatchers.`is` (0))
        MatcherAssert.assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), CoreMatchers.`is` (true))
    }
    @Test
    fun loadReminders_checkLoading()= mainCoroutineRule.runBlockingTest{
        // Pause dispatcher to verify initial values
        mainCoroutineRule.pauseDispatcher()
        //GIVEN -  Save reminder
        fakeDataSource.deleteAllReminders()
        fakeDataSource.saveReminder(reminder)

        //WHEN - load Reminders
        reminderListViewModel.loadReminders()
        //THEN - loading indicator is shown
        MatcherAssert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()
        // THEN - loading indicator is hidden
        MatcherAssert.assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )

    }

}