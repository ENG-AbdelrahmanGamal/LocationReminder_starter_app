package com.udacity.project4.end_to_end_test

import android.app.Activity
import android.app.Application
import androidx.appcompat.widget.ResourceManagerInternal.get
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.tasks.Task
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
//marked Large Test because this is end to end test
// test so much of our app they're consider large tests
@LargeTest
class RemenderActivityTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()


    //by registering these two resources when either of these two resources is busy, esspresso will wait until they are idle before moving to the next command
    @Before
    fun registerIdlingResourse(){
    IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
            IdlingRegistry.getInstance().register(dataBindingIdlingResource)

    }
    @After
    fun  unRegisterIdlingResourse(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)

    }
    @Before
    fun init() {
        stopKoin() //stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
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
    fun addreminderfragment_doubleUpfloatingActionBar() = runBlocking {
        val task = ReminderDTO("title","description","location",
            (-360..360).random().toDouble(),  (-360..360).random().toDouble(),"id")
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
    @ExperimentalCoroutinesApi
    @Test
    fun saveLocation_showToast_withDifferentSituation() = runBlocking {

        // GIVEN - Launch reminder activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        //WHEN - click on btn then enter details of reminder
        Espresso.onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.reminderTitle))
            .perform(ViewActions.typeText("test title"), ViewActions.closeSoftKeyboard())
        Espresso.onView(withId(R.id.reminderDescription))
            .perform(ViewActions.typeText("test description"), ViewActions.closeSoftKeyboard())
        // click on location then save btn without click on map
        Espresso.onView(withId(R.id.selectLocation)).perform(ViewActions.click())
        Espresso.onView(withId(R.id.saveReminder)).perform(ViewActions.click())

        //THEN - we expect that Toast will appear when click on save btn location
        Espresso.onView(ViewMatchers.withText(R.string.select_poi)).
        inRoot(
            RootMatchers.withDecorView(
                CoreMatchers.not(
                    CoreMatchers.`is`(
                        getActivity(
                            activityScenario
                        )?.window?.decorView
                    )
                )
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        activityScenario.close()
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>?): Activity? {
        var activity: Activity? = null
        activityScenario?.onActivity {
            activity = it
        }
        return activity
    }

}   fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.radio).navigationContentDescription as String
    }
    return description
}