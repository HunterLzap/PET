package com.petmanagement.petmanagementbackend.service;

import com.petmanagement.petmanagementbackend.models.Pet;
import com.petmanagement.petmanagementbackend.payload.request.AddPetRequest;
import com.petmanagement.petmanagementbackend.payload.request.UpdatePetRequest;
import com.petmanagement.petmanagementbackend.payload.response.PetResponse;
import com.petmanagement.petmanagementbackend.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 宠物管理服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {
    
    private final PetRepository petRepository;
    private final BaseDictService baseDictService;
    
    /**
     * 添加宠物
     * @param userId 用户ID
     * @param request 添加宠物请求
     * @return 宠物信息
     */
    @Transactional
    public PetResponse addPet(Long userId, AddPetRequest request) {
        log.info("用户 {} 添加宠物: {}", userId, request.getName());
        
        // 1. 验证基础数据有效性
        validateDictValues(request.getSpeciesCode(), request.getBreedCode(), request.getGenderCode());
        
        // 2. 获取基础数据名称（冗余存储）
        String speciesName = baseDictService.getDictValueName("pet_species", request.getSpeciesCode());
        String breedName = baseDictService.getDictValueName("pet_breed", request.getBreedCode());
        String genderName = baseDictService.getDictValueName("pet_gender", request.getGenderCode());
        
        // 3. 构建宠物对象
        Pet pet = new Pet();
        pet.setOwnerId(userId);
        pet.setName(request.getName());
        pet.setSpeciesCode(request.getSpeciesCode());
        pet.setSpeciesName(speciesName);
        pet.setBreedCode(request.getBreedCode());
        pet.setBreedName(breedName);
        pet.setGenderCode(request.getGenderCode());
        pet.setGenderName(genderName);
        pet.setBirthday(request.getBirthDate());
        pet.setWeight(request.getWeight());
        pet.setColor(request.getColor());
        pet.setDescription(request.getDescription());
        pet.setAvatarUrl(request.getAvatarUrl());
        
        // 4. 保存到数据库
        Pet savedPet = petRepository.save(pet);
        
        log.info("宠物添加成功，ID: {}", savedPet.getId());
        
        // 5. 转换为响应对象
        return convertToResponse(savedPet);
    }
    
    /**
     * 获取我的宠物列表
     * @param userId 用户ID
     * @return 宠物列表
     */
    public List<PetResponse> getMyPets(Long userId) {
        log.info("查询用户 {} 的宠物列表", userId);
        
        List<Pet> pets = petRepository.findByOwnerId(userId);
        
        return pets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取宠物详情
     * @param userId 用户ID
     * @param petId 宠物ID
     * @return 宠物详情
     */
    public PetResponse getPetDetail(Long userId, Long petId) {
        log.info("查询宠物详情，用户: {}, 宠物ID: {}", userId, petId);
        
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new RuntimeException("宠物不存在或无权访问"));
        
        return convertToResponse(pet);
    }
    
    /**
     * 更新宠物信息
     * @param userId 用户ID
     * @param petId 宠物ID
     * @param request 更新请求
     * @return 更新后的宠物信息
     */
    @Transactional
    public PetResponse updatePet(Long userId, Long petId, UpdatePetRequest request) {
        log.info("更新宠物信息，用户: {}, 宠物ID: {}", userId, petId);
        
        // 1. 查询宠物（验证所有权）
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new RuntimeException("宠物不存在或无权访问"));
        
        // 2. 验证基础数据
        validateDictValues(pet.getSpeciesCode(), request.getBreedCode(), request.getGenderCode());
        
        // 3. 更新字段
        pet.setName(request.getName());
        pet.setBreedCode(request.getBreedCode());
        pet.setBreedName(baseDictService.getDictValueName("pet_breed", request.getBreedCode()));
        pet.setGenderCode(request.getGenderCode());
        pet.setGenderName(baseDictService.getDictValueName("pet_gender", request.getGenderCode()));
        pet.setBirthday(request.getBirthDate());
        pet.setWeight(request.getWeight());
        pet.setColor(request.getColor());
        pet.setDescription(request.getDescription());
        pet.setAvatarUrl(request.getAvatarUrl());
        
        // 4. 保存
        Pet updatedPet = petRepository.save(pet);
        
        log.info("宠物信息更新成功，ID: {}", petId);
        
        return convertToResponse(updatedPet);
    }
    
    /**
     * 删除宠物
     * @param userId 用户ID
     * @param petId 宠物ID
     */
    @Transactional
    public void deletePet(Long userId, Long petId) {
        log.info("删除宠物，用户: {}, 宠物ID: {}", userId, petId);
        
        // 1. 验证所有权
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new RuntimeException("宠物不存在或无权访问"));
        
        // 2. 删除
        petRepository.delete(pet);
        
        log.info("宠物删除成功，ID: {}", petId);
    }
    
    /**
     * 验证基础数据有效性
     */
    private void validateDictValues(String speciesCode, String breedCode, String genderCode) {
        if (!baseDictService.isValidDictValue("pet_species", speciesCode)) {
            throw new RuntimeException("无效的宠物种类");
        }
        if (!baseDictService.isValidDictValue("pet_breed", breedCode)) {
            throw new RuntimeException("无效的宠物品种");
        }
        if (!baseDictService.isValidDictValue("pet_gender", genderCode)) {
            throw new RuntimeException("无效的宠物性别");
        }
    }
    
    /**
     * 转换为响应对象
     */
    private PetResponse convertToResponse(Pet pet) {
        // 计算年龄（月）
        Integer ageInMonths = null;
        if (pet.getBirthday() != null) {
            Period period = Period.between(pet.getBirthday(), LocalDate.now());
            ageInMonths = period.getYears() * 12 + period.getMonths();
        }
        
        return PetResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .speciesCode(pet.getSpeciesCode())
                .speciesName(pet.getSpeciesName())
                .breedCode(pet.getBreedCode())
                .breedName(pet.getBreedName())
                .genderCode(pet.getGenderCode())
                .genderName(pet.getGenderName())
                .birthDate(pet.getBirthday())
                .ageInMonths(ageInMonths)
                .weight(pet.getWeight())
                .color(pet.getColor())
                .description(pet.getDescription())
                .avatarUrl(pet.getAvatarUrl())
                .createdAt(pet.getCreatedAt())
                .updatedAt(pet.getUpdatedAt())
                .build();
    }
}