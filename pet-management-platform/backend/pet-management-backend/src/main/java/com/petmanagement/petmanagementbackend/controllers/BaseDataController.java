package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.BaseData;
import com.petmanagement.petmanagementbackend.models.BaseDataLog;
import com.petmanagement.petmanagementbackend.models.BaseDataVersion;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.BaseDataRepository;
import com.petmanagement.petmanagementbackend.service.BaseDataService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/base-data")
public class BaseDataController {

    @Autowired
    BaseDataRepository baseDataRepository;

    @Autowired
    private BaseDataService baseDataService;

    // 获取所有基础数据 (仅限管理员)
    @GetMapping
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<List<BaseData>> getAllBaseData() {
        try {
            List<BaseData> baseData = baseDataRepository.findAll();
            return new ResponseEntity<>(baseData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据类型获取基础数据 (所有用户可读)
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<List<BaseData>> getBaseDataByType(@PathVariable("type") String type) {
        try {
            List<BaseData> baseData = baseDataRepository.findByType(type);
            return new ResponseEntity<>(baseData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据ID获取基础数据 (仅限管理员)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<BaseData> getBaseDataById(@PathVariable("id") Long id) {
        Optional<BaseData> baseData = baseDataRepository.findById(id);
        return baseData.map(data -> new ResponseEntity<>(data, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 创建基础数据 (记录版本与日志)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBaseData(@Valid @RequestBody BaseData baseData, @RequestHeader(value = "X-User", required = false) String user) {
        try {
            BaseData saved = baseDataService.create(baseData, user != null ? user : "system", "手动创建");
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("创建失败: " + e.getMessage()));
        }
    }

    // 更新基础数据 (记录版本与日志)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBaseData(@PathVariable("id") Long id, @Valid @RequestBody BaseData baseData, @RequestHeader(value = "X-User", required = false) String user) {
        try {
            BaseData saved = baseDataService.update(id, baseData, user != null ? user : "system", "手动编辑");
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("更新失败: " + e.getMessage()));
        }
    }

    // 禁用基础数据 (逻辑)
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableBaseData(@PathVariable("id") Long id, @RequestHeader(value = "X-User", required = false) String user) {
        try {
            baseDataService.disable(id, user != null ? user : "system");
            return ResponseEntity.ok(new MessageResponse("已禁用"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("禁用失败: " + e.getMessage()));
        }
    }

    // 删除（写入日志/快照）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBaseData(@PathVariable("id") Long id, @RequestHeader(value = "X-User", required = false) String user) {
        try {
            baseDataService.delete(id, user != null ? user : "system");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("删除失败: " + e.getMessage()));
        }
    }

    // 查询版本历史
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BaseDataVersion>> getVersions(@PathVariable("id") Long id) {
        return ResponseEntity.ok(baseDataService.getVersions(id));
    }

    // 查询操作日志
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BaseDataLog>> getLogs(@PathVariable("id") Long id) {
        return ResponseEntity.ok(baseDataService.getLogs(id));
    }

    // 回滚到版本
    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rollbackToVersion(@PathVariable("id") Long id, @RequestParam Integer version, @RequestHeader(value = "X-User", required = false) String user) {
        try {
            BaseData rolled = baseDataService.rollbackToVersion(id, version, user != null ? user : "system");
            return ResponseEntity.ok(rolled);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("回滚失败: " + e.getMessage()));
        }
    }
}

