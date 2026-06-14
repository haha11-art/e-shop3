package com.ecommerce.service;

import com.ecommerce.common.Result;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 购物车服务 - 添加/修改/删除/查询购物车
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * 添加商品到购物车
     */
    @Transactional
    public Result<?> addToCart(Long userId, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !"ON_SALE".equals(product.getStatus())) {
            return Result.error("商品不存在或已下架");
        }
        if (product.getStock() < quantity) {
            return Result.error("库存不足");
        }

        // 检查购物车是否已有该商品
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            User user = userRepository.findById(userId).orElseThrow();
            CartItem cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(quantity)
                    .selected(true)
                    .build();
            cartItemRepository.save(cartItem);
        }
        return Result.success("添加成功", null);
    }

    /**
     * 获取购物车列表
     */
    public Result<?> getCartList(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        int selectedCount = 0;

        for (CartItem item : items) {
            if (item.getSelected()) {
                totalAmount = totalAmount.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                selectedCount += item.getQuantity();
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("totalAmount", totalAmount);
        data.put("selectedCount", selectedCount);
        data.put("totalCount", cartItemRepository.countByUserId(userId));
        return Result.success(data);
    }

    /**
     * 更新购物车商品数量
     */
    @Transactional
    public Result<?> updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !item.getUser().getId().equals(userId)) {
            return Result.error("购物车项不存在");
        }
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStock() < quantity) {
                return Result.error("库存不足");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return Result.success("更新成功", null);
    }

    /**
     * 删除购物车商品
     */
    @Transactional
    public Result<?> removeFromCart(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !item.getUser().getId().equals(userId)) {
            return Result.error("购物车项不存在");
        }
        cartItemRepository.delete(item);
        return Result.success("删除成功", null);
    }

    /**
     * 切换选中状态
     */
    @Transactional
    public Result<?> toggleSelected(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !item.getUser().getId().equals(userId)) {
            return Result.error("购物车项不存在");
        }
        item.setSelected(!item.getSelected());
        cartItemRepository.save(item);
        return Result.success(item);
    }

    /**
     * 获取选中的购物车项（用于下单）
     */
    public List<CartItem> getSelectedItems(Long userId) {
        return cartItemRepository.findByUserIdAndSelectedTrue(userId);
    }

    /**
     * 清空购物车
     */
    @Transactional
    public Result<?> clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        return Result.success("购物车已清空", null);
    }
}
