package masil.backend.modules.member.service;

import lombok.RequiredArgsConstructor;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberImage;
import masil.backend.modules.member.repository.MemberImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberImageLowService {
    private final MemberImageRepository memberImageRepository;

    @Transactional
    public void saveImages(final Member member, final List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            final MemberImage image = new MemberImage(member, imageUrls.get(i), i);
            memberImageRepository.save(image);
        }
    }
}
