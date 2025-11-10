package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.payload.request.AddPetRequest;
import com.petmanagement.petmanagementbackend.payload.request.UpdatePetRequest;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.payload.response.PetResponse;
import com.petmanagement.petmanagementbackend.security.services.UserDetailsImpl;
import com.petmanagement.petmanagementbackend.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/pets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PetController {
    
    private final PetService petService;
    
    /**
     * 获取宠物列表
     * - 用户：获取自己的宠物
     * - 商家：获取所有宠物（用于创建订单时选择）
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<List<PetResponse>> getMyPets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        
        boolean isMerchant = authentication.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
            a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
            a.getAuthority().equals("ROLE_MERCHANT_GOODS")
        );
        
        List<PetResponse> pets;
        if (isMerchant) {
            // 商家获取自己客户的所有宠物
            pets = petService.getMerchantCustomerPets(userId);
        } else {
            // 普通用户获取自己的宠物
            pets = petService.getMyPets(userId);
        }
        
        return ResponseEntity.ok(pets);
    }
    
    /**
     * 获取宠物详情
     */
    @GetMapping("/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<PetResponse> getPetDetail(
            @PathVariable Long petId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        PetResponse pet = petService.getPetDetail(userId, petId);
        return ResponseEntity.ok(pet);
    }
    
    /**
     * 添加宠物
     * - 用户：为自己添加宠物
     * - 商家：为客户添加宠物（需要传customerId）
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<?> addPet(
            @Valid @RequestBody AddPetRequest request,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        boolean isMerchant = authentication.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
            a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
            a.getAuthority().equals("ROLE_MERCHANT_GOODS")
        );
        
        PetResponse pet;
        
        if (isMerchant) {
            // 商家为客户添加宠物
            if (request.getCustomerId() == null) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse("商家添加宠物时必须指定客户ID"));
            }
            pet = petService.addPetForCustomer(request.getCustomerId(), request);
        } else {
            // 用户为自己添加宠物
            pet = petService.addPet(userDetails.getId(), request);
        }
        
        return ResponseEntity.ok(pet);
    }
    
    /**
     * 更新宠物信息
     */
    @PutMapping("/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable Long petId,
            @Valid @RequestBody UpdatePetRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        PetResponse pet = petService.updatePet(userId, petId, request);
        return ResponseEntity.ok(pet);
    }
    
    /**
     * 删除宠物
     */
    @DeleteMapping("/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePet(
            @PathVariable Long petId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        petService.deletePet(userId, petId);
        return ResponseEntity.ok(new MessageResponse("宠物删除成功"));
    }
    
    /**
     * ✅ 新增：获取指定客户的宠物列表（商家使用）
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<List<PetResponse>> getCustomerPets(@PathVariable Long customerId) {
        List<PetResponse> pets = petService.getCustomerPets(customerId);
        return ResponseEntity.ok(pets);
    }
    
    private Long getCurrentUserId(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}