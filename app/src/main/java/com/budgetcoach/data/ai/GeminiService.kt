package com.budgetcoach.data.ai

import com.budgetcoach.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {

    private val apiKey = BuildConfig.GEMINI_API_KEY

    private val model: GenerativeModel? = if (apiKey.isNotBlank()) {
        GenerativeModel(
            modelName = "models/gemini-1.5-flash", // 모델 풀 네임 명시 (models/ 접두사 추가)
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 1024
            }
        )
    } else null

    val isAvailable: Boolean get() = model != null

    private val systemPrompt = """
당신은 '코치'라는 이름의 AI 재정 전략가입니다.
사용자의 가계부 데이터를 기반으로 재정 조언을 제공합니다.

역할:
- 지출 기록을 분석하고 피드백 제공
- 예산 내에서 생활할 수 있도록 현실적인 조언
- 긍정적이고 동기 부여적인 어투 사용
- 한국어로 답변

규칙:
- 금액은 원(₩) 단위로 표시
- 답변은 간결하게 (3-4문장)
- 이모지를 적절히 활용
""".trimIndent()

    suspend fun chat(
        userMessage: String,
        budgetContext: String
    ): String = withContext(Dispatchers.IO) {
        if (model == null || apiKey.isBlank()) {
            return@withContext "⚠️ Gemini API 키가 설정되지 않았어요.\nlocal.properties에 GEMINI_API_KEY를 추가해주세요."
        }

        try {
            val fullPrompt = """
$systemPrompt

[현재 사용자 재정 상태]
$budgetContext

[사용자 메시지]
$userMessage
""".trimIndent()

            val response = model.generateContent(fullPrompt)
            response.text ?: "응답을 생성할 수 없습니다."
        } catch (e: Exception) {
            // 상세한 오류 메시지 확인을 위해 e.message 출력
            "⚠️ AI 응답 오류: ${e.message ?: "알 수 없는 오류가 발생했습니다."}"
        }
    }

    suspend fun reportExpense(
        itemName: String,
        amount: Long,
        monthlyTotal: Long,
        remainingBudget: Long
    ): String {
        val context = """
월 누적 지출: ₩${String.format("%,d", monthlyTotal)}
남은 예산: ₩${String.format("%,d", remainingBudget)}
""".trimIndent()

        return chat(
            "방금 '$itemName'에 ₩${String.format("%,d", amount)}을 지출했어. 피드백 주고 남은 예산 상태 알려줘.",
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
일일 권장 지출: ₩${String.format("%,d", dailyRecommended)}
이번 달 총 지출: ₩${String.format("%,d", totalSpent)}
""".trimIndent()

        return chat("오늘 재정 상태에 대한 조언을 해줘.", context)
    }
}
