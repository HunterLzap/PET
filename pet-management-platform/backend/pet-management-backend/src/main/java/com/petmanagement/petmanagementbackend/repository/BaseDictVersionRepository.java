package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.BaseDictVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BaseDictVersionRepository extends JpaRepository<BaseDictVersion, Long> {
    
    List<BaseDictVersion> findByDictCodeOrderByVersionDesc(String dictCode);
    
    List<BaseDictVersion> findByDictCodeAndOperatedAtBetween(
        String dictCode, LocalDateTime startTime, LocalDateTime endTime);
    
    BaseDictVersion findTopByDictCodeOrderByVersionDesc(String dictCode);
}