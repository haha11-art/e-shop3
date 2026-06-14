package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 按分类查询在售商品（分页） */
    Page<Product> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    /** 查询所有在售商品（分页） */
    Page<Product> findByStatus(String status, Pageable pageable);

    /** 关键词搜索商品名称 */
    @Query("SELECT p FROM Product p WHERE p.status = 'ON_SALE' AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /** 查询热销商品 */
    List<Product> findTop10ByStatusOrderBySalesCountDesc(String status);

    /** 按分类查询商品列表 */
    List<Product> findByCategoryId(Long categoryId);
}
