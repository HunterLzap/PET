package com.petmanagement.petmanagementbackend.repository;

import com.petmanagement.petmanagementbackend.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByBusinessId(Long businessId);
    List<Order> findByPetId(Long petId);
}

