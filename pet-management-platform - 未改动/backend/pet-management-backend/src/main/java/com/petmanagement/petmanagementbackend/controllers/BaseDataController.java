package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.BaseData;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.BaseDataRepository;
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

    // 创建基础数据 (仅限管理员)
    @PostMapping
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<?> createBaseData(@Valid @RequestBody BaseData baseData) {
        if (baseDataRepository.existsByTypeAndValue(baseData.getType(), baseData.getValue())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Base data with this type and value already exists!"));
        }
        try {
            BaseData _baseData = baseDataRepository.save(new BaseData(
                    baseData.getType(),
                    baseData.getValue(),
                    baseData.getDescription()
            ));
            return new ResponseEntity<>(_baseData, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新基础数据 (仅限管理员)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<BaseData> updateBaseData(@PathVariable("id") Long id, @Valid @RequestBody BaseData baseData) {
        Optional<BaseData> baseDataOptional = baseDataRepository.findById(id);

        if (baseDataOptional.isPresent()) {
            BaseData _baseData = baseDataOptional.get();
            _baseData.setType(baseData.getType());
            _baseData.setValue(baseData.getValue());
            _baseData.setDescription(baseData.getDescription());
            return new ResponseEntity<>(baseDataRepository.save(_baseData), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 删除基础数据 (仅限管理员)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<HttpStatus> deleteBaseData(@PathVariable("id") Long id) {
        try {
            baseDataRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

