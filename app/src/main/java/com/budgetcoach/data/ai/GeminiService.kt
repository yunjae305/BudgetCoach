package com.budgetcoach.data.ai

import com.budgetcoach.BuildConfig
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {

    // 1. API 키 설정 (local.properties의 GEMINI_API_KEY 사용)
    private val apiKey = BuildConfig.GEMINI_API_KEY

    // 2. 모델 설정 (Gemini 2.5 Flash 적용)
    // 사용자의 확인에 따라 Gemini 2.5 Flash 모델로 설정합니다.
    private val model: GenerativeModel? = if (apiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "models/gemini-2.5-flash", 
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
            },
            systemInstruction = content {
                text("""
                    당신은 '코치'라는 이름의 AI 재정 전략가입니다.
                    사용자의 가계부 데이터를 기반으로 재정 조언을 제공합니다.
                    금액은 원(₩) 단위로 표시하고, 답변은 간결하게(3-4문장), 이모지를 적절히 활용해주세요.
                    한국어로만 답변하세요.
                """.trimIndent())
            }
        )
    } else null

    val isAvailable: Boolean get() = model != null && apiKey.isNotBlank()

    // 3. 채팅 세션 관리
    private var chat: Chat? = null

    private fun initChatSession() {
        if (chat == null && model != null) {
            chat = model.startChat()
        }
    }

    suspend fun chat(
        userMessage: String,
        budgetContext: String
    ): String = withContext(Dispatchers.IO) {
        if (!isAvailable) {
            return@withContext "⚠️ Gemini API 키가 설정되지 않았거나 유효하지 않습니다."
        }

        try {
            initChatSession()

            val fullPrompt = """
                [현재 가계부 상황]
                $budgetContext
                
                [사용자 질문/요청]
                $userMessage
            """.trimIndent()

            val response = chat?.sendMessage(fullPrompt)
            response?.text ?: "코치가 응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요."
        } catch (e: Exception) {
            val errorMsg = e.message ?: ""
            if (errorMsg.contains("404")) {
                "⚠️ 모델 인식 오류(404): 'gemini-2.5-flash' 모델을 찾을 수 없습니다. API 키의 권한이나 모델명을 다시 확인해 주세요."
            } else {
                "⚠️ AI 응답 오류: ${e.localizedMessage}"
            }
        }
    }

    suspend fun reportExpense(itemName: String, amount: Long, monthlyTotal: Long, remainingBudget: Long): String {
        val context = "이번 달 총 지출: ₩${String.format("%,d", monthlyTotal)}\n남은 예산: ₩${String.format("%,d", remainingBudget)}"
        return chat("방금 '$itemName'에 ₩${String.format("%,d", amount)}을 지출했어. 내 예산 상태에 대해 조언해줘.", context)
    }

    suspend fun getAdvice(remainingBudget: Long, remainingDays: Int, dailyRecommended: Long, totalSpent: Long): String {
        val context = """
            남은 예산: ₩${String.format("%,d", remainingBudget)}
            남은 일수: ${remainingDays}일
            하루 권장 지출액: ₩${String.format("%,d", dailyRecommended)}
            이번 달 총 지출: ₩${String.format("%,d", totalSpent)}
        """.trimIndent()
        return chat("현재 재정 상황을 분석해서 오늘 하루 어떻게 보내야 할지 조언해줘.", context)
    }
}
