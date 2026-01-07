package masil.backend.modules.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.modules.member.entity.Member;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAiService {
    private final ChatClient.Builder chatClientBuilder;
    private final MemberLowService memberLowService;

    @Async
    @Transactional
    public void generateMemberSummary(final Long memberId, final String otherInfo) {
        try {
            log.info("AI 요약 생성 시작 - memberId: {}", memberId);

            if (otherInfo == null || otherInfo.isBlank()) {
                log.warn("자기소개가 비어있어 AI 요약을 생성하지 않습니다. - memberId: {}", memberId);
                return;
            }

            // 한국어 요약 생성
            final String koreanPrompt = createKoreanPrompt(otherInfo);
            final String aiSummaryKr = callGptApi(koreanPrompt);

            // 일본어 요약 생성
            final String japanesePrompt = createJapanesePrompt(otherInfo);
            final String aiSummaryJp = callGptApi(japanesePrompt);

            // 회원 정보 업데이트
            final Member member = memberLowService.getValidateExistMemberById(memberId);
            member.updateAiSummary(aiSummaryKr);
            member.updateAiSummaryJp(aiSummaryJp);

            log.info("AI 요약 생성 완료 - memberId: {}, summaryKr: {}, summaryJp: {}",
                    memberId, aiSummaryKr, aiSummaryJp);
        } catch (Exception e) {
            log.error("AI 요약 생성 실패 - memberId: {}", memberId, e);
        }
    }

    private String createKoreanPrompt(final String otherInfo) {
        return String.format("""
                당신은 소개팅 앱의 회원 프로필을 요약하는 AI입니다.
                다음 자기소개를 읽고, 이 사람을 1-2줄로 매력적이고 간결하게 요약해주세요.
                
                [자기소개]
                %s
                
                요구사항:
                1. 반드시 1-2줄 이내로 작성
                2. 이 사람의 매력 포인트와 특징을 강조
                3. 존댓말을 사용하지 말고 자연스러운 소개 형식으로 작성
                4. 3인칭 시점으로 작성 (예: "~한 사람입니다.", "~을 좋아하는 분입니다.")
                5. 긍정적이고 매력적인 표현 사용
                
                예시:
                - "여행과 독서를 사랑하며, 새로운 경험에 열려있는 긍정적인 사람입니다."
                - "운동과 건강한 라이프스타일을 추구하며, 솔직하고 진솔한 대화를 중요시하는 분입니다."
                """, otherInfo);
    }

    private String createJapanesePrompt(final String otherInfo) {
        return String.format("""
                あなたはマッチングアプリの会員プロフィールを要約するAIです。
                次の自己紹介を読んで、この人を1-2行で魅力的かつ簡潔に要約してください。
                
                [自己紹介]
                %s
                
                要件:
                1. 必ず1-2行以内で作成
                2. この人の魅力ポイントと特徴を強調
                3. 敬語は使わず、自然な紹介形式で作成
                4. 三人称視点で作成 (例: "〜な人です。", "〜を好きな方です。")
                5. ポジティブで魅力的な表現を使用
                
                例:
                - "旅行と読書を愛し、新しい経験にオープンなポジティブな人です。"
                - "運動と健康的なライフスタイルを追求し、率直で真摯な会話を大切にする方です。"
                """, otherInfo);
    }

    private String callGptApi(final String prompt) {
        final ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
