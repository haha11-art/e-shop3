package com.ecommerce.mp.controller;

import com.ecommerce.mp.entity.MpOrder;
import com.ecommerce.mp.entity.MpOrderItem;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MyBatis Plus 订单 CRUD 接口集成测试
 * 测试全部 7 个接口：创建、查询详情、按用户分页查询、查询全部、更新状态、更新信息、删除
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MpOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** 记录创建成功的订单ID，供后续测试使用 */
    private static Long createdOrderId;
    private static String createdOrderNo;

    /**
     * 测试1：创建订单 (POST /api/mp/orders)
     */
    @Test
    @Order(1)
    public void test01_CreateOrder() throws Exception {
        MpOrder order = new MpOrder();
        order.setUserId(2L);
        order.setTotalAmount(new BigDecimal("19998.00"));
        order.setPayAmount(new BigDecimal("19998.00"));
        order.setFreight(BigDecimal.ZERO);
        order.setShippingAddress("上海市浦东新区张江高科技园区");
        order.setReceiverName("测试用户");
        order.setReceiverPhone("13800000001");
        order.setRemark("测试订单-MyBatis Plus");

        List<MpOrderItem> items = new ArrayList<>();
        MpOrderItem item1 = new MpOrderItem();
        item1.setProductId(1L);
        item1.setProductName("iPhone 15 Pro Max 256GB");
        item1.setProductImage("https://via.placeholder.com/400x400?text=iPhone15");
        item1.setUnitPrice(new BigDecimal("9999.00"));
        item1.setQuantity(2);
        item1.setTotalPrice(new BigDecimal("19998.00"));
        items.add(item1);
        order.setOrderItems(items);

        MvcResult result = mockMvc.perform(post("/api/mp/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.orderNo").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        // 保存订单ID供后续使用
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        createdOrderId = root.get("data").get("id").asLong();
        createdOrderNo = root.get("data").get("orderNo").asText();

        System.out.println("[测试1-创建订单] 成功, orderId=" + createdOrderId + ", orderNo=" + createdOrderNo);
    }

    /**
     * 测试2：查询订单详情 (GET /api/mp/orders/{id})
     */
    @Test
    @Order(2)
    public void test02_GetOrderById() throws Exception {
        Assertions.assertNotNull(createdOrderId, "订单ID不能为空，请先运行创建订单测试");

        MvcResult result = mockMvc.perform(get("/api/mp/orders/" + createdOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(createdOrderId))
                .andExpect(jsonPath("$.data.orderNo").value(createdOrderNo))
                .andExpect(jsonPath("$.data.orderItems").isArray())
                .andExpect(jsonPath("$.data.orderItems[0].productName").value("iPhone 15 Pro Max 256GB"))
                .andReturn();

        System.out.println("[测试2-查询详情] 成功, 订单项数量=" +
                objectMapper.readTree(result.getResponse().getContentAsString())
                        .get("data").get("orderItems").size());
    }

    /**
     * 测试3：查询不存在的订单 (GET /api/mp/orders/99999)
     */
    @Test
    @Order(3)
    public void test03_GetOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/mp/orders/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        System.out.println("[测试3-查询不存在订单] 成功, 返回404");
    }

    /**
     * 测试4：按用户分页查询订单 (GET /api/mp/orders/user/{userId})
     */
    @Test
    @Order(4)
    public void test04_GetOrdersByUserId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mp/orders/user/2")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        System.out.println("[测试4-用户分页查询] 成功, 总记录数=" + data.get("total").asInt()
                + ", 当前页数量=" + data.get("records").size());
    }

    /**
     * 测试5：按用户+状态筛选查询 (GET /api/mp/orders/user/{userId}?status=PENDING)
     */
    @Test
    @Order(5)
    public void test05_GetOrdersByUserIdWithStatus() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mp/orders/user/2")
                        .param("status", "PENDING")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        JsonNode records = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data").get("records");
        // 验证所有返回的订单状态都是 PENDING
        for (JsonNode record : records) {
            Assertions.assertEquals("PENDING", record.get("status").asText());
        }

        System.out.println("[测试5-状态筛选查询] 成功, PENDING订单数=" + records.size());
    }

    /**
     * 测试6：查询全部订单分页 (GET /api/mp/orders)
     */
    @Test
    @Order(6)
    public void test06_GetAllOrders() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/mp/orders")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        System.out.println("[测试6-全部分页查询] 成功, 总记录数=" + data.get("total").asInt());
    }

    /**
     * 测试7：更新订单状态 (PUT /api/mp/orders/{id}/status)
     */
    @Test
    @Order(7)
    public void test07_UpdateOrderStatus() throws Exception {
        Assertions.assertNotNull(createdOrderId, "订单ID不能为空");

        MvcResult result = mockMvc.perform(put("/api/mp/orders/" + createdOrderId + "/status")
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.payTime").exists())
                .andReturn();

        System.out.println("[测试7-更新状态] 成功, 新状态=PAID, payTime已设置");
    }

    /**
     * 测试8：更新订单信息 (PUT /api/mp/orders/{id})
     */
    @Test
    @Order(8)
    public void test08_UpdateOrder() throws Exception {
        Assertions.assertNotNull(createdOrderId, "订单ID不能为空");

        MpOrder update = new MpOrder();
        update.setShippingAddress("北京市海淀区中关村");
        update.setReceiverName("新收货人");
        update.setRemark("已修改地址");

        mockMvc.perform(put("/api/mp/orders/" + createdOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.shippingAddress").value("北京市海淀区中关村"))
                .andExpect(jsonPath("$.data.receiverName").value("新收货人"));

        System.out.println("[测试8-更新信息] 成功, 地址和收货人已更新");
    }

    /**
     * 测试9：创建订单参数校验-缺少userId (POST /api/mp/orders)
     */
    @Test
    @Order(9)
    public void test09_CreateOrder_MissingUserId() throws Exception {
        MpOrder order = new MpOrder();
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPayAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/mp/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        System.out.println("[测试9-参数校验] 成功, 缺少userId返回400");
    }

    /**
     * 测试10：删除订单 (DELETE /api/mp/orders/{id})
     */
    @Test
    @Order(10)
    public void test10_DeleteOrder() throws Exception {
        Assertions.assertNotNull(createdOrderId, "订单ID不能为空");

        mockMvc.perform(delete("/api/mp/orders/" + createdOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证订单已被删除
        mockMvc.perform(get("/api/mp/orders/" + createdOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        System.out.println("[测试10-删除订单] 成功, orderId=" + createdOrderId + " 已删除");
    }

    /**
     * 测试11：删除不存在的订单 (DELETE /api/mp/orders/99999)
     */
    @Test
    @Order(11)
    public void test11_DeleteOrderNotFound() throws Exception {
        mockMvc.perform(delete("/api/mp/orders/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        System.out.println("[测试11-删除不存在订单] 成功, 返回404");
    }
}
