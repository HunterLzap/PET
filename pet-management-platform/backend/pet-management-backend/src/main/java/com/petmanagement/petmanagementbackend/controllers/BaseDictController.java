package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.service.BaseDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 基础数据字典接口
 * 提供基础数据的查询API
 */
@RestController
@RequestMapping("/api/base-dict")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BaseDictController {
    
    private final BaseDictService baseDictService;
    
    /**
     * 获取字典值列表（所有端可调用）
     * GET /api/base-dict/values/pet_species
     * 
     * @param dictCode 字典编码
     * @return 字典值列表
     */
    @GetMapping("/values/{dictCode}")
    public ResponseEntity<List<Map<String, Object>>> getDictValues(
            @PathVariable String dictCode) {
        
        List<Map<String, Object>> values = baseDictService.getDictValues(dictCode);
        return ResponseEntity.ok(values);
    }
    
    /**
     * 根据父级查询子级（级联查询）
     * GET /api/base-dict/values/pet_breed/by-species/dog
     * 
     * @param dictCode 字典编码
     * @param speciesCode 父级编码
     * @return 子级字典值列表
     */
    @GetMapping("/values/{dictCode}/by-species/{speciesCode}")
    public ResponseEntity<List<Map<String, Object>>> getBreedsBySpecies(
            @PathVariable String dictCode,
            @PathVariable String speciesCode) {
        
        List<Map<String, Object>> values = baseDictService
            .getDictValuesBySpecies(dictCode, speciesCode);
        return ResponseEntity.ok(values);
    }
    
    /**
     * 获取单个字典值
     * GET /api/base-dict/value/pet_species/dog
     * 
     * @param dictCode 字典编码
     * @param valueCode 值编码
     * @return 字典值
     */
    @GetMapping("/value/{dictCode}/{valueCode}")
    public ResponseEntity<Map<String, Object>> getDictValue(
            @PathVariable String dictCode,
            @PathVariable String valueCode) {
        
        Map<String, Object> value = baseDictService.getDictValue(dictCode, valueCode);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }
    
    /**
     * 验证字典值是否有效
     * GET /api/base-dict/validate/pet_species/dog
     * 
     * @param dictCode 字典编码
     * @param valueCode 值编码
     * @return 验证结果
     */
    @GetMapping("/validate/{dictCode}/{valueCode}")
    public ResponseEntity<Map<String, Boolean>> validateDictValue(
            @PathVariable String dictCode,
            @PathVariable String valueCode) {
        
        boolean isValid = baseDictService.isValidDictValue(dictCode, valueCode);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    /**
     * 获取所有常用的基础数据（前端一次性加载）
     * GET /api/base-dict/all-common
     * 
     * @return 所有常用基础数据
     */
    @GetMapping("/all-common")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getAllCommonDict() {
        Map<String, List<Map<String, Object>>> result = Map.of(
            "petSpecies", baseDictService.getDictValues("pet_species"),
            "petGender", baseDictService.getDictValues("pet_gender"),
            "petSize", baseDictService.getDictValues("pet_size"),
            "medicalService", baseDictService.getDictValues("medical_service"),
            "fosterService", baseDictService.getDictValues("foster_service"),
            "beautyService", baseDictService.getDictValues("beauty_service"),
            "merchantType", baseDictService.getDictValues("merchant_type")
        );
        return ResponseEntity.ok(result);
    }
}