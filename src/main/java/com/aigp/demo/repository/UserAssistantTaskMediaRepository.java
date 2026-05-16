package com.aigp.demo.repository;

import com.aigp.demo.domain.media.UserAssistantTaskMedia;
import com.aigp.demo.domain.media.UserAssistantTaskMedia.Pk;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAssistantTaskMediaRepository extends JpaRepository<UserAssistantTaskMedia, Pk> {

	List<UserAssistantTaskMedia> findByTaskIdOrderBySortOrderAsc(Long taskId);

	List<UserAssistantTaskMedia> findByTaskIdInOrderByTaskIdAscSortOrderAsc(Collection<Long> taskIds);

	void deleteByTaskId(Long taskId);
}
