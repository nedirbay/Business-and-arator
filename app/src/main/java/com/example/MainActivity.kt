package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LessonScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LearningViewModel

enum class NavigationTab {
    DASHBOARD,
    HISTORY,
    PROFILE
}

class MainActivity : ComponentActivity() {
    private val viewModel: LearningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge drawing
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentLesson by viewModel.currentLesson.collectAsState()
                val isLoadingLesson by viewModel.isLoadingLesson.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                
                // Active Quiz state flows
                val activeQuestionIndex by viewModel.activeQuestionIndex.collectAsState()
                val selectedOptionIndex by viewModel.selectedOptionIndex.collectAsState()
                val isAnswerSubmitted by viewModel.isAnswerSubmitted.collectAsState()
                val quizScore by viewModel.quizScore.collectAsState()
                val isQuizFinished by viewModel.isQuizFinished.collectAsState()

                // Profile and History database flows
                val userProfile by viewModel.userProfile.collectAsState()
                val completedLessons by viewModel.completedLessons.collectAsState()

                var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
                var selectedCategoryForLesson by remember { mutableStateOf("") }

                if (currentLesson != null || isLoadingLesson || errorMessage != null) {
                    // Immersive Full Screen Lesson View (hides the bottom bar)
                    LessonScreen(
                        category = selectedCategoryForLesson,
                        lesson = currentLesson,
                        isLoading = isLoadingLesson,
                        errorMessage = errorMessage,
                        activeQuestionIndex = activeQuestionIndex,
                        selectedOptionIndex = selectedOptionIndex,
                        isAnswerSubmitted = isAnswerSubmitted,
                        quizScore = quizScore,
                        isQuizFinished = isQuizFinished,
                        onSelectOption = { viewModel.selectQuizOption(it) },
                        onSubmitAnswer = { viewModel.submitAnswer(it) },
                        onNextQuestion = { viewModel.nextQuestion(it) },
                        onCompleteLesson = { viewModel.completeLesson(selectedCategoryForLesson) },
                        onBack = { viewModel.exitLesson() }
                    )
                } else {
                    // Standard Scaffolding with Bottom Navigation
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.testTag("bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.DASHBOARD,
                                    onClick = { currentTab = NavigationTab.DASHBOARD },
                                    icon = { Icon(Icons.Default.School, contentDescription = "Sapaklar") },
                                    label = { Text("Sapaklar") },
                                    modifier = Modifier.testTag("tab_dashboard")
                                )
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.HISTORY,
                                    onClick = { currentTab = NavigationTab.HISTORY },
                                    icon = { Icon(Icons.Default.History, contentDescription = "Geçilenler") },
                                    label = { Text("Geçilenler") },
                                    modifier = Modifier.testTag("tab_history")
                                )
                                NavigationBarItem(
                                    selected = currentTab == NavigationTab.PROFILE,
                                    onClick = { currentTab = NavigationTab.PROFILE },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                                    label = { Text("Profil") },
                                    modifier = Modifier.testTag("tab_profile")
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        when (currentTab) {
                            NavigationTab.DASHBOARD -> {
                                DashboardScreen(
                                    profile = userProfile,
                                    recentLessons = completedLessons,
                                    onStartLesson = { category ->
                                        selectedCategoryForLesson = category
                                        viewModel.startNewLesson(category)
                                    },
                                    onViewHistory = { currentTab = NavigationTab.HISTORY },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            NavigationTab.HISTORY -> {
                                HistoryScreen(
                                    completedLessons = completedLessons,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            NavigationTab.PROFILE -> {
                                ProfileScreen(
                                    profile = userProfile,
                                    onResetProgress = { viewModel.clearProgress() },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
