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

    // 2. 모델 설정
    private val model: GenerativeModel? = if (apiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
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

    val isAvailable: Boolean get() = model != null

    // 3. 채팅 세션 관리
    private var chat: Chat? = null

    private fun initChatSessionIfNeeded() {
        if (chat == null && model != null) {
            chat = model.startChat()
        }
    }

    suspend fun chat(
        userMessage: String,
        budgetContext: String
    ): String = withContext(Dispatchers.IO) {
        if (model == null || apiKey.isBlank()) {
            return@withContext "⚠️ Gemini API 키가 설정되지 않았어요. local.properties를 확인해주세요."
        }

        initChatSessionIfNeeded()

        val fullPrompt = """
            [현재 가계부 상황]
            ${budgetContext}
            
            [사용자 질문/요청]
            ${userMessage}
        """.trimIndent()

        try {
            val response = chat?.sendMessage(fullPrompt)
            response?.text ?: "응답을 받을 수 없습니다."
        } catch (e: Exception) {
            val errorMsg = e.message ?: ""
            // 404 오류 발생 시 models/ 접두사 명시하여 재시도 (일부 SDK 버전 호환성)
            if (errorMsg.contains("404") || errorMsg.contains("not found")) {
                try {
                    val retryModel = GenerativeModel(
                        modelName = "models/gemini-1.5-flash",
                        apiKey = apiKey,
                        generationConfig = generationConfig { temperature = 0.7f }
                    )
                    val retryChat = retryModel.startChat()
                    val retryResponse = retryChat.sendMessage(fullPrompt)
                    return@withContext retryResponse.text ?: "응답 실패"
                } catch (retryE: Exception) {
                    return@withContext "⚠️ 모델 인식 오류(404): ${retryE.message}"
                }
            }
            "⚠️ AI 응답 오류: ${e.message}"
        }
    }

    suspend fun reportExpense(
        itemName: String,
        amount: Long,
        monthlyTotal: Long,
        remainingBudget: Long
    ): String {
        val context = """
            이번 달 총 지출: ₩${String.format("%,d", monthlyTotal)}
            남은 예산: ₩${String.format("%,d", remainingBudget)}
        """.trimIndent()

        return chat(
            "방금 '$itemName'에 ₩${String.format("%,d", amount)}을 지출했어. 내 예산 상태에 대해 조언해줘.",
            context
        )
    }

    suspend fun getAdvice(
        remainingBudget: Long,
        remainingDays: Int,
        dailyRecommended: Long,
        totalSpent: Long
    ): String {
        val context = """
            남은 예산: ₩${String.format("%,d", remainingBudget)}
            남은 일수: ${remainingDays}일
            하루 권장 지출액: ₩${String.format("%,d", dailyRecommended)}
            이번 달 총 지출: ₩${String.format("%,d", totalSpent)}
        """.trimIndent()

        return chat("현재 재정 상황을 분석해서 오늘 하루 어떻게 보내야 할지 조언해줘.", context)
    }
}
