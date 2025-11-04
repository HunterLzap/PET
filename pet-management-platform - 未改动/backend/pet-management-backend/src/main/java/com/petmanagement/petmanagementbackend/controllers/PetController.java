package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.Pet;
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
@RequestMapping("/api/pets")
public class PetController {

    @Autowired
    PetRepository petRepository;

    // 获取当前用户的所有宠物
    @GetMapping
    @PreAuthorize("hasRole('PET_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<Pet>> getAllPetsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        List<Pet> pets = petRepository.findByOwnerId(currentUserId);
        return new ResponseEntity<>(pets, HttpStatus.OK);
    }

    // 根据ID获取宠物
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PET_OWNER') or hasRole('BUSINESS') or hasRole('ADMIN')")
    public ResponseEntity<Pet> getPetById(@PathVariable("id") Long id) {
        Optional<Pet> petData = petRepository.findById(id);

        if (petData.isPresent()) {
            Pet pet = petData.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 宠物主人只能查看自己的宠物
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!pet.getOwnerId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            // 商家和管理员可以查看所有宠物 (后续可根据业务关系进一步细化)
            return new ResponseEntity<>(pet, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 创建宠物
    @PostMapping
    @PreAuthorize("hasRole('PET_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Pet> createPet(@Valid @RequestBody Pet pet) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long currentUserId = userDetails.getId();

            // 宠物主人只能为自己创建宠物
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                pet.setOwnerId(currentUserId);
            }
            // 管理员可以为指定ownerId创建宠物

            Pet _pet = petRepository.save(new Pet(pet.getOwnerId(), pet.getName(), pet.getSpecies(), pet.getBreedId(), pet.getGender(), pet.getBirthday(), pet.getWeight(), pet.getColor(), pet.getDescription(), pet.getAvatarUrl()));
            return new ResponseEntity<>(_pet, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新宠物
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PET_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Pet> updatePet(@PathVariable("id") Long id, @Valid @RequestBody Pet pet) {
        Optional<Pet> petData = petRepository.findById(id);

        if (petData.isPresent()) {
            Pet _pet = petData.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 宠物主人只能更新自己的宠物
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!_pet.getOwnerId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            _pet.setName(pet.getName());
            _pet.setSpecies(pet.getSpecies());
            _pet.setBreedId(pet.getBreedId());
            _pet.setGender(pet.getGender());
            _pet.setBirthday(pet.getBirthday());
            _pet.setWeight(pet.getWeight());
            _pet.setColor(pet.getColor());
            _pet.setDescription(pet.getDescription());
            _pet.setAvatarUrl(pet.getAvatarUrl());
            return new ResponseEntity<>(petRepository.save(_pet), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 删除宠物
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PET_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deletePet(@PathVariable("id") Long id) {
        try {
            Optional<Pet> petData = petRepository.findById(id);
            if (petData.isPresent()) {
                Pet pet = petData.get();
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                // 宠物主人只能删除自己的宠物
                if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                    if (!pet.getOwnerId().equals(userDetails.getId())) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                }
                petRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
