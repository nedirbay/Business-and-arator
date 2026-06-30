package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LearningViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.lessonDao()
    private val geminiRepository = GeminiRepository()

    // Reactive database flows
    val userProfile: StateFlow<UserProfile?> = dao.getUserProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val completedLessons: StateFlow<List<CompletedLesson>> = dao.getAllCompletedLessons()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active lesson generation states
    private val _currentLesson = MutableStateFlow<LessonJson?>(null)
    val currentLesson: StateFlow<LessonJson?> = _currentLesson.asStateFlow()

    private val _isLoadingLesson = MutableStateFlow(false)
    val isLoadingLesson: StateFlow<Boolean> = _isLoadingLesson.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Interactive Quiz State Machine
    private val _activeQuestionIndex = MutableStateFlow(0)
    val activeQuestionIndex: StateFlow<Int> = _activeQuestionIndex.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _isAnswerSubmitted = MutableStateFlow(false)
    val isAnswerSubmitted: StateFlow<Boolean> = _isAnswerSubmitted.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    // Storing user's selected answers for review [QuestionIndex -> SelectedOption]
    val userAnswers = mutableStateMapOf<Int, Int>()

    init {
        // Initialize default user profile if it doesn't exist
        viewModelScope.launch {
            val profile = dao.getUserProfileDirect()
            if (profile == null) {
                dao.insertUserProfile(UserProfile())
            }
        }
    }

    /**
     * Generates a new lesson for the selected category using Gemini.
     * Checks database history first to prevent topic repeating.
     */
    fun startNewLesson(category: String) {
        viewModelScope.launch {
            _isLoadingLesson.value = true
            _errorMessage.value = null
            _currentLesson.value = null
            
            // Reset Quiz State
            resetQuizState()

            try {
                // Get list of completed lesson titles in this category to avoid repeating
                val completedTitles = dao.getCompletedTitlesByCategory(category)
                
                // Fetch from Gemini (or fallback if key is missing/error occurs)
                val lesson = geminiRepository.generateLesson(category, completedTitles)
                _currentLesson.value = lesson
            } catch (e: Exception) {
                _errorMessage.value = "Sapak ýüklemekde ýalňyşlyk ýüz berdi: ${e.localizedMessage}. Gaýtadan synanyşyň."
            } finally {
                _isLoadingLesson.value = false
            }
        }
    }

    private fun resetQuizState() {
        _activeQuestionIndex.value = 0
        _selectedOptionIndex.value = null
        _isAnswerSubmitted.value = false
        _quizScore.value = 0
        _isQuizFinished.value = false
        userAnswers.clear()
    }

    /**
     * Handles option selection in active quiz
     */
    fun selectQuizOption(optionIndex: Int) {
        if (!_isAnswerSubmitted.value) {
            _selectedOptionIndex.value = optionIndex
        }
    }

    /**
     * Submits answer for the current quiz question and records score
     */
    fun submitAnswer(correctOptionIndex: Int) {
        val selected = _selectedOptionIndex.value ?: return
        _isAnswerSubmitted.value = true
        userAnswers[_activeQuestionIndex.value] = selected
        
        if (selected == correctOptionIndex) {
            _quizScore.value += 1
        }
    }

    /**
     * Proceeds to next quiz question or finishes quiz
     */
    fun nextQuestion(totalQuestions: Int) {
        if (_activeQuestionIndex.value + 1 < totalQuestions) {
            _activeQuestionIndex.value += 1
            _selectedOptionIndex.value = null
            _isAnswerSubmitted.value = false
        } else {
            _isQuizFinished.value = true
        }
    }

    /**
     * Completes active lesson, saves details into Room database,
     * awards XP, and updates the user's daily learning streak.
     */
    fun completeLesson(category: String) {
        val lesson = _currentLesson.value ?: return
        val score = _quizScore.value
        val total = lesson.quiz.size

        viewModelScope.launch {
            // Save completed lesson entry
            val completed = CompletedLesson(
                category = category,
                title = lesson.title,
                introduction = lesson.introduction,
                coreConceptsJson = RetrofitClient.moshi.adapter(List::class.java).toJson(lesson.coreConcepts),
                lessonText = lesson.lessonText,
                practicalAssignment = lesson.practicalAssignment,
                quizJson = RetrofitClient.moshi.adapter(List::class.java).toJson(lesson.quiz),
                quizScore = score,
                quizTotal = total
            )
            dao.insertCompletedLesson(completed)

            // Fetch and update user profile, streaks, and experience points (XP)
            val currentProfile = dao.getUserProfileDirect() ?: UserProfile()
            
            // Streak computation using day-indices
            val currentMs = System.currentTimeMillis()
            val oneDayMs = 1000L * 60L * 60L * 24L
            val currentDayIndex = currentMs / oneDayMs
            val lastActiveDayIndex = currentProfile.lastActiveDate / oneDayMs

            val newStreak = when {
                currentProfile.lastActiveDate == 0L -> 1 // First time ever
                lastActiveDayIndex == currentDayIndex -> currentProfile.streak // Already active today
                lastActiveDayIndex == currentDayIndex - 1 -> currentProfile.streak + 1 // Active consecutive day
                else -> 1 // Streak broken, restart
            }

            // XP calculations: 50 XP base for reading, +20 XP per correct quiz answer
            val xpReward = 50 + (score * 20)

            val updatedProfile = currentProfile.copy(
                streak = newStreak,
                lastActiveDate = currentMs,
                xp = currentProfile.xp + xpReward,
                completedLessonsCount = currentProfile.completedLessonsCount + 1,
                sozleyisProgressCount = currentProfile.sozleyisProgressCount + if (category == "Sözleýiş Medeniýeti") 1 else 0,
                tasirProgressCount = currentProfile.tasirProgressCount + if (category == "Adamlara Täsir Ýetirmek") 1 else 0,
                biznesProgressCount = currentProfile.biznesProgressCount + if (category == "Biznes Strategiýalary") 1 else 0
            )

            dao.insertUserProfile(updatedProfile)
            
            // Clear current active lesson to return to dashboard
            _currentLesson.value = null
            resetQuizState()
        }
    }

    /**
     * Clears all progress (convenient for testing or resetting user data)
     */
    fun clearProgress() {
        viewModelScope.launch {
            dao.clearAllProgress()
            dao.insertUserProfile(UserProfile())
            _currentLesson.value = null
            resetQuizState()
        }
    }

    /**
     * Cancels the active lesson and returns to dashboard
     */
    fun exitLesson() {
        _currentLesson.value = null
        _errorMessage.value = null
        resetQuizState()
    }
}
