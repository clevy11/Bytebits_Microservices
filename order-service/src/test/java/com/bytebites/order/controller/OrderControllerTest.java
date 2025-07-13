package com.bytebites.order.controller;

import com.bytebites.order.dto.OrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 400 when request body is invalid")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnBadRequestWhenInvalid() throws Exception {
        OrderRequest request = new OrderRequest(); // empty invalid request
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create order when request is valid")
    @WithMockUser(roles = "CUSTOMER")
    void shouldCreateOrderWhenValid() throws Exception {
        OrderRequest request = new OrderRequest();
        request.setRestaurantId(1L);
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setName("Pizza");
        item.setPrice(BigDecimal.valueOf(10.5));
        item.setQuantity(2);
        request.setItems(List.of(item));
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
