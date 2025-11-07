package com.petmanagement.petmanagementbackend.service;

import com.petmanagement.petmanagementbackend.models.BaseDictValue;
import com.petmanagement.petmanagementbackend.repository.BaseDictValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基础数据字典服务
 * 提供基础数据的查询功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaseDictService {
    
    private final BaseDictValueRepository dictValueRepository;
    
    /**
     * 获取字典值列表（带缓存）
     * @param dictCode 字典编码，如：pet_species, pet_breed
     * @return 字典值列表
     */
    @Cacheable(value = "dictValues", key = "#dictCode")
    public List<Map<String, Object>> getDictValues(String dictCode) {
        log.info("从数据库查询字典值: {}", dictCode);
        
        List<BaseDictValue> values = dictValueRepository
            .findByDictCodeAndStatus(dictCode, 1);
        
        return values.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }
    
    /**
     * 根据父级查询子级（级联）
     * 例如：根据宠物种类查询品种
     * @param dictCode 字典编码，如：pet_breed
     * @param speciesCode 父级编码，如：dog
     * @return 子级字典值列表
     */
    @Cacheable(value = "dictValues", key = "#dictCode + '_' + #speciesCode")
    public List<Map<String, Object>> getDictValuesBySpecies(String dictCode, String speciesCode) {
        log.info("查询字典值: dictCode={}, speciesCode={}", dictCode, speciesCode);
        
        List<BaseDictValue> values = dictValueRepository
            .findByDictCodeAndSpecies(dictCode, speciesCode);
        
        return values.stream()
            .map(this::convertToMap)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取单个字典值
     * @param dictCode 字典编码
     * @param valueCode 值编码
     * @return 字典值
     */
    public Map<String, Object> getDictValue(String dictCode, String valueCode) {
        return dictValueRepository
            .findByDictCodeAndValueCode(dictCode, valueCode)
            .map(this::convertToMap)
            .orElse(null);
    }
    
    /**
     * 验证字典值是否有效
     * @param dictCode 字典编码
     * @param valueCode 值编码
     * @return 是否有效
     */
    public boolean isValidDictValue(String dictCode, String valueCode) {
        return dictValueRepository.existsByDictCodeAndValueCode(dictCode, valueCode);
    }
    
    /**
     * 获取字典值名称
     * @param dictCode 字典编码
     * @param valueCode 值编码
     * @return 值名称
     */
    public String getDictValueName(String dictCode, String valueCode) {
        return dictValueRepository
            .findByDictCodeAndValueCode(dictCode, valueCode)
            .map(BaseDictValue::getValueName)
            .orElse(null);
    }
    
    /**
     * 转换为Map（前端友好的格式）
     */
    private Map<String, Object> convertToMap(BaseDictValue value) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", value.getId());
        map.put("dictCode", value.getDictCode());
        map.put("valueCode", value.getValueCode());
        map.put("valueName", value.getValueName());
        map.put("valueOrder", value.getValueOrder());
        map.put("extraData", value.getExtraData());
        map.put("colorTag", value.getColorTag());
        map.put("icon", value.getIcon());
        map.put("status", value.getStatus());
        return map;
    }
}