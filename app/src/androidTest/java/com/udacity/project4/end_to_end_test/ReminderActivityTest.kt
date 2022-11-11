package com.udacity.project4.end_to_end_test

import android.app.Activity
import android.app.Application
import android.view.Display
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
//marked Large Test because this is end to end test
// test so much of our app they're consider large tests
@LargeTest
class RemenderActivityTest : KoinTest {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

//    @get:Rule
//    val activityRule = ActivityTestRule(RemindersActivity::class.java)

    //by registering these two resources when either of these
    // two resources is busy, esspresso will wait until they are idle
    // before moving to the next command
    @Before
    fun registerIdlingResourse() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)

    }

    @After
    fun unRegisterIdlingResourse() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

    }

    @Before
    fun init() {
        //stop the original app koin
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //initialize a new koin module
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
        //Get repository
        repository = get()

        //clear all data
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Test
    fun remindersScreen_gotoClickFloatingActionbar_navigateToSaveReminderScreen() = runBlocking {
        // start the reminders screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // click on the add reminder button
        Espresso.onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())

        // check that we are on the SaveReminder screen
        Espresso.onView(ViewMatchers.withId(R.id.reminderTitle))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun showSnackbar_enterLocation() = runBlocking {
        // GIVEN - Launch reminder activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        //WHEN - click on btn then enter details of reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("title"), closeSoftKeyboard())
        onView(withId(R.id.saveReminder)).perform(click())


        //THEN >>> expect value Snackbar display when add reminder
        Espresso.onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
        activityScenario.close()

    }

    // this method to test a Toast in the code, I had a problem
    // because run code on emulator
    // API greater than 30 in addition to i was use keyword matcher not matches and missed use keyword not
    // i believe this was issue when I submitted the project and this method not passed this method
    @ExperimentalCoroutinesApi
    @Test
    fun saveLocation_showToast() = runBlocking {
        // GIVEN - Launch reminder activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        //WHEN - when click on button then enter details of reminder like title and description
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("TITLE1"), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(typeText("DESC1"), closeSoftKeyboard())

        // click on location then click on button save  without click on map to select any area
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.save_current_Location)).perform(click())

        //THEN - we expect that Toast will appear after click on button  save location.
        onView(withText(R.string.select_poi)).
        inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView))))
            .check(matches(isDisplayed()))
        activityScenario.close()

    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }



    @Test
    fun addreminderfragment_doubleUpfloatingActionBar() = runBlocking {
        val task = ReminderDTO(
            "title", "description", "location",
            (-360..360).random().toDouble(), (-360..360).random().toDouble(), "id"
        )
        repository.saveReminder(task)

        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Click on the task on the list.

        // 2. Click on the edit task button.

        // 3. Confirm that if we click Up button once, we end up back at the task details page.

        // 4. Confirm that if we click Up button a second time, we end up back at the home screen.

        // When using ActivityScenario.launch(), always call close().
        activityScenario.close()
    }

}


