package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.security.services.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import com.petmanagement.petmanagementbackend.models.NfcTag;
import com.petmanagement.petmanagementbackend.models.NfcTagStatus;
import com.petmanagement.petmanagementbackend.models.Pet;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.NfcTagRepository;
import com.petmanagement.petmanagementbackend.repository.PetRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/nfc-tags")
public class NfcTagController {

    @Autowired
    NfcTagRepository nfcTagRepository;

    @Autowired
    PetRepository petRepository;

    // 获取所有NFC吊牌 (仅限管理员)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NfcTag>> getAllNfcTags() {
        try {
            List<NfcTag> nfcTags = nfcTagRepository.findAll();
            return new ResponseEntity<>(nfcTags, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据ID获取NFC吊牌 (仅限管理员)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NfcTag> getNfcTagById(@PathVariable("id") Long id) {
        Optional<NfcTag> nfcTagData = nfcTagRepository.findById(id);
        return nfcTagData.map(nfcTag -> new ResponseEntity<>(nfcTag, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 创建NFC吊牌 (仅限管理员)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNfcTag(@Valid @RequestBody NfcTag nfcTag) {
        if (nfcTagRepository.existsByTagUid(nfcTag.getTagUid())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: NFC Tag UID is already in use!"));
        }
        try {
            NfcTag _nfcTag = nfcTagRepository.save(new NfcTag(
                    nfcTag.getTagUid(),
                    null, // Initially not bound to any pet
                    nfcTag.getProtocolType(),
                    NfcTagStatus.UNBOUND,
                    null
            ));
            return new ResponseEntity<>(_nfcTag, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新NFC吊牌 (仅限管理员)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NfcTag> updateNfcTag(@PathVariable("id") Long id, @Valid @RequestBody NfcTag nfcTag) {
        Optional<NfcTag> nfcTagData = nfcTagRepository.findById(id);

        if (nfcTagData.isPresent()) {
            NfcTag _nfcTag = nfcTagData.get();
            _nfcTag.setTagUid(nfcTag.getTagUid());
            _nfcTag.setProtocolType(nfcTag.getProtocolType());
            _nfcTag.setStatus(nfcTag.getStatus());
            _nfcTag.setActivatedAt(nfcTag.getActivatedAt());
            return new ResponseEntity<>(nfcTagRepository.save(_nfcTag), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 删除NFC吊牌 (仅限管理员)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteNfcTag(@PathVariable("id") Long id) {
        try {
            nfcTagRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 绑定NFC吊牌到宠物 (管理员或宠物主人)
    @PostMapping("/bind/{tagId}/{petId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")  // ✅ 改为 USER
    public ResponseEntity<?> bindNfcTagToPet(@PathVariable("tagId") Long tagId, @PathVariable("petId") Long petId) {
        Optional<NfcTag> nfcTagData = nfcTagRepository.findById(tagId);
        Optional<Pet> petData = petRepository.findById(petId);

        if (nfcTagData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: NFC Tag not found!"), HttpStatus.NOT_FOUND);
        }
        if (petData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: Pet not found!"), HttpStatus.NOT_FOUND);
        }

        NfcTag nfcTag = nfcTagData.get();
        Pet pet = petData.get();

        // 检查NFC吊牌是否已被绑定
        if (nfcTag.getPetId() != null) {
            return new ResponseEntity<>(new MessageResponse("Error: NFC Tag is already bound to another pet!"), HttpStatus.BAD_REQUEST);
        }
        // 检查宠物是否已绑定NFC吊牌
        if (nfcTagRepository.findByPetId(petId).isPresent()) {
            return new ResponseEntity<>(new MessageResponse("Error: Pet is already bound to an NFC Tag!"), HttpStatus.BAD_REQUEST);
        }

        // 宠物主人只能绑定自己的宠物
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {  // ✅ 改为 ROLE_USER
            if (!pet.getOwnerId().equals(((UserDetailsImpl) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal()).getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        nfcTag.setPetId(pet.getId());
        nfcTag.setStatus(NfcTagStatus.BOUND);
        nfcTag.setActivatedAt(LocalDateTime.now());
        nfcTagRepository.save(nfcTag);

        return new ResponseEntity<>(new MessageResponse("NFC Tag bound to Pet successfully!"), HttpStatus.OK);
    }

    // 解绑NFC吊牌 (管理员或宠物主人)
    @PostMapping("/unbind/{tagId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")  // ✅ 改为 USER
    public ResponseEntity<?> unbindNfcTagFromPet(@PathVariable("tagId") Long tagId) {
        Optional<NfcTag> nfcTagData = nfcTagRepository.findById(tagId);

        if (nfcTagData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: NFC Tag not found!"), HttpStatus.NOT_FOUND);
        }

        NfcTag nfcTag = nfcTagData.get();

        if (nfcTag.getPetId() == null) {
            return new ResponseEntity<>(new MessageResponse("Error: NFC Tag is not bound to any pet!"), HttpStatus.BAD_REQUEST);
        }

        // 宠物主人只能解绑自己宠物上的NFC吊牌
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {  // ✅ 改为 ROLE_USER
            Optional<Pet> petData = petRepository.findById(nfcTag.getPetId());
            if (petData.isEmpty() || !petData.get().getOwnerId().equals(
                    ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        nfcTag.setPetId(null);
        nfcTag.setStatus(NfcTagStatus.UNBOUND);
        nfcTag.setActivatedAt(null);
        nfcTagRepository.save(nfcTag);

        return new ResponseEntity<>(new MessageResponse("NFC Tag unbound from Pet successfully!"), HttpStatus.OK);
    }

    // 根据NFC Tag UID获取宠物信息 (所有已认证用户)
    @GetMapping("/scan/{tagUid}")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")  // ✅ 改为实际角色
    public ResponseEntity<?> getPetByNfcTagUid(@PathVariable("tagUid") String tagUid) {
        Optional<NfcTag> nfcTagData = nfcTagRepository.findByTagUid(tagUid);

        if (nfcTagData.isEmpty() || nfcTagData.get().getPetId() == null) {
            return new ResponseEntity<>(new MessageResponse("Error: NFC Tag not found or not bound to any pet!"), HttpStatus.NOT_FOUND);
        }

        NfcTag nfcTag = nfcTagData.get();
        Optional<Pet> petData = petRepository.findById(nfcTag.getPetId());

        if (petData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: Bound pet not found!"), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(petData.get(), HttpStatus.OK);
    }
}