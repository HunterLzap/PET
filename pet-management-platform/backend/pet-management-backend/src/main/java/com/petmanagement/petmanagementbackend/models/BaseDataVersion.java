package com.petmanagement.petmanagementbackend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "base_data_version")
public class BaseDataVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_data_id")
    private BaseData baseData;

    @Column(columnDefinition = "TEXT")
    private String snapshot;

    private Integer version;

    @Column(length=32)
    private String action; // 新增、编辑、禁用、导入、回滚

    @Column(length=64)
    private String operatedBy;
    private LocalDateTime operatedAt;
    @Column(length=255)
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BaseData getBaseData() { return baseData; }
    public void setBaseData(BaseData baseData) { this.baseData = baseData; }
    public String getSnapshot() { return snapshot; }
    public void setSnapshot(String snapshot) { this.snapshot = snapshot; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOperatedBy() { return operatedBy; }
    public void setOperatedBy(String operatedBy) { this.operatedBy = operatedBy; }
    public LocalDateTime getOperatedAt() { return operatedAt; }
    public void setOperatedAt(LocalDateTime operatedAt) { this.operatedAt = operatedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
