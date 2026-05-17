package com.aigp.demo.repository;

import com.aigp.demo.domain.media.UserMediaAsset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMediaAssetRepository extends JpaRepository<UserMediaAsset, Long> {

	Optional<UserMediaAsset> findByIdAndUser_Id(Long id, Long userId);

	List<UserMediaAsset> findByIdInAndUser_Id(Collection<Long> ids, Long userId);
}
