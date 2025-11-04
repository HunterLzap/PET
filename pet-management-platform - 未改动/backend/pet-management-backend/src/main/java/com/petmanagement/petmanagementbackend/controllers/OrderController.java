package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.*;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.NfcTagRepository;
import com.petmanagement.petmanagementbackend.repository.OrderRepository;
import com.petmanagement.petmanagementbackend.repository.PetRepository;
import com.petmanagement.petmanagementbackend.repository.ServiceRepository;
import com.petmanagement.petmanagementbackend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PetRepository petRepository;

    @Autowired
    NfcTagRepository nfcTagRepository;

    @Autowired
    ServiceRepository serviceRepository;

    // 获取当前用户的所有订单 (宠物主人获取自己的，商家获取与自己相关的，管理员获取所有)
    @GetMapping
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<List<Order>> getAllOrdersForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            List<Order> orders = orderRepository.findAll();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
            List<Order> orders = orderRepository.findByUserId(currentUserId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"))) {
            List<Order> orders = orderRepository.findByBusinessId(currentUserId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    // 根据ID获取订单
    @GetMapping("/{id}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<Order> getOrderById(@PathVariable("id") Long id) {
        Optional<Order> orderData = orderRepository.findById(id);

        if (orderData.isPresent()) {
            Order order = orderData.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!order.getUserId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"))) {
                if (!order.getBusinessId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            return new ResponseEntity<>(order, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 创建订单 (仅限宠物主人)
    @PostMapping
    @PreAuthorize("hasRole(\'PET_OWNER\')")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long currentUserId = userDetails.getId();

            // 宠物主人只能为自己创建订单
            order.setUserId(currentUserId);

            // 验证服务是否存在
            if (order.getServiceId() != null && serviceRepository.findById(order.getServiceId()).isEmpty()) {
                return new ResponseEntity(new MessageResponse("Error: Service not found!"), HttpStatus.BAD_REQUEST);
            }

            // 验证宠物是否存在且属于当前用户
            if (order.getPetId() != null) {
                Optional<Pet> pet = petRepository.findById(order.getPetId());
                if (pet.isEmpty() || !pet.get().getOwnerId().equals(currentUserId)) {
                    return new ResponseEntity(new MessageResponse("Error: Pet not found or does not belong to current user!"), HttpStatus.BAD_REQUEST);
                }
            }

            Order _order = orderRepository.save(new Order(
                    order.getUserId(),
                    order.getBusinessId(),
                    order.getServiceId(),
                    order.getPetId(),
                    order.getTotalAmount(),
                    OrderStatus.PENDING,
                    PaymentStatus.PENDING,
                    order.getAppointmentTime(),
                    order.getRemarks()
            ));
            return new ResponseEntity<>(_order, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新订单 (宠物主人只能更新自己的，商家只能更新自己相关的，管理员可以更新所有)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'BUSINESS\') or hasRole(\'ADMIN\')")
    public ResponseEntity<Order> updateOrder(@PathVariable("id") Long id, @Valid @RequestBody Order order) {
        Optional<Order> orderData = orderRepository.findById(id);

        if (orderData.isPresent()) {
            Order _order = orderData.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                if (!_order.getUserId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_BUSINESS"))) {
                if (!_order.getBusinessId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            _order.setServiceId(order.getServiceId());
            _order.setPetId(order.getPetId());
            _order.setTotalAmount(order.getTotalAmount());
            _order.setStatus(order.getStatus());
            _order.setPaymentStatus(order.getPaymentStatus());
            _order.setAppointmentTime(order.getAppointmentTime());
            _order.setRemarks(order.getRemarks());
            return new ResponseEntity<>(orderRepository.save(_order), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 删除订单 (宠物主人只能删除自己的，管理员可以删除所有)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(\'PET_OWNER\') or hasRole(\'ADMIN\')")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable("id") Long id) {
        try {
            Optional<Order> orderData = orderRepository.findById(id);
            if (orderData.isPresent()) {
                Order order = orderData.get();
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PET_OWNER"))) {
                    if (!order.getUserId().equals(userDetails.getId())) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                }
                orderRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 商家通过NFC Tag UID关联订单 (商家)
    @GetMapping("/scan-nfc/{tagUid}")
    @PreAuthorize("hasRole(\'BUSINESS\')")
    public ResponseEntity<?> getOrdersByNfcTagUid(@PathVariable("tagUid") String tagUid) {
        Optional<NfcTag> nfcTagData = nfcTagRepository.findByTagUid(tagUid);

        if (nfcTagData.isEmpty() || nfcTagData.get().getPetId() == null) {
            return new ResponseEntity<>(new MessageResponse("Error: NFC Tag not found or not bound to any pet!"), HttpStatus.NOT_FOUND);
        }

        NfcTag nfcTag = nfcTagData.get();
        Long petId = nfcTag.getPetId();

        // 获取当前商家ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentBusinessId = userDetails.getId();

        // 查找与该宠物和当前商家相关的订单
        List<Order> orders = orderRepository.findByPetId(petId).stream()
                .filter(order -> order.getBusinessId().equals(currentBusinessId))
                .collect(java.util.stream.Collectors.toList());

        if (orders.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("No orders found for this pet with current business."), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
}

