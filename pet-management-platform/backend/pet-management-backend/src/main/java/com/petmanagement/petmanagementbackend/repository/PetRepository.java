package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 宠物数据访问层
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    /**
     * 根据主人ID查询宠物列表
     * @param ownerId 主人ID
     * @return 宠物列表
     */
    List<Pet> findByOwnerId(Long ownerId);
    
    /**
     * 根据宠物ID和主人ID查询宠物（用于验证所有权）
     * @param id 宠物ID
     * @param ownerId 主人ID
     * @return 宠物（如果存在且属于该主人）
     */
    Optional<Pet> findByIdAndOwnerId(Long id, Long ownerId);
    
    /**
     * 统计用户的宠物数量
     * @param ownerId 主人ID
     * @return 宠物数量
     */
    long countByOwnerId(Long ownerId);
}