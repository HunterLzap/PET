package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.NfcTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NfcTagRepository extends JpaRepository<NfcTag, Long> {
    Optional<NfcTag> findByTagUid(String tagUid);
    Optional<NfcTag> findByPetId(Long petId);
    boolean existsByTagUid(String tagUid);
}

