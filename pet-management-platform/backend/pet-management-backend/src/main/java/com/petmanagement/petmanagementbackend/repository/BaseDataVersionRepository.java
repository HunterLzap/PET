package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.BaseDataVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BaseDataVersionRepository extends JpaRepository<BaseDataVersion, Long> {
    List<BaseDataVersion> findByBaseDataIdOrderByVersionDesc(Long baseDataId);
}
