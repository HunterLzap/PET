package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.BaseData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaseDataRepository extends JpaRepository<BaseData, Long> {
    List<BaseData> findByType(String type);
    Optional<BaseData> findByTypeAndValue(String type, String value);
    boolean existsByTypeAndValue(String type, String value);
}

