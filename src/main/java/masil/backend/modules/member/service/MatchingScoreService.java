package masil.backend.modules.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberPreference;
import masil.backend.modules.member.enums.*;
import masil.backend.modules.member.repository.MemberPreferenceRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingScoreService {
    
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final ObjectMapper objectMapper;
    
    // 우선순위별 가중치
    private static final double PRIORITY1_WEIGHT = 3.0;
    private static final double PRIORITY2_WEIGHT = 2.0;
    private static final double PRIORITY3_WEIGHT = 1.0;
    
    // 점수 계산 상수
    private static final double PERFECT_SCORE = 100.0;
    private static final double DEFAULT_SCORE = 50.0;
    private static final double MIN_SCORE = 0.0;
    
    // 키 점수 계산 상수
    private static final double HEIGHT_PENALTY_PER_CM = 2.0;
    
    // 학벌 점수 계산 상수
    private static final double EDUCATION_PENALTY_PER_LEVEL = 20.0;
    
    // 자산 점수 계산 상수
    private static final double ASSET_ABOVE_PENALTY_PER_LEVEL = 10.0;
    private static final double ASSET_BELOW_PENALTY_PER_LEVEL = 15.0;
    
    // 직업 점수 계산 상수
    private static final double JOB_PREFERRED_SCORE = 100.0;
    private static final double JOB_AVOIDED_SCORE = 0.0;
    private static final double JOB_NEUTRAL_SCORE = 60.0;
    
    // 점수 구간별 그라데이션 색상 (5점 단위, 어두운 초록 → 진한 빨강)
    private static final String COLOR_100 = "#1B5E20";      // 100: 어두운 초록
    private static final String COLOR_95 = "#2E7D32";       // 95: 진한 초록
    private static final String COLOR_90 = "#388E3C";       // 90: 초록
    private static final String COLOR_85 = "#4CAF50";       // 85: 밝은 초록
    private static final String COLOR_80 = "#8BC34A";       // 80: 연두
    private static final String COLOR_75 = "#CDDC39";       // 75: 라임
    private static final String COLOR_70 = "#FFEB3B";       // 70: 노랑
    private static final String COLOR_65 = "#FFC107";       // 65: amber
    private static final String COLOR_60 = "#FF9800";       // 60: 주황
    private static final String COLOR_55 = "#FF5722";       // 55: 진한 주황
    private static final String COLOR_50 = "#D32F2F";       // 50: 진한 빨강
    private static final String COLOR_UNDER_50 = "#B71C1C"; // 0-50: 더 진한 빨강 (고정)
    
    public Double calculateMatchingScore(Member femaleMember, Member maleMember) {
        log.debug("매칭 점수 계산 시작 - 여성 회원: {}, 남성 회원: {}", 
                  femaleMember.getId(), maleMember.getId());
        
        MemberPreference preference = memberPreferenceRepository.findByMember(femaleMember)
                .orElse(null);
        
        if (preference == null) {
            log.warn("여성 유저 {}의 선호도 정보가 없습니다. 기본 점수 반환.", femaleMember.getId());
            return DEFAULT_SCORE;
        }
        
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        PreferenceCategory priority1 = preference.getPriority1();
        PreferenceCategory priority2 = preference.getPriority2();
        PreferenceCategory priority3 = preference.getPriority3();
        
        log.debug("우선순위 설정 - P1: {}, P2: {}, P3: {}", priority1, priority2, priority3);
        
        // 키 매칭 
        if (isPriorityCategory(priority1, priority2, priority3, PreferenceCategory.HEIGHT)) {
            double heightScore = calculateHeightScore(preference, maleMember);
            double weight = getWeightForCategory(priority1, priority2, priority3, PreferenceCategory.HEIGHT);
            totalScore += heightScore * weight;
            totalWeight += weight;
            log.debug("키 점수: {} (가중치: {})", heightScore, weight);
        }
        
        // 종교 매칭 
        if (isPriorityCategory(priority1, priority2, priority3, PreferenceCategory.RELIGION)) {
            double religionScore = calculateReligionScore(preference, maleMember);
            double weight = getWeightForCategory(priority1, priority2, priority3, PreferenceCategory.RELIGION);
            totalScore += religionScore * weight;
            totalWeight += weight;
            log.debug("종교 점수: {} (가중치: {})", religionScore, weight);
        }
        
        // 학벌 매칭 
        if (isPriorityCategory(priority1, priority2, priority3, PreferenceCategory.EDUCATION)) {
            double educationScore = calculateEducationScore(preference, maleMember);
            double weight = getWeightForCategory(priority1, priority2, priority3, PreferenceCategory.EDUCATION);
            totalScore += educationScore * weight;
            totalWeight += weight;
            log.debug("학벌 점수: {} (가중치: {})", educationScore, weight);
        }
        
        // 자산 매칭
        if (isPriorityCategory(priority1, priority2, priority3, PreferenceCategory.ASSET)) {
            double assetScore = calculateAssetScore(preference, maleMember);
            double weight = getWeightForCategory(priority1, priority2, priority3, PreferenceCategory.ASSET);
            totalScore += assetScore * weight;
            totalWeight += weight;
            log.debug("자산 점수: {} (가중치: {})", assetScore, weight);
        }
        
        // 외모 스타일 매칭 
        if (isPriorityCategory(priority1, priority2, priority3, PreferenceCategory.APPEARANCE)) {
            double appearanceScore = calculateAppearanceScore(preference, maleMember);
            double weight = getWeightForCategory(priority1, priority2, priority3, PreferenceCategory.APPEARANCE);
            totalScore += appearanceScore * weight;
            totalWeight += weight;
            log.debug("외모 스타일 점수: {} (가중치: {})", appearanceScore, weight);
        }
        
        // 직업 매칭
        if (isPriorityCategory(priority1, priority2, priority3, PreferenceCategory.JOB)) {
            double jobScore = calculateJobScore(preference, maleMember);
            double weight = getWeightForCategory(priority1, priority2, priority3, PreferenceCategory.JOB);
            totalScore += jobScore * weight;
            totalWeight += weight;
            log.debug("직업 점수: {} (가중치: {})", jobScore, weight);
        }
        
        // 부모님 자산 매칭
        if (isPriorityCategory(priority1, priority2, priority3, PreferenceCategory.PARENT_ASSET)) {
            double parentAssetScore = calculateParentAssetScore(preference, maleMember);
            double weight = getWeightForCategory(priority1, priority2, priority3, PreferenceCategory.PARENT_ASSET);
            totalScore += parentAssetScore * weight;
            totalWeight += weight;
            log.debug("부모님 자산 점수: {} (가중치: {})", parentAssetScore, weight);
        }
        
        if (totalWeight == 0) {
            log.warn("총 가중치가 0입니다. 기본 점수 반환.");
            return DEFAULT_SCORE;
        }
        
        double finalScore = totalScore / totalWeight;
        finalScore = Math.min(PERFECT_SCORE, Math.max(MIN_SCORE, finalScore));
        
        log.info("최종 매칭 점수: {} (여성: {}, 남성: {})", finalScore, 
                 femaleMember.getId(), maleMember.getId());
        
        return finalScore;
    }
    
    /**
     * 해당 카테고리가 우선순위에 포함되는지 확인합니다.
     */
    private boolean isPriorityCategory(PreferenceCategory p1, PreferenceCategory p2, 
                                       PreferenceCategory p3, PreferenceCategory target) {
        return target == p1 || target == p2 || target == p3;
    }
    
    /**
     * 카테고리의 우선순위에 따른 가중치를 반환합니다.
     */
    private double getWeightForCategory(PreferenceCategory p1, PreferenceCategory p2, 
                                        PreferenceCategory p3, PreferenceCategory target) {
        if (target == p1) return PRIORITY1_WEIGHT;
        if (target == p2) return PRIORITY2_WEIGHT;
        if (target == p3) return PRIORITY3_WEIGHT;
        return 0.0;
    }
    
    /**
     * 키 매칭 점수를 계산합니다.
     * 선호 범위 내면 만점, 범위를 벗어나면 거리에 비례하여 감점합니다.
     */
    private double calculateHeightScore(MemberPreference preference, Member maleMember) {
        if (preference.getPreferredHeightMin() == null || preference.getPreferredHeightMax() == null) {
            log.debug("선호 키 범위가 설정되지 않음. 기본 점수 반환.");
            return DEFAULT_SCORE;
        }
        
        Integer height = maleMember.getHeight();
        if (height == null) {
            log.debug("남성 회원의 키 정보 없음. 최저 점수 반환.");
            return MIN_SCORE;
        }
        
        int min = preference.getPreferredHeightMin();
        int max = preference.getPreferredHeightMax();
        
        if (height >= min && height <= max) {
            log.debug("키 {}cm가 선호 범위({}-{}) 내에 있음. 만점.", height, min, max);
            return PERFECT_SCORE;
        } else {
            // 범위를 벗어나면 가장 가까운 경계와의 거리에 따라 감점
            int distance = Math.min(
                    Math.abs(height - min),
                    Math.abs(height - max)
            );
            double score = Math.max(MIN_SCORE, PERFECT_SCORE - (distance * HEIGHT_PENALTY_PER_CM));
            log.debug("키 {}cm가 선호 범위({}-{})를 벗어남. 거리: {}cm, 점수: {}", 
                      height, min, max, distance, score);
            return score;
        }
    }
    
    /**
     * 종교 매칭 점수를 계산합니다.
     * 기피 종교에 해당하면 0점, 그렇지 않으면 만점입니다.
     */
    private double calculateReligionScore(MemberPreference preference, Member maleMember) {
        Religion memberReligion = maleMember.getReligion();
        
        if (memberReligion == null) {
            log.debug("남성 회원의 종교 정보 없음. 기본 점수 반환.");
            return DEFAULT_SCORE;
        }
        
        if (preference.getAvoidReligionsBitmask() == null || preference.getAvoidReligionsBitmask() == 0) {
            log.debug("기피 종교 설정 없음. 만점 반환.");
            return PERFECT_SCORE;
        }
        
        List<Religion> avoidReligions = Religion.fromBitmask(preference.getAvoidReligionsBitmask());
        
        if (avoidReligions.contains(memberReligion)) {
            log.debug("종교 {}가 기피 종교 목록에 포함됨. 최저 점수.", memberReligion);
            return MIN_SCORE;
        }
        
        log.debug("종교 {}가 기피 종교가 아님. 만점.", memberReligion);
        return PERFECT_SCORE;
    }
    
    /**
     * 학벌 매칭 점수를 계산합니다.
     * 선호 학벌 이상이면 만점, 낮으면 차이에 비례하여 감점합니다.
     */
    private double calculateEducationScore(MemberPreference preference, Member maleMember) {
        EducationLevel preferredLevel = preference.getPreferredEducationLevel();
        Education memberEducation = maleMember.getEducation();
        
        if (preferredLevel == null) {
            log.debug("선호 학벌 설정 없음. 기본 점수 반환.");
            return DEFAULT_SCORE;
        }
        
        if (memberEducation == null) {
            log.debug("남성 회원의 학벌 정보 없음. 최저 점수 반환.");
            return MIN_SCORE;
        }
        
        // EducationLevel과 Education을 비교하기 위해 ordinal 사용
        // 높은 ordinal = 높은 학력으로 가정
        int preferredOrdinal = preferredLevel.ordinal();
        int memberOrdinal = memberEducation.ordinal();
        
        if (memberOrdinal >= preferredOrdinal) {
            log.debug("학벌 {}가 선호 레벨 {} 이상. 만점.", memberEducation, preferredLevel);
            return PERFECT_SCORE;
        } else {
            // 낮으면 차이만큼 감점
            int diff = preferredOrdinal - memberOrdinal;
            double score = Math.max(MIN_SCORE, PERFECT_SCORE - (diff * EDUCATION_PENALTY_PER_LEVEL));
            log.debug("학벌 {}가 선호 레벨 {} 미만. 차이: {}, 점수: {}", 
                      memberEducation, preferredLevel, diff, score);
            return score;
        }
    }
    
    /**
     * 자산 매칭 점수를 계산합니다.
     * Asset enum의 중간값을 기준으로 선호 범위와 비교합니다.
     */
    private double calculateAssetScore(MemberPreference preference, Member maleMember) {
        Long preferredMin = preference.getPreferredAssetMin();
        Long preferredMax = preference.getPreferredAssetMax();
        Asset memberAsset = maleMember.getAsset();
        
        if (preferredMin == null || preferredMax == null) {
            log.debug("선호 자산 범위 설정 없음. 기본 점수 반환.");
            return DEFAULT_SCORE;
        }
        
        if (memberAsset == null) {
            log.debug("남성 회원의 자산 정보 없음. 최저 점수 반환.");
            return MIN_SCORE;
        }
        
        // Asset enum을 실제 금액 범위로 변환
        long memberAssetValue = convertAssetToValue(memberAsset);
        
        // 선호 범위 내에 있는 경우
        if (memberAssetValue >= preferredMin && memberAssetValue <= preferredMax) {
            log.debug("자산 {}가 선호 범위({}-{}) 내에 있음. 보너스 점수.", 
                      memberAsset, preferredMin, preferredMax);
            return PERFECT_SCORE;
        }
        
        // 선호 범위보다 높은 경우 (약간의 보너스)
        if (memberAssetValue > preferredMax) {
            long excess = memberAssetValue - preferredMax;
            long excessLevels = excess / 100_000_000; // 1억 단위로 계산
            double score = Math.max(DEFAULT_SCORE, 
                                  PERFECT_SCORE - (excessLevels * ASSET_ABOVE_PENALTY_PER_LEVEL));
            log.debug("자산 {}가 선호 범위보다 높음. 초과: {}억, 점수: {}", 
                      memberAsset, excessLevels, score);
            return score;
        }
        
        // 선호 범위보다 낮은 경우
        long shortfall = preferredMin - memberAssetValue;
        long shortfallLevels = shortfall / 100_000_000; // 1억 단위로 계산
        double score = Math.max(MIN_SCORE, 
                              PERFECT_SCORE - (shortfallLevels * ASSET_BELOW_PENALTY_PER_LEVEL));
        log.debug("자산 {}가 선호 범위보다 낮음. 부족: {}억, 점수: {}", 
                  memberAsset, shortfallLevels, score);
        return score;
    }
    
    /**
     * Asset enum을 실제 금액(중간값)으로 변환합니다.
     */
    private long convertAssetToValue(Asset asset) {
        return switch (asset) {
            case UNDER_100M -> 50_000_000L;           // 5천만원 (중간값)
            case BETWEEN_100M_300M -> 200_000_000L;   // 2억원 (중간값)
            case BETWEEN_300M_500M -> 400_000_000L;   // 4억원 (중간값)
            case BETWEEN_500M_1B -> 750_000_000L;     // 7.5억원 (중간값)
            case OVER_1B -> 1_500_000_000L;           // 15억원 (추정값)
        };
    }
    
    /**
     * 외모 스타일 매칭 점수를 계산합니다.
     * 현재 Member 엔티티에 AppearanceStyle 필드가 없어 기본 점수를 반환합니다.
     * 향후 Member 엔티티에 appearanceStyle 필드 추가 시 구현 필요.
     */
    private double calculateAppearanceScore(MemberPreference preference, Member maleMember) {
        AppearanceStyle preferredStyle = preference.getPreferredAppearanceStyle();
        
        if (preferredStyle == null) {
            log.debug("선호 외모 스타일 설정 없음. 만점 반환.");
            return PERFECT_SCORE;
        }
        
        // TODO: Member 엔티티에 AppearanceStyle 필드 추가 후 실제 비교 로직 구현
        // 현재는 Member에 해당 필드가 없으므로 기본 점수 반환
        log.debug("Member 엔티티에 외모 스타일 정보 없음. 기본 점수 반환.");
        return DEFAULT_SCORE;
        
        /* 향후 구현 예시:
        AppearanceStyle memberStyle = maleMember.getAppearanceStyle();
        if (memberStyle == null) {
            return DEFAULT_SCORE;
        }
        if (memberStyle == preferredStyle) {
            return PERFECT_SCORE;
        }
        return DEFAULT_SCORE;
        */
    }
    
    /**
     * 직업 매칭 점수를 계산합니다.
     * JSON으로 저장된 선호/기피 직업 목록을 파싱하여 비교합니다.
     * 현재 Member 엔티티에 JobType 필드가 없어 기본 점수를 반환합니다.
     */
    private double calculateJobScore(MemberPreference preference, Member maleMember) {
        // TODO: Member 엔티티에 JobType 필드 추가 후 실제 비교 로직 구현
        // 현재는 Member에 직업 필드가 없으므로 기본 점수 반환
        log.debug("Member 엔티티에 직업 정보 없음. 기본 점수 반환.");
        return DEFAULT_SCORE;
        
        /* 향후 구현 예시:
        JobType memberJob = maleMember.getJobType();
        
        if (memberJob == null) {
            log.debug("남성 회원의 직업 정보 없음. 기본 점수 반환.");
            return DEFAULT_SCORE;
        }
        
        // 기피 직업 목록 확인
        List<JobType> avoidedJobs = parseJobsFromJson(preference.getAvoidedJobs());
        if (avoidedJobs.contains(memberJob)) {
            log.debug("직업 {}가 기피 직업 목록에 포함됨. 최저 점수.", memberJob);
            return JOB_AVOIDED_SCORE;
        }
        
        // 선호 직업 목록 확인
        List<JobType> preferredJobs = parseJobsFromJson(preference.getPreferredJobs());
        if (!preferredJobs.isEmpty() && preferredJobs.contains(memberJob)) {
            log.debug("직업 {}가 선호 직업 목록에 포함됨. 만점.", memberJob);
            return JOB_PREFERRED_SCORE;
        }
        
        // 선호/기피 둘 다 아닌 경우
        log.debug("직업 {}가 중립적. 기본 점수.", memberJob);
        return JOB_NEUTRAL_SCORE;
        */
    }
    
    /**
     * JSON 문자열로 저장된 직업 목록을 파싱합니다.
     */
    private List<JobType> parseJobsFromJson(String jobsJson) {
        if (jobsJson == null || jobsJson.isBlank()) {
            return Collections.emptyList();
        }
        
        try {
            return objectMapper.readValue(jobsJson, new TypeReference<List<JobType>>() {});
        } catch (JsonProcessingException e) {
            log.error("직업 목록 JSON 파싱 실패: {}", jobsJson, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 부모님 자산 매칭 점수를 계산합니다.
     * 현재 Member 엔티티에 ParentAssetLevel 필드가 없어 기본 점수를 반환합니다.
     * 향후 Member 엔티티에 parentAssetLevel 필드 추가 시 구현 필요.
     */
    private double calculateParentAssetScore(MemberPreference preference, Member maleMember) {
        ParentAssetLevel requirement = preference.getParentAssetRequirement();
        
        if (requirement == null || requirement == ParentAssetLevel.NO_CONCERN) {
            log.debug("부모 자산 요구사항 없음. 만점 반환.");
            return PERFECT_SCORE;
        }
        
        // TODO: Member 엔티티에 ParentAssetLevel 필드 추가 후 실제 비교 로직 구현
        // 현재는 Member에 해당 필드가 없으므로 기본 점수 반환
        log.debug("Member 엔티티에 부모 자산 정보 없음. 기본 점수 반환.");
        return DEFAULT_SCORE;
        
        /* 향후 구현 예시:
        ParentAssetLevel memberParentAsset = maleMember.getParentAssetLevel();
        
        if (memberParentAsset == null) {
            log.debug("남성 회원의 부모 자산 정보 없음. 최저 점수 반환.");
            return MIN_SCORE;
        }
        
        // 요구사항에 따른 매칭
        if (requirement == ParentAssetLevel.OVER_100M) {
            if (memberParentAsset == ParentAssetLevel.OVER_100M) {
                return PERFECT_SCORE;
            }
            return MIN_SCORE;
        } else if (requirement == ParentAssetLevel.RETIREMENT_ONLY) {
            if (memberParentAsset == ParentAssetLevel.OVER_100M || 
                memberParentAsset == ParentAssetLevel.RETIREMENT_ONLY) {
                return PERFECT_SCORE;
            }
            return DEFAULT_SCORE;
        }
        
        return PERFECT_SCORE;
        */
    }

    public String getScoreColorGradient(double score) {
        if (score < 0 || score > 100) {
            log.warn("유효하지 않은 점수: {}. 기본 색상 반환.", score);
            return COLOR_UNDER_50;
        }
        
        // 5점 단위 그라데이션 (어두운 초록 → 진한 빨강)
        if (score == 100) return COLOR_100;     // 100: 어두운 초록
        if (score >= 95) return COLOR_95;       // 95-99: 진한 초록
        if (score >= 90) return COLOR_90;       // 90-94: 초록
        if (score >= 85) return COLOR_85;       // 85-89: 밝은 초록
        if (score >= 80) return COLOR_80;       // 80-84: 연두
        if (score >= 75) return COLOR_75;       // 75-79: 라임
        if (score >= 70) return COLOR_70;       // 70-74: 노랑
        if (score >= 65) return COLOR_65;       // 65-69: amber
        if (score >= 60) return COLOR_60;       // 60-64: 주황
        if (score >= 55) return COLOR_55;       // 55-59: 진한 주황
        if (score >= 50) return COLOR_50;       // 50-54: 진한 빨강
        
        // 50점 미만은 모두 고정 빨강색
        return COLOR_UNDER_50;
    }
    

    public String getScoreLevel(double score) {
        if (score < 0 || score > 100) {
            return "알 수 없음";
        }
        
        if (score == 100) return "완벽한 매칭";
        if (score >= 95) return "최고의 매칭";
        if (score >= 90) return "매우 높은 매칭";
        if (score >= 85) return "높은 매칭";
        if (score >= 80) return "상당히 좋은 매칭";
        if (score >= 75) return "좋은 매칭";
        if (score >= 70) return "괜찮은 매칭";
        if (score >= 65) return "보통 이상";
        if (score >= 60) return "보통 매칭";
        if (score >= 55) return "보통 이하";
        if (score >= 50) return "다소 낮은 매칭";
        return "낮은 매칭";
    }
}