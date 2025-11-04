package com.petmanagement.petmanagementbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petmanagement.petmanagementbackend.models.BaseData;
import com.petmanagement.petmanagementbackend.models.BaseDataLog;
import com.petmanagement.petmanagementbackend.models.BaseDataVersion;
import com.petmanagement.petmanagementbackend.repository.BaseDataLogRepository;
import com.petmanagement.petmanagementbackend.repository.BaseDataRepository;
import com.petmanagement.petmanagementbackend.repository.BaseDataVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BaseDataService {
    @Autowired
    private BaseDataRepository baseDataRepository;
    @Autowired
    private BaseDataVersionRepository baseDataVersionRepository;
    @Autowired
    private BaseDataLogRepository baseDataLogRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public BaseData create(BaseData entity, String username, String actionRemark) {
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setCreatedBy(username);
        entity.setUpdatedBy(username);
        entity.setVersion(1);
        BaseData saved = baseDataRepository.save(entity);
        saveVersionAndLog(saved, "CREATE", username, actionRemark);
        return saved;
    }

    @Transactional
    public BaseData update(Long id, BaseData update, String username, String actionRemark) {
        Optional<BaseData> opt = baseDataRepository.findById(id);
        if (opt.isPresent()) {
            BaseData entity = opt.get();
            entity.setType(update.getType());
            entity.setValue(update.getValue());
            entity.setDescription(update.getDescription());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setUpdatedBy(username);
            entity.setVersion(entity.getVersion() == null ? 2 : entity.getVersion() + 1);
            BaseData saved = baseDataRepository.save(entity);
            saveVersionAndLog(saved, "UPDATE", username, actionRemark);
            return saved;
        }
        throw new RuntimeException("数据未找到");
    }

    @Transactional
    public void disable(Long id, String username) {
        Optional<BaseData> opt = baseDataRepository.findById(id);
        if (opt.isPresent()) {
            BaseData entity = opt.get();
            entity.setDisabledAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setUpdatedBy(username);
            entity.setVersion(entity.getVersion() == null ? 2 : entity.getVersion() + 1);
            baseDataRepository.save(entity);
            saveVersionAndLog(entity, "DISABLE", username, "");
        } else {
            throw new RuntimeException("数据未找到");
        }
    }

    @Transactional
    public void delete(Long id, String username) {
        Optional<BaseData> opt = baseDataRepository.findById(id);
        opt.ifPresent(entity -> {
            saveVersionAndLog(entity, "DELETE", username, "");
            baseDataRepository.deleteById(id);
        });
    }

    public List<BaseDataVersion> getVersions(Long id) {
        return baseDataVersionRepository.findByBaseDataIdOrderByVersionDesc(id);
    }

    public List<BaseDataLog> getLogs(Long id) {
        return baseDataLogRepository.findByBaseDataIdOrderByOperatedAtDesc(id);
    }

    @Transactional
    public BaseData rollbackToVersion(Long baseDataId, Integer version, String username) {
        List<BaseDataVersion> versions = baseDataVersionRepository.findByBaseDataIdOrderByVersionDesc(baseDataId);
        for (BaseDataVersion v : versions) {
            if (v.getVersion().equals(version)) {
                try {
                    BaseData baseData = objectMapper.readValue(v.getSnapshot(), BaseData.class);
                    baseData.setId(baseDataId);
                    baseData.setUpdatedAt(LocalDateTime.now());
                    baseData.setUpdatedBy(username);
                    baseData.setVersion(version);
                    BaseData saved = baseDataRepository.save(baseData);
                    saveVersionAndLog(saved, "ROLLBACK", username, "回滚到版本: " + version);
                    return saved;
                } catch (Exception e) {
                    throw new RuntimeException("回滚失败: " + e.getMessage());
                }
            }
        }
        throw new RuntimeException("不存在此版本");
    }

    private void saveVersionAndLog(BaseData baseData, String action, String username, String remark) {
        try {
            String snapshot = objectMapper.writeValueAsString(baseData);
            BaseDataVersion version = new BaseDataVersion();
            version.setBaseData(baseData);
            version.setSnapshot(snapshot);
            version.setVersion(baseData.getVersion());
            version.setAction(action);
            version.setOperatedBy(username);
            version.setOperatedAt(LocalDateTime.now());
            version.setRemark(remark);
            baseDataVersionRepository.save(version);

            BaseDataLog log = new BaseDataLog();
            log.setBaseData(baseData);
            log.setAction(action);
            log.setOperatedBy(username);
            log.setOperatedAt(LocalDateTime.now());
            log.setDetail(remark);
            baseDataLogRepository.save(log);
        } catch (Exception e) {
            throw new RuntimeException("写入版本/日志失败: " + e.getMessage());
        }
    }
}
