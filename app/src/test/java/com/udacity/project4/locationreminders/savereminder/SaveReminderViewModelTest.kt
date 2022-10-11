package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.Source.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
internal class SaveReminderViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private var remindersList = mutableListOf<ReminderDTO>()

    //TODO: provide testing to the SaveReminderView and its live data objects

    fun initRepository() {
        stopKoin()
        fakeDataSource = FakeDataSource(remindersList)
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)

    }
    @Before
    fun setUpViewModel(){
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),fakeDataSource)
    }

    @After
    fun stopDown() {
        stopKoin()
    }

    @Test
    fun reminder_saved() {
        initRepository()
        val reminder = ReminderDataItem(
            "Reminder", "Description",
            "Location", (-360..360).random().toDouble(),
            (-360..360).random().toDouble(), "id"
        )
        saveReminderViewModel.saveReminder(reminder)
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`("Reminder Saved !")
        )
    }

    @Test
    fun save_Reminder_expect_description() {
        initRepository()
        val reminder2 = ReminderDataItem(
            "title", "",
            "Location", (-360..360).random().toDouble(),
            (-360..360).random().toDouble(), "id"
        )
        saveReminderViewModel.validateAndSaveReminder(reminder2)
        MatcherAssert.assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            CoreMatchers.notNullValue()
        )
    }


    @Test
    fun validate_loading_LiveData() = runBlocking {
        //given reminder
        val reminder = ReminderDataItem(
            "Reminder", "",
            "Location", (-360..360).random().toDouble(),
            (-360..360).random().toDouble(), "id"
        )
        initRepository()
        mainCoroutineRule.pauseDispatcher()
        //when >>>> save reminder
        saveReminderViewModel.validateAndSaveReminder(reminder)

        //then >>>  expected loading  is true  display data of reminder
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )
        mainCoroutineRule.resumeDispatcher()
        //then >>>  expected loading  is false  disappeared
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )


    }

}