package org.kuraterut.orderservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.OrderItemDto;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.Order;
import org.kuraterut.orderservice.model.OrderItem;
import org.kuraterut.orderservice.model.OrderOutbox;
import org.kuraterut.orderservice.model.event.ProductHoldItemFailed;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {


    public Order toEntity(CreateOrderRequest request, Long userId){
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(toEntities(request.getItems(), order));
        return order;
    }

    public OrderItem toEntity(OrderItemDto dto, Order order){
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setProductId(dto.getProductId());
        orderItem.setOrder(order);
        return orderItem;
    }
    public List<OrderItem> toEntities(List<OrderItemDto> dtos, Order order){
        return dtos.stream().map(orderItemDto -> toEntity(orderItemDto, order)).toList();
    }

    public OrderItemDto toResponse(OrderItem item){
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setProductId(item.getProductId());
        orderItemDto.setQuantity(item.getQuantity());
        return orderItemDto;
    }
    public List<OrderItemDto> toResponses(List<OrderItem> items){
        return items.stream().map(this::toResponse).toList();
    }

    public OrderResponse toResponse(Order order) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<ProductHoldItemFailed> details = new ArrayList<>();
        if(order.getDetails() != null){
            for(String detail : order.getDetails()){
                ProductHoldItemFailed productHoldItemFailed = mapper.readValue(detail, ProductHoldItemFailed.class);
                details.add(productHoldItemFailed);
            }
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setCreatedAt(order.getCreatedAt());
        orderResponse.setUpdatedAt(order.getUpdatedAt());
        orderResponse.setId(order.getId());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setUserId(order.getUserId());
        orderResponse.setItems(toResponses(order.getItems()));
        orderResponse.setDetails(details);
        return orderResponse;
    }


    public List<OrderResponse> toResponses(List<Order> orders) throws JsonProcessingException {
        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Order order : orders) {
            orderResponses.add(toResponse(order));
        }
        return orderResponses;
    }


    public OrderOutbox toOutbox(Order order){
        OrderOutbox orderOutbox = new OrderOutbox();
        orderOutbox.setOrder(order);
        orderOutbox.setProcessed(false);
        return orderOutbox;
    }

}
