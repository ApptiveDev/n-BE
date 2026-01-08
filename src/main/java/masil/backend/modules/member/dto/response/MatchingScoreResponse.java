package masil.backend.modules.member.dto.response;

import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.enums.MatchingStatus;

import java.util.List;

public record MatchingScoreResponse(
        Long memberId,
        String name,
        String email,
        Integer height,
        Integer weight,
        String residenceArea,
        Double matchingScore,
        String scoreColor,                // 그라데이션 색상 (초록→빨강)
        String scoreLevel,                // 점수 레벨 설명
        Integer matchingCount             // 현재 매칭된 개수
) {
    /**
     * 색상과 레벨 정보를 포함한 Response 생성
     */
    public static MatchingScoreResponse from(Member member, Double score, String color, String level, Integer matchingCount) {
        return new MatchingScoreResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getHeight(),
                member.getWeight(),
                member.getResidenceArea(),
                score,
                color,
                level,
                matchingCount
        );
    }
    
    /**
     * 하위 호환성을 위한 기존 메서드
     */
    @Deprecated
    public static MatchingScoreResponse from(Member member, Double score, Integer matchingCount) {
        return new MatchingScoreResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getHeight(),
                member.getWeight(),
                member.getResidenceArea(),
                score,
                null,
                null,
                matchingCount
        );
    }
}