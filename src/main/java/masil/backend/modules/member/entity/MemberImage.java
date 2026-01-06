package masil.backend.modules.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import masil.backend.global.base.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String imageUrl;

    @Column
    private Integer displayOrder;

    public MemberImage(Member member, String imageUrl, Integer displayOrder) {
        this.member = member;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }
}
