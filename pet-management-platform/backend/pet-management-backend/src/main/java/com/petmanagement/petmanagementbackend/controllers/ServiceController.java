package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.Service;
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
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    ServiceRepository serviceRepository;

    // ✅ 获取所有服务 (所有已认证用户都可以查看服务列表，用于创建订单)
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<List<Service>> getAllServices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 管理员可以获取所有服务
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            List<Service> services = serviceRepository.findAll();
            return new ResponseEntity<>(services, HttpStatus.OK);
        } 
        // 商家获取自己的服务
        else if (authentication.getAuthorities().stream().anyMatch(a -> 
                a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
                a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
                a.getAuthority().equals("ROLE_MERCHANT_GOODS"))) {
            List<Service> services = serviceRepository.findByBusinessId(userDetails.getId());
            return new ResponseEntity<>(services, HttpStatus.OK);
        } 
        // 普通用户获取所有服务（用于下单）
        else {
            List<Service> services = serviceRepository.findAll();
            return new ResponseEntity<>(services, HttpStatus.OK);
        }
    }

    // 根据ID获取服务
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<Service> getServiceById(@PathVariable("id") Long id) {
        Optional<Service> serviceData = serviceRepository.findById(id);

        if (serviceData.isPresent()) {
            Service service = serviceData.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 商家只能查看自己的服务详情（编辑时）
            if (authentication.getAuthorities().stream().anyMatch(a -> 
                    a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
                    a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
                    a.getAuthority().equals("ROLE_MERCHANT_GOODS"))) {
                if (!service.getBusinessId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }
            return new ResponseEntity<>(service, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 创建服务 (仅限商家和管理员)
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<Service> createService(@Valid @RequestBody Service service) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 商家只能创建自己的服务
            if (authentication.getAuthorities().stream().anyMatch(a -> 
                    a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
                    a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
                    a.getAuthority().equals("ROLE_MERCHANT_GOODS"))) {
                service.setBusinessId(userDetails.getId());
            }
            // 管理员可以为指定businessId创建服务

            Service _service = serviceRepository.save(new Service(
                    service.getBusinessId(),
                    service.getName(),
                    service.getType(),
                    service.getPrice(),
                    service.getDescription(),
                    service.getDurationMinutes()
            ));
            return new ResponseEntity<>(_service, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新服务
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<Service> updateService(@PathVariable("id") Long id, @Valid @RequestBody Service service) {
        Optional<Service> serviceData = serviceRepository.findById(id);

        if (serviceData.isPresent()) {
            Service _service = serviceData.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 商家只能更新自己的服务
            if (authentication.getAuthorities().stream().anyMatch(a -> 
                    a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
                    a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
                    a.getAuthority().equals("ROLE_MERCHANT_GOODS"))) {
                if (!_service.getBusinessId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            _service.setName(service.getName());
            _service.setType(service.getType());
            _service.setPrice(service.getPrice());
            _service.setDescription(service.getDescription());
            _service.setDurationMinutes(service.getDurationMinutes());
            return new ResponseEntity<>(serviceRepository.save(_service), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 删除服务
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteService(@PathVariable("id") Long id) {
        try {
            Optional<Service> serviceData = serviceRepository.findById(id);
            if (serviceData.isPresent()) {
                Service service = serviceData.get();
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                // 商家只能删除自己的服务
                if (authentication.getAuthorities().stream().anyMatch(a -> 
                        a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
                        a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
                        a.getAuthority().equals("ROLE_MERCHANT_GOODS"))) {
                    if (!service.getBusinessId().equals(userDetails.getId())) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                }
                serviceRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}