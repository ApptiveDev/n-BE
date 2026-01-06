package masil.backend.modules.member.repository;

import masil.backend.modules.member.entity.MemberImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberImageRepository extends JpaRepository<MemberImage, Long> {
}
