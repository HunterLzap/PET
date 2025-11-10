package com.petmanagement.petmanagementbackend.service;

import com.petmanagement.petmanagementbackend.models.Customer;
import com.petmanagement.petmanagementbackend.models.Pet;
import com.petmanagement.petmanagementbackend.payload.request.AddPetRequest;
import com.petmanagement.petmanagementbackend.payload.request.UpdatePetRequest;
import com.petmanagement.petmanagementbackend.payload.response.PetResponse;
import com.petmanagement.petmanagementbackend.repository.CustomerRepository;
import com.petmanagement.petmanagementbackend.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {
    
    private final PetRepository petRepository;
    private final CustomerRepository customerRepository;
    private final BaseDictService baseDictService;
    
    /**
     * 用户添加自己的宠物
     */
    @Transactional
    public PetResponse addPet(Long userId, AddPetRequest request) {
        log.info("用户 {} 添加宠物: {}", userId, request.getName());
        
        validateDictValues(request.getSpeciesCode(), request.getBreedCode(), request.getGenderCode());
        
        String speciesName = baseDictService.getDictValueName("pet_species", request.getSpeciesCode());
        String breedName = baseDictService.getDictValueName("pet_breed", request.getBreedCode());
        String genderName = baseDictService.getDictValueName("pet_gender", request.getGenderCode());
        
        Pet pet = new Pet();
        pet.setOwnerId(userId);  // 用户自己的宠物
        pet.setCustomerId(null);  // 不属于客户
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
        
        Pet savedPet = petRepository.save(pet);
        log.info("宠物添加成功，ID: {}", savedPet.getId());
        
        return convertToResponse(savedPet);
    }
    
    /**
     * ✅ 商家为客户添加宠物
     */
    @Transactional
    public PetResponse addPetForCustomer(Long customerId, AddPetRequest request) {
        log.info("为客户 {} 添加宠物: {}", customerId, request.getName());
        
        // 验证客户是否存在
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("客户不存在"));
        
        validateDictValues(request.getSpeciesCode(), request.getBreedCode(), request.getGenderCode());
        
        String speciesName = baseDictService.getDictValueName("pet_species", request.getSpeciesCode());
        String breedName = baseDictService.getDictValueName("pet_breed", request.getBreedCode());
        String genderName = baseDictService.getDictValueName("pet_gender", request.getGenderCode());
        
        Pet pet = new Pet();
        pet.setOwnerId(null);  // 不属于系统用户
        pet.setCustomerId(customerId);  // 属于商家的客户
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
        
        Pet savedPet = petRepository.save(pet);
        log.info("为客户添加宠物成功，宠物ID: {}", savedPet.getId());
        
        return convertToResponse(savedPet);
    }
    
    /**
     * 获取用户自己的宠物列表
     */
    public List<PetResponse> getMyPets(Long userId) {
        log.info("查询用户 {} 的宠物列表", userId);
        List<Pet> pets = petRepository.findByOwnerId(userId);
        return pets.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * ✅ 获取客户的宠物列表
     */
    public List<PetResponse> getCustomerPets(Long customerId) {
        log.info("查询客户 {} 的宠物列表", customerId);
        List<Pet> pets = petRepository.findByCustomerId(customerId);
        return pets.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * ✅ 获取商家所有客户的宠物（用于订单创建时选择）
     */
    public List<PetResponse> getMerchantCustomerPets(Long merchantId) {
        log.info("查询商家 {} 的所有客户宠物", merchantId);
        
        // 获取商家的所有客户
        List<Customer> customers = customerRepository.findByMerchantId(merchantId);
        
        // 获取所有客户的宠物
        List<Pet> allPets = new ArrayList<>();
        for (Customer customer : customers) {
            List<Pet> pets = petRepository.findByCustomerId(customer.getId());
            allPets.addAll(pets);
        }
        
        return allPets.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * 获取所有宠物列表（管理员使用）
     */
    public List<PetResponse> getAllPets() {
        log.info("查询所有宠物列表");
        List<Pet> pets = petRepository.findAll();
        return pets.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * 获取宠物详情
     */
    public PetResponse getPetDetail(Long userId, Long petId) {
        log.info("查询宠物详情，用户: {}, 宠物ID: {}", userId, petId);
        
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new RuntimeException("宠物不存在或无权访问"));
        
        return convertToResponse(pet);
    }
    
    /**
     * 更新宠物信息
     */
    @Transactional
    public PetResponse updatePet(Long userId, Long petId, UpdatePetRequest request) {
        log.info("更新宠物信息，用户: {}, 宠物ID: {}", userId, petId);
        
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new RuntimeException("宠物不存在或无权访问"));
        
        validateDictValues(pet.getSpeciesCode(), request.getBreedCode(), request.getGenderCode());
        
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
        
        Pet updatedPet = petRepository.save(pet);
        log.info("宠物信息更新成功，ID: {}", petId);
        
        return convertToResponse(updatedPet);
    }
    
    /**
     * 删除宠物
     */
    @Transactional
    public void deletePet(Long userId, Long petId) {
        log.info("删除宠物，用户: {}, 宠物ID: {}", userId, petId);
        
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new RuntimeException("宠物不存在或无权访问"));
        
        petRepository.delete(pet);
        log.info("宠物删除成功，ID: {}", petId);
    }
    
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
    
    private PetResponse convertToResponse(Pet pet) {
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