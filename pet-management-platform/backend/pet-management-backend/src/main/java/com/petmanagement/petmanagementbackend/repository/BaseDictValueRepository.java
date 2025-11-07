package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.BaseDictValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BaseDictValueRepository extends JpaRepository<BaseDictValue, Long> {
    
    List<BaseDictValue> findByDictCodeAndStatus(String dictCode, Integer status);
    
    Optional<BaseDictValue> findByDictCodeAndValueCode(String dictCode, String valueCode);
    
    boolean existsByDictCodeAndValueCode(String dictCode, String valueCode);
    
    // 根据父级字典值查询子级（用于级联，如根据宠物种类查询品种）
    @Query(value = "SELECT v.* FROM base_dict_value v " +
                   "WHERE v.dict_code = :dictCode " +
                   "AND v.status = 1 " +
                   "AND JSON_EXTRACT(v.extra_data, '$.species') = :speciesCode " +
                   "ORDER BY v.value_order", 
           nativeQuery = true)
    List<BaseDictValue> findByDictCodeAndSpecies(@Param("dictCode") String dictCode, 
                                                   @Param("speciesCode") String speciesCode);
}