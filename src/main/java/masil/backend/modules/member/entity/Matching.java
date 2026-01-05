package masil.backend.modules.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import masil.backend.global.base.BaseEntity;
import masil.backend.modules.member.enums.MatchingStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "matching")
public class Matching extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "female_member_id", nullable = false)
    private Member femaleMember;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "male_member_id", nullable = false)
    private Member maleMember;
    @Column(nullable = false)
    private Integer matchingOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status = MatchingStatus.PENDING_FEMALE_SELECTION;
    
    @Builder
    private Matching(Member femaleMember, Member maleMember, Integer matchingOrder) {
        this.femaleMember = femaleMember;
        this.maleMember = maleMember;
        this.matchingOrder = matchingOrder;
        this.status = MatchingStatus.PENDING_FEMALE_SELECTION;
    }
    
    public void updateMaleMember(Member newMaleMember) {
        this.maleMember = newMaleMember;
    }
    //매칭 수락 거절 로직
    

    public void selectByFemale() {
        this.status = MatchingStatus.PENDING_MALE_ACCEPTANCE;
    }
    public void acceptByMale() {
        this.status = MatchingStatus.ACCEPTED;
    }
    public void reject() {
        this.status = MatchingStatus.REJECTED;
    }
}