package masil.backend.modules.member.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import masil.backend.global.base.BaseEntity;
import masil.backend.modules.member.enums.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE member SET is_deleted = true where id = ?")
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column
    private String providerId;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column
    private Integer height;

    @Column
    private Integer weight;

    @Column
    private String residenceArea;

    @Enumerated(EnumType.STRING)
    @Column
    private SmokingStatus smokingStatus;

    @Enumerated(EnumType.STRING)
    @Column
    private DrinkingFrequency drinkingFrequency;

    @Enumerated(EnumType.STRING)
    @Column
    private Religion religion;

    @Enumerated(EnumType.STRING)
    @Column
    private Education education;

    @Enumerated(EnumType.STRING)
    @Column
    private Asset asset;

    @Column(columnDefinition = "TEXT")
    private String otherInfo;

    @Column
    private String thumbnailImageUrl;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(length = 500)
    private String fcmToken;

    @Builder
    private Member(
            final Long id, final Provider provider, final String providerId,
            final String name, final String email, final String password,
            final Gender gender, final Integer height, final Integer weight, final String residenceArea,
            final SmokingStatus smokingStatus, final DrinkingFrequency drinkingFrequency, final Religion religion,
            final Education education, final Asset asset, final String otherInfo, final String thumbnailImageUrl, final String aiSummary
    ) {
        this.id = id;
        this.provider = provider != null ? provider : Provider.LOCAL;
        this.providerId = providerId;
        this.isDeleted = false;
        this.status = MemberStatus.INCOMPLETE_PROFILE;
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.residenceArea = residenceArea;
        this.smokingStatus = smokingStatus;
        this.drinkingFrequency = drinkingFrequency;
        this.religion = religion;
        this.education = education;
        this.asset = asset;
        this.otherInfo = otherInfo;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.aiSummary = aiSummary;
    }

    public void updateProfile(
            final String name,
            final Gender gender,
            final Integer height,
            final Integer weight,
            final String residenceArea,
            final SmokingStatus smokingStatus,
            final DrinkingFrequency drinkingFrequency,
            final Religion religion,
            final Education education,
            final Asset asset,
            final String otherInfo,
            final String thumbnailImageUrl
    ) {
        this.name = name;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.residenceArea = residenceArea;
        this.smokingStatus = smokingStatus;
        this.drinkingFrequency = drinkingFrequency;
        this.religion = religion;
        this.education = education;
        this.asset = asset;
        this.otherInfo = otherInfo;
        this.thumbnailImageUrl = thumbnailImageUrl;
    }

    public void updateAiSummary(final String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public void changeToConnecting() {
        this.status = MemberStatus.CONNECTING;
    }

    public void changeStatus(MemberStatus newStatus) {
        this.status = newStatus;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
