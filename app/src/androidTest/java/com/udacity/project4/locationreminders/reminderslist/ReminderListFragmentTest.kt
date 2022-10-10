package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import kotlin.properties.ReadOnlyProperty


internal class ReminderListFragmentTest {
    private  lateinit var remindersRepository: ReminderDataSource
    private lateinit var appContext: Application

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        //stop koin
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()
        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
         //   single { FakeDataSource() as ReminderDataSource }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
    }
    @After
    fun cleanupDb() = runBlockingTest {
        remindersRepository.deleteAllReminders()
    }


//    @Test
//    fun reminderIsShownInRecyclerView() {
//        runBlocking {
//            // GIVEN - one reminder
//            val reminder_1 = ReminderDTO(
//                "title1",
//                "description1",
//                "somewhere1",
//                (-360..360).random().toDouble(),
//                (-360..360).random().toDouble(),
//                "id"
//            )
//            remindersRepository.saveReminder(reminder_1)
//
//            // WHEN - ReminderListFragment is displayed
//            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
//
//            // THEN - the reminder is displayed
//            Espresso.onView(ViewMatchers.withText(reminder_1.title))
//                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//            Espresso.onView(ViewMatchers.withText(reminder_1.description))
//                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//            Espresso.onView(ViewMatchers.withText(reminder_1.location))
//                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//        }
//    }


//    @Test
//    fun errorSnackBackShown() = runBlockingTest {
//        remindersRepository.deleteAllReminders()
//        // WHEN - Details fragment launched to display task
//        Espresso.onView(ViewMatchers.withText("No reminders found"))
//            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
//    }
    @Test
    fun clickTask_navigateToSaveReminderFragment() = runBlockingTest{
        //GIVEN - ReminderListFragment is displayed
        val scenario =  launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        //WHEN - Click on add reminder
        Espresso.onView((ViewMatchers.withId(R.id.addReminderFAB))).perform(ViewActions.click())
        //THEN - navigate to SaveReminderFragment
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }

}





