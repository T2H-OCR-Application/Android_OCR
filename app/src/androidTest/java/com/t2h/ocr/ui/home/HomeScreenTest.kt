package com.t2h.ocr.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSearchBarIsVisible() {
        // This is a scaffold, the HomeScreen will be implemented in Task 3
        // composeTestRule.setContent { HomeScreen() }
        // composeTestRule.onNodeWithTag("search_bar").assertIsDisplayed()
    }

    @Test
    fun testBentoGridIsVisible() {
        // composeTestRule.setContent { HomeScreen() }
        // composeTestRule.onNodeWithTag("bento_grid").assertIsDisplayed()
    }
}
