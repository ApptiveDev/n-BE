package masil.backend.modules.member.repository;

import java.util.List;
import masil.backend.modules.member.entity.MemberImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberImageRepository extends JpaRepository<MemberImage, Long> {
    void deleteByMemberId(Long memberId);
    List<MemberImage> findByMemberIdOrderByDisplayOrder(Long memberId);
}
