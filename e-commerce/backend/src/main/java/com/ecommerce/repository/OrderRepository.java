package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, String status, Pageable pageable);
    Optional<Order> findByOrderNo(String orderNo);
    Page<Order> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
