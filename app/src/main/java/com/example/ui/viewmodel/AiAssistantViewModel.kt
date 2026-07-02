package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiMessage(
    val id: String,
    val text: String,
    val sender: String, // "USER" or "AI"
    val timestamp: Long = System.currentTimeMillis()
)

class AiAssistantViewModel : ViewModel() {

    private val geminiService = GeminiService()

    private val _messages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                id = "welcome",
                text = "Hello! I am your EduSphere AI Companion. How can I assist you with your learning goals today?",
                sender = "AI"
            )
        )
    )
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = AiMessage(id = java.util.UUID.randomUUID().toString(), text = text, sender = "USER")
        _messages.value = _messages.value + userMsg

        _isThinking.value = true

        viewModelScope.launch {
            val systemInstruction = """
                You are EduSphere AI, a brilliant, professional, and patient academic tutor and education ERP assistant. 
                Your purpose is to assist students with their coursework (explaining topics, solving mathematics step-by-step, summarizing lecture slides, formulating 5-day study plans, suggesting library materials) and help teachers compile lesson plans, lecture ideas, or draft quiz structures. 
                Keep your voice encouraging, scholarly, and organized. Use markdown tables, bold key headers, and latex equations when appropriate.
            """.trimIndent()

            val aiResponse = geminiService.generateContent(text, systemInstruction)
            
            val aiMsg = AiMessage(id = java.util.UUID.randomUUID().toString(), text = aiResponse, sender = "AI")
            _messages.value = _messages.value + aiMsg
            _isThinking.value = false
        }
    }

    fun triggerQuickAction(actionType: String, contextDetail: String = "") {
        val prompt = when (actionType) {
            "EXPLAIN_TOPIC" -> "Explain the academic concept of \"$contextDetail\" clearly with core principles, key characteristics, and real-world examples."
            "STUDY_PLAN" -> "Help me design a detailed 5-day study plan with specific daily goals, visual recall metrics, and focus guidelines for: $contextDetail."
            "SOLVE_MATH" -> "Please solve this mathematical/computational equation step-by-step, explaining each analytical theorem: $contextDetail."
            "LESSON_PLAN" -> "Generate a comprehensive 60-minute lesson plan for a university lecture on \"$contextDetail\", detailing timelines, visual aid suggestions, and an active lab exercise."
            "SUMMARIZE_NOTES" -> "Summarize the major highlights and take-home definitions from this course notes topic: $contextDetail."
            "GENERATE_QUIZ" -> "Draft 3 review quiz questions (including MCQs and Short Answers) to test comprehension on: $contextDetail."
            else -> "Give me a brief tip on how to succeed in academic planning."
        }
        sendMessage(prompt)
    }

    fun clearHistory() {
        _messages.value = listOf(
            AiMessage(
                id = "welcome",
                text = "History cleared. I am ready for your next educational inquiry! What topic should we master?",
                sender = "AI"
            )
        )
    }
}
