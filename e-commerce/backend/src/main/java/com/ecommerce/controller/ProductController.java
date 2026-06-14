package com.ecommerce.controller;

import com.ecommerce.common.Result;
import com.ecommerce.service.ProductService;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器 - 商品列表、搜索、详情、分类
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 获取商品列表（分页）
     * GET /api/products?page=0&size=10&categoryId=1&sortBy=default
     */
    @GetMapping
    public Result<?> getProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "default") String sortBy) {
        return productService.getProductList(page, size, categoryId, sortBy);
    }

    /**
     * 搜索商品
     * GET /api/products/search?keyword=手机&page=0&size=10&sortBy=default
     */
    @GetMapping("/search")
    public Result<?> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "default") String sortBy) {
        return productService.searchProducts(keyword, page, size, sortBy);
    }

    /**
     * 获取商品详情
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public Result<?> getProductDetail(@PathVariable Long id) {
        return productService.getProductDetail(id);
    }

    /**
     * 获取热销商品
     * GET /api/products/hot
     */
    @GetMapping("/hot")
    public Result<?> getHotProducts() {
        return productService.getHotProducts();
    }

    /**
     * 获取商品分类列表
     * GET /api/products/categories
     */
    @GetMapping("/categories")
    public Result<?> getCategories() {
        return productService.getCategories();
    }

    /**
     * 获取子分类
     * GET /api/products/categories/{parentId}/children
     */
    @GetMapping("/categories/{parentId}/children")
    public Result<?> getSubCategories(@PathVariable Long parentId) {
        return productService.getSubCategories(parentId);
    }
}
