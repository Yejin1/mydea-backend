package com.mydea.mydea_backend.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydea.mydea_backend.order.dto.*;
import com.mydea.mydea_backend.order.domain.OrderStatus;
import com.mydea.mydea_backend.order.service.OrderService;
import com.mydea.mydea_backend.order.support.SecurityUtils;
import com.mydea.mydea_backend.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Collections;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = OrderController.class)
@WithMockUser(username = "1", roles = "USER")
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockitoBean
    OrderService orderService;
    @MockitoBean
    SecurityUtils securityUtils;

    @TestConfiguration
    static class JwtStubConfig {
        @Bean
        JwtTokenProvider jwtTokenProvider() {
            return new JwtTokenProvider("0123456789012345678901234567890123456789", 3600);
        }
    }

    private String json(Object o) throws Exception {
        return om.writeValueAsString(o);
    }

    private OrderResponse.Item item(Long orderItemId, Long workId, int unitPrice, int qty) {
        return OrderResponse.Item.builder()
                .orderItemId(orderItemId)
                .workId(workId)
                .name("Work" + workId)
                .optionHash("opt" + workId)
                .thumbUrl("https://img/" + workId + ".png")
                .unitPrice(unitPrice)
                .quantity(qty)
                .lineTotal(unitPrice * qty)
                .build();
    }

    private OrderResponse response(Long orderId, OrderStatus status, List<OrderResponse.Item> items) {
        int subtotal = items.stream().mapToInt(OrderResponse.Item::getLineTotal).sum();
        int shipping = 3000;
        int discount = 0;
        return OrderResponse.builder()
                .orderId(orderId)
                .orderNo("20250101-" + String.format("%06d", orderId))
                .status(status)
                .subtotal(subtotal)
                .shippingFee(shipping)
                .discount(discount)
                .total(subtotal + shipping - discount)
                .recipientName("홍길동")
                .phone("01012345678")
                .address1("서울시 어딘가")
                .address2("101동")
                .zipcode("12345")
                .note("메모")
                .createdAt(LocalDateTime.now())
                .paidAt(null)
                .shippedAt(null)
                .deliveredAt(null)
                .canceledAt(null)
                .items(items)
                .build();
    }

    @Test
    @DisplayName("POST /api/orders/preview - 장바구니 프리뷰 성공")
    void preview_ok() throws Exception {
        OrderPreviewRequest req = new OrderPreviewRequest();
        req.setCartId(55L);

        OrderPreviewResponse resp = OrderPreviewResponse.builder()
                .items(List.of(OrderPreviewResponse.Item.builder()
                        .workId(10L)
                        .name("Work10")
                        .optionHash("opt10")
                        .thumbUrl("https://img/10.png")
                        .unitPrice(10000)
                        .quantity(2)
                        .lineTotal(20000)
                        .build()))
                .subtotal(20000)
                .shippingFee(3000)
                .discount(0)
                .total(23000)
                .build();

        given(orderService.preview(eq(55L))).willReturn(resp);

        mvc.perform(post("/api/orders/preview")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(20000))
                .andExpect(jsonPath("$.items[0].workId").value(10L));

        then(orderService).should().preview(55L);
    }

    @Test
    @DisplayName("POST /api/orders - 주문 생성 성공")
    void create_ok() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setCartId(55L);
        req.setRecipientName("홍길동");
        req.setPhone("01012345678");
        req.setAddress1("서울시 어딘가");
        req.setAddress2("101동");
        req.setZipcode("12345");
        req.setNote("메모");

        OrderResponse resp = response(100L, OrderStatus.PAYMENT_PENDING, List.of(
                item(1L, 10L, 10000, 2)));

        given(orderService.create(eq(1L), isNull(), any(OrderCreateRequest.class)))
                .willReturn(resp);

        mvc.perform(post("/api/orders")
                .with(csrf())
                .with(user("1"))
                .principal(new UsernamePasswordAuthenticationToken("1", null, Collections.emptyList()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100L))
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"));

        then(orderService).should().create(eq(1L), isNull(), any(OrderCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/pay - 결제 성공 시 상태 PAID")
    void pay_ok() throws Exception {
        Long orderId = 100L;
        PayRequest payReq = new PayRequest();
        payReq.setSuccess(true);
        payReq.setMethod("SIMULATOR");

        OrderResponse paidResp = response(orderId, OrderStatus.PAID, List.of(
                item(1L, 10L, 10000, 2)));

        given(orderService.paySimulator(eq(1L), eq(orderId), any(PayRequest.class)))
                .willReturn(paidResp);

        mvc.perform(post("/api/orders/{id}/pay", orderId)
                .with(csrf())
                .with(user("1"))
                .principal(new UsernamePasswordAuthenticationToken("1", null, Collections.emptyList()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(payReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        then(orderService).should().paySimulator(eq(1L), eq(orderId), any(PayRequest.class));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - 단건 조회")
    void get_one() throws Exception {
        Long orderId = 100L;
        OrderResponse resp = response(orderId, OrderStatus.PAYMENT_PENDING, List.of(item(1L, 10L, 10000, 2)));
        given(orderService.get(eq(1L), eq(orderId))).willReturn(resp);

        mvc.perform(get("/api/orders/{id}", orderId)
                .with(user("1"))
                .principal(new UsernamePasswordAuthenticationToken("1", null, Collections.emptyList()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId));

        then(orderService).should().get(1L, orderId);
    }

    @Test
    @DisplayName("GET /api/orders - 페이지 목록")
    void list_ok() throws Exception {
        OrderResponse r1 = response(100L, OrderStatus.PAYMENT_PENDING, List.of(item(1L, 10L, 10000, 1)));
        Page<OrderResponse> page = new PageImpl<>(List.of(r1));
        given(orderService.list(eq(1L), eq(0), eq(20))).willReturn(page);

        mvc.perform(get("/api/orders")
                .with(user("1"))
                .principal(new UsernamePasswordAuthenticationToken("1", null, Collections.emptyList()))
                .param("page", "0")
                .param("size", "20")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("content")))
                .andExpect(content().string(containsString("orderId")));

        then(orderService).should().list(1L, 0, 20);
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - 취소")
    void cancel_ok() throws Exception {
        Long orderId = 100L;
        CancelRequest creq = new CancelRequest();
        creq.setReason("CHANGE_OF_MIND");

        OrderResponse canceled = response(orderId, OrderStatus.CANCELED, List.of(item(1L, 10L, 10000, 2)));
        given(orderService.cancel(eq(1L), eq(orderId), eq("CHANGE_OF_MIND")))
                .willReturn(canceled);

        mvc.perform(post("/api/orders/{id}/cancel", orderId)
                .with(csrf())
                .with(user("1"))
                .principal(new UsernamePasswordAuthenticationToken("1", null, Collections.emptyList()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(creq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        then(orderService).should().cancel(1L, orderId, "CHANGE_OF_MIND");
    }
}
