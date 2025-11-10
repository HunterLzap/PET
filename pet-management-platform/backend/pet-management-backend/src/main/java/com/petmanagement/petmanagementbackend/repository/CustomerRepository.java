package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // 根据商家ID查询所有客户
    List<Customer> findByMerchantId(Long merchantId);
    
    // 根据商家ID和手机号查询（防止重复）
    Optional<Customer> findByMerchantIdAndPhone(Long merchantId, String phone);
    
    // 根据用户ID查询（客户注册后绑定）
    Optional<Customer> findByUserId(Long userId);
    
    // 根据商家ID和状态查询
    List<Customer> findByMerchantIdAndStatus(Long merchantId, Integer status);
    
    // 搜索客户（姓名或手机号模糊匹配）
    List<Customer> findByMerchantIdAndNameContainingOrPhoneContaining(
        Long merchantId, String name, String phone
    );
}