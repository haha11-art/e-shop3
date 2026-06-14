package com.ecommerce.service;

import com.ecommerce.common.Result;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务 - 商品展示、搜索、分类管理
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * 获取商品列表（分页）
     */
    public Result<?> getProductList(int page, int size, Long categoryId, String sortBy) {
        Sort sort = getSort(sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Product> productPage;
        if (categoryId != null) {
            productPage = productRepository.findByCategoryIdAndStatus(categoryId, "ON_SALE", pageRequest);
        } else {
            productPage = productRepository.findByStatus("ON_SALE", pageRequest);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("products", productPage.getContent());
        data.put("totalPages", productPage.getTotalPages());
        data.put("totalElements", productPage.getTotalElements());
        data.put("currentPage", page);
        return Result.success(data);
    }

    /**
     * 搜索商品
     */
    public Result<?> searchProducts(String keyword, int page, int size, String sortBy) {
        Sort sort = getSort(sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.searchByKeyword(keyword, pageRequest);

        Map<String, Object> data = new HashMap<>();
        data.put("products", productPage.getContent());
        data.put("totalPages", productPage.getTotalPages());
        data.put("totalElements", productPage.getTotalElements());
        data.put("currentPage", page);
        data.put("keyword", keyword);
        return Result.success(data);
    }

    /**
     * 获取商品详情
     */
    public Result<?> getProductDetail(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !"ON_SALE".equals(product.getStatus())) {
            return Result.error("商品不存在或已下架");
        }
        return Result.success(product);
    }

    /**
     * 获取热销商品
     */
    public Result<?> getHotProducts() {
        List<Product> products = productRepository.findTop10ByStatusOrderBySalesCountDesc("ON_SALE");
        return Result.success(products);
    }

    /**
     * 获取所有分类
     */
    public Result<?> getCategories() {
        List<Category> topCategories = categoryRepository.findByParentIdIsNullOrderBySortOrderAsc();
        return Result.success(topCategories);
    }

    /**
     * 获取子分类
     */
    public Result<?> getSubCategories(Long parentId) {
        List<Category> subCategories = categoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
        return Result.success(subCategories);
    }

    /**
     * 排序方式
     */
    private Sort getSort(String sortBy) {
        return switch (sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "sales" -> Sort.by(Sort.Direction.DESC, "salesCount");
            default -> Sort.by(Sort.Direction.DESC, "createTime");
        };
    }
}
