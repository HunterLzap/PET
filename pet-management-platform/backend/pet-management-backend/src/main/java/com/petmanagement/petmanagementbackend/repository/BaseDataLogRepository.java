package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.BaseDataLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BaseDataLogRepository extends JpaRepository<BaseDataLog, Long> {
    List<BaseDataLog> findByBaseDataIdOrderByOperatedAtDesc(Long baseDataId);
}
