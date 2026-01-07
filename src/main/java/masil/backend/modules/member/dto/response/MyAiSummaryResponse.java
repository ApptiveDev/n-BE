package masil.backend.modules.member.dto.response;

import masil.backend.modules.member.entity.Member;

public record MyAiSummaryResponse(
        Long memberId,
        String name,
        String aiSummaryKo,
        String aiSummaryJa
) {
    public MyAiSummaryResponse(final Member member) {
        this(
                member.getId(),
                member.getName(),
                member.getAiSummary(),
                member.getAiSummaryJp()
        );
    }
}
