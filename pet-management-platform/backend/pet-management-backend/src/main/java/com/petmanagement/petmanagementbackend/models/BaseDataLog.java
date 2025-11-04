package com.petmanagement.petmanagementbackend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "base_data_log")
public class BaseDataLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_data_id")
    private BaseData baseData;

    @Column(length=32)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(length=64)
    private String operatedBy;
    private LocalDateTime operatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BaseData getBaseData() { return baseData; }
    public void setBaseData(BaseData baseData) { this.baseData = baseData; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getOperatedBy() { return operatedBy; }
    public void setOperatedBy(String operatedBy) { this.operatedBy = operatedBy; }
    public LocalDateTime getOperatedAt() { return operatedAt; }
    public void setOperatedAt(LocalDateTime operatedAt) { this.operatedAt = operatedAt; }
}
