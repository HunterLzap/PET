package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.HealthRecord;
import com.petmanagement.petmanagementbackend.models.Pet;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.HealthRecordRepository;
import com.petmanagement.petmanagementbackend.repository.PetRepository;
import com.petmanagement.petmanagementbackend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    @Autowired
    HealthRecordRepository healthRecordRepository;

    @Autowired
    PetRepository petRepository;

    // 获取宠物的所有健康记录 (宠物主人只能获取自己的宠物的记录，管理员可以获取所有)
    @GetMapping("/pet/{petId}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<List<HealthRecord>> getHealthRecordsByPetId(@PathVariable("petId") Long petId) {
        try {
            Optional<Pet> petData = petRepository.findById(petId);
            if (petData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Pet pet = petData.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 宠物主人只能查看自己宠物的健康记录
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!pet.getOwnerId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            List<HealthRecord> healthRecords = healthRecordRepository.findByPetId(petId);
            return new ResponseEntity<>(healthRecords, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据ID获取健康记录 (宠物主人只能获取自己的宠物的记录，管理员可以获取所有)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<HealthRecord> getHealthRecordById(@PathVariable("id") Long id) {
        Optional<HealthRecord> healthRecordData = healthRecordRepository.findById(id);

        if (healthRecordData.isPresent()) {
            HealthRecord healthRecord = healthRecordData.get();
            Optional<Pet> petData = petRepository.findById(healthRecord.getPetId());
            if (petData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Pet pet = petData.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 宠物主人只能查看自己宠物的健康记录
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!pet.getOwnerId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            return new ResponseEntity<>(healthRecord, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 创建健康记录 (宠物主人只能为自己的宠物创建，管理员可以为任何宠物创建)
    @PostMapping
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<?> createHealthRecord(@Valid @RequestBody HealthRecord healthRecord) {
        try {
            Optional<Pet> petData = petRepository.findById(healthRecord.getPetId());
            if (petData.isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Error: Pet not found!"), HttpStatus.BAD_REQUEST);
            }
            Pet pet = petData.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 宠物主人只能为自己的宠物创建健康记录
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!pet.getOwnerId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            HealthRecord _healthRecord = healthRecordRepository.save(new HealthRecord(
                    healthRecord.getPetId(),
                    healthRecord.getRecordDate(),
                    healthRecord.getRecordType(),
                    healthRecord.getDescription(),
                    healthRecord.getNotes(),
                    healthRecord.getAttachmentUrl()
            ));
            return new ResponseEntity<>(_healthRecord, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新健康记录 (宠物主人只能更新自己的宠物的记录，管理员可以更新所有)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<HealthRecord> updateHealthRecord(@PathVariable("id") Long id, @Valid @RequestBody HealthRecord healthRecord) {
        Optional<HealthRecord> healthRecordData = healthRecordRepository.findById(id);

        if (healthRecordData.isPresent()) {
            HealthRecord _healthRecord = healthRecordData.get();
            Optional<Pet> petData = petRepository.findById(_healthRecord.getPetId());
            if (petData.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Pet pet = petData.get();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 宠物主人只能更新自己宠物的健康记录
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!pet.getOwnerId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            _healthRecord.setRecordDate(healthRecord.getRecordDate());
            _healthRecord.setRecordType(healthRecord.getRecordType());
            _healthRecord.setDescription(healthRecord.getDescription());
            _healthRecord.setNotes(healthRecord.getNotes());
            _healthRecord.setAttachmentUrl(healthRecord.getAttachmentUrl());
            return new ResponseEntity<>(healthRecordRepository.save(_healthRecord), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 删除健康记录 (宠物主人只能删除自己的宠物的记录，管理员可以删除所有)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<HttpStatus> deleteHealthRecord(@PathVariable("id") Long id) {
        try {
            Optional<HealthRecord> healthRecordData = healthRecordRepository.findById(id);
            if (healthRecordData.isPresent()) {
                HealthRecord healthRecord = healthRecordData.get();
                Optional<Pet> petData = petRepository.findById(healthRecord.getPetId());
                if (petData.isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                Pet pet = petData.get();

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                // 宠物主人只能删除自己宠物的健康记录
                if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                    if (!pet.getOwnerId().equals(userDetails.getId())) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                }
                healthRecordRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

