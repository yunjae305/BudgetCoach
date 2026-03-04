package com.budgetcoach.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetcoach.data.ai.GeminiService
import com.budgetcoach.data.repository.BudgetRepository
import com.budgetcoach.data.repository.ExpenseRepository
import com.budgetcoach.domain.usecase.BudgetCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AiChatViewModel(
    private val geminiService: GeminiService,
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetCalculator: BudgetCalculator
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                "안녕하세요! 💰 저는 AI 재정 코치입니다.\n예산 관리에 대한 질문이나 조언이 필요하시면 말씀해주세요!",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isAiAvailable: Boolean get() = geminiService.isAvailable

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val userMsg = ChatMessage(userMessage, isUser = true)
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val context = buildBudgetContext()
                val response = geminiService.chat(userMessage, context)
                val aiMsg = ChatMessage(response, isUser = false)
                _messages.value = _messages.value + aiMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage("⚠️ 오류가 발생했어요: ${e.message}", isUser = false)
                _messages.value = _messages.value + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun quickAction(action: String) {
        when (action) {
            "advice" -> sendMessage("오늘 내 재정 상태에 맞춰서 따뜻한 조언 한마디 해줘.")
            "weekly" -> sendMessage("이번 주 지출 내역을 분석해서 칭찬이나 주의할 점을 알려줘.")
            "tips" -> sendMessage("지금 내 소비 습관에서 돈을 더 아낄 수 있는 꿀팁 3가지만 알려줘.")
        }
    }

    private suspend fun buildBudgetContext(): String {
        val yearMonthStr = budgetCalculator.getCurrentYearMonth()
        val now = YearMonth.now()
        val startOfMonth = now.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val totalSpent = expenseRepository.getTotalByDateRangeSync(startOfMonth, endOfMonth) ?: 0L
        val budgetEntity = budgetRepository.getByYearMonthSync(yearMonthStr)
        val totalBudget = budgetEntity?.totalBudget ?: 0L
        
        val categoryTotals = expenseRepository.getCategoryTotalsSync(startOfMonth, endOfMonth)
        val categoryContext = categoryTotals.joinToString("\n") { 
            "- ${it.category}: ₩${String.format("%,d", it.total)}"
        }

        val dailyInfo = budgetCalculator.calculateDailyBudget(totalBudget, totalSpent, LocalDate.now())

        return """
[현재 재정 요약]
- 이번 달 예산: ₩${String.format("%,d", totalBudget)}
- 현재까지 지출: ₩${String.format("%,d", totalSpent)}
- 남은 예산: ₩${String.format("%,d", dailyInfo.remainingBudget)}
- 남은 일수: ${dailyInfo.remainingDays}일
- 하루 권장 지출액: ₩${String.format("%,d", dailyInfo.dailyRecommended)}

[카테고리별 지출 현황]
$categoryContext
""".trimIndent()
    }
}
