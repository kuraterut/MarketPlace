package org.kuraterut.orderservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderListResponse;
import org.kuraterut.orderservice.model.event.dto.OrderItemDto;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.entity.Order;
import org.kuraterut.orderservice.model.entity.OrderItem;
import org.kuraterut.orderservice.model.event.outbox.CreateOrderEventOutbox;
import org.kuraterut.orderservice.model.event.dto.ProductHoldItemFailed;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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

    public OrderResponse toResponse(Order order) {
        ObjectMapper mapper = new ObjectMapper();
        List<ProductHoldItemFailed> details = new ArrayList<>();
        if(order.getDetails() != null){
            for(String detail : order.getDetails()){
                try{
                    ProductHoldItemFailed productHoldItemFailed = mapper.readValue(detail, ProductHoldItemFailed.class);
                    details.add(productHoldItemFailed);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Json Processing Exception", e);
                }
            }
        }

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setCreatedAt(order.getCreatedAt().toString());
        orderResponse.setUpdatedAt(order.getUpdatedAt().toString());
        orderResponse.setId(order.getId());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setUserId(order.getUserId());
        orderResponse.setItems(toResponses(order.getItems()));
        orderResponse.setDetails(details);
        return orderResponse;
    }


//    public List<OrderResponse> toResponses(List<Order> orders) {
//        return orders.stream().map(this::toResponse).toList();
//    }

    public OrderListResponse toResponses(Page<Order> orders)  {
        return new OrderListResponse(orders.map(this::toResponse).stream().toList());
    }


    public CreateOrderEventOutbox toOutbox(Order order){
        CreateOrderEventOutbox createOrderEventOutbox = new CreateOrderEventOutbox();
        createOrderEventOutbox.setOrder(order);
        createOrderEventOutbox.setProcessed(false);
        return createOrderEventOutbox;
    }

}
