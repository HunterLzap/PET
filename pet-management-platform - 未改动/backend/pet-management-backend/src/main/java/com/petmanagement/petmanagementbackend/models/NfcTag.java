package com.petmanagement.petmanagementbackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "nfc_tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = "tag_uid"),
        @UniqueConstraint(columnNames = "pet_id")
})
public class NfcTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "tag_uid", nullable = false)
    private String tagUid; // NFC tag unique identifier

    @Column(name = "pet_id")
    private Long petId; // Foreign key to Pet, can be null if not bound

    @NotBlank
    @Size(max = 20)
    @Column(name = "protocol_type", nullable = false)
    private String protocolType; // e.g., ISO14443, ISO15693

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private NfcTagStatus status; // e.g., ACTIVE, INACTIVE, LOST

    private LocalDateTime activatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public NfcTag() {
    }

    public NfcTag(String tagUid, Long petId, String protocolType, NfcTagStatus status, LocalDateTime activatedAt) {
        this.tagUid = tagUid;
        this.petId = petId;
        this.protocolType = protocolType;
        this.status = status;
        this.activatedAt = activatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTagUid() {
        return tagUid;
    }

    public void setTagUid(String tagUid) {
        this.tagUid = tagUid;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

    public NfcTagStatus getStatus() {
        return status;
    }

    public void setStatus(NfcTagStatus status) {
        this.status = status;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

