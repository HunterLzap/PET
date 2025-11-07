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

/**
 * 宠物管理 API
 * 用户端接口，需要登录
 */
@RestController
@RequestMapping("/api/user/pets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PetController {
    
    private final PetService petService;
    
    /**
     * 获取我的宠物列表
     * GET /api/user/pets
     * 
     * @param authentication 当前登录用户
     * @return 宠物列表
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<PetResponse>> getMyPets(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<PetResponse> pets = petService.getMyPets(userId);
        return ResponseEntity.ok(pets);
    }
    
    /**
     * 获取宠物详情
     * GET /api/user/pets/{petId}
     * 
     * @param petId 宠物ID
     * @param authentication 当前登录用户
     * @return 宠物详情
     */
    @GetMapping("/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PetResponse> getPetDetail(
            @PathVariable Long petId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        PetResponse pet = petService.getPetDetail(userId, petId);
        return ResponseEntity.ok(pet);
    }
    
    /**
     * 添加宠物
     * POST /api/user/pets
     * 
     * @param request 添加宠物请求
     * @param authentication 当前登录用户
     * @return 新添加的宠物信息
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PetResponse> addPet(
            @Valid @RequestBody AddPetRequest request,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        PetResponse pet = petService.addPet(userId, request);
        return ResponseEntity.ok(pet);
    }
    
    /**
     * 更新宠物信息
     * PUT /api/user/pets/{petId}
     * 
     * @param petId 宠物ID
     * @param request 更新请求
     * @param authentication 当前登录用户
     * @return 更新后的宠物信息
     */
    @PutMapping("/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
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
     * DELETE /api/user/pets/{petId}
     * 
     * @param petId 宠物ID
     * @param authentication 当前登录用户
     * @return 删除成功消息
     */
    @DeleteMapping("/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePet(
            @PathVariable Long petId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        petService.deletePet(userId, petId);
        return ResponseEntity.ok(new MessageResponse("宠物删除成功"));
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}