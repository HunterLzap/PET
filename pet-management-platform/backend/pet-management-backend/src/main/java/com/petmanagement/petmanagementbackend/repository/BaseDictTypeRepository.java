package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.BaseDictType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaseDictTypeRepository extends JpaRepository<BaseDictType, Long> {
    
    Optional<BaseDictType> findByDictCode(String dictCode);
    
    List<BaseDictType> findByParentCodeAndStatus(String parentCode, Integer status);
    
    List<BaseDictType> findByStatus(Integer status);
    
    boolean existsByDictCode(String dictCode);
}