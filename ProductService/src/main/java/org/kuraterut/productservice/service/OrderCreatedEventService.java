package org.kuraterut.productservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.productservice.dto.OrderItemDto;
import org.kuraterut.productservice.model.event.inbox.OrderCreatedInbox;
import org.kuraterut.productservice.model.entity.Product;
import org.kuraterut.productservice.model.entity.ProductHolded;
import org.kuraterut.productservice.model.utils.ProductHoldItemFailedReason;
import org.kuraterut.productservice.model.utils.ProductHoldedStatus;
import org.kuraterut.productservice.model.event.*;
import org.kuraterut.productservice.repository.OrderCreatedInboxRepository;
import org.kuraterut.productservice.repository.ProductHoldedRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.usecases.OrderCreatedEventUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedEventService implements OrderCreatedEventUseCase {
    private final OrderCreatedInboxRepository orderCreatedInboxRepository;
    private final ProductRepository productRepository;
    private final ProductHoldedRepository productHoldedRepository;
    private final KafkaTemplate<String, ProductHoldSuccessEvent> productHoldSuccessEventKafkaTemplate;
    private final KafkaTemplate<String, ProductHoldFailedEvent> productHoldFailedEventKafkaTemplate;

    private final ObjectMapper mapper;

    @Value("${kafka-topics.product-hold-failed}")
    private String productHoldFailedTopic;

    @Value("${kafka-topics.product-hold-success}")
    private String productHoldSuccessTopic;

    @Override
    @KafkaListener(topics = "${kafka-topics.order-created}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenOrderCreated(String message, Acknowledgment ack) {
        try{
            OrderCreatedEvent event = mapper.readValue(message, OrderCreatedEvent.class);
            OrderCreatedInbox inbox = new OrderCreatedInbox();
            inbox.setOrderId(event.getOrderId());
            inbox.setUserId(event.getUserId());
            List<String> jsonItems = new ArrayList<>();
            for (OrderItemDto item : event.getItems()) {
                jsonItems.add(mapper.writeValueAsString(item));
            }
            inbox.setJsonItems(jsonItems);
            inbox.setProcessed(false);
            orderCreatedInboxRepository.save(inbox);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedRateString = "${scheduling.process-order-created-rate}")
    @Transactional
    public void processOrderCreatedEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
        List<OrderCreatedInbox> inboxes = orderCreatedInboxRepository.findTop100ByProcessedIsFalse();
        for (OrderCreatedInbox inbox : inboxes) {
            List<String> jsonItems = inbox.getJsonItems();
            boolean flagAsFailed = false;
            List<ProductHoldItemFailed> itemsFailed = new ArrayList<>();
            List<ProductHoldItemSuccess> itemSuccesses = new ArrayList<>();
            for(String jsonItem : jsonItems) {
                OrderItemDto orderItem = mapper.readValue(jsonItem, OrderItemDto.class);
                Optional<Product> productOptional = productRepository.findById(orderItem.getProductId());
                if(productOptional.isEmpty()) {
                    ProductHoldItemFailed productHoldItemFailed = new ProductHoldItemFailed();
                    productHoldItemFailed.setProductId(orderItem.getProductId());
                    productHoldItemFailed.setQuantity(orderItem.getQuantity());
                    productHoldItemFailed.setReason(ProductHoldItemFailedReason.NOT_FOUND);
                    itemsFailed.add(productHoldItemFailed);
                    flagAsFailed = true;
                    continue;
                }
                Product product = productOptional.get();

                if (productRepository.reduceStockIfAvailable(orderItem.getProductId(), orderItem.getQuantity()) == 0){
                    flagAsFailed = true;
                    ProductHoldItemFailed productHoldItemFailed = new ProductHoldItemFailed();
                    productHoldItemFailed.setProductId(orderItem.getProductId());
                    productHoldItemFailed.setQuantity(orderItem.getQuantity());
                    productHoldItemFailed.setReason(ProductHoldItemFailedReason.NOT_ENOUGH_ITEMS);
                    itemsFailed.add(productHoldItemFailed);
                }
                else{
                    ProductHolded productHolded = new ProductHolded();
                    productHolded.setProductId(orderItem.getProductId());
                    productHolded.setQuantity(orderItem.getQuantity());
                    productHolded.setStatus(ProductHoldedStatus.HOLDED);
                    productHolded.setOrderId(inbox.getOrderId());
                    productHolded.setUnitPrice(product.getPrice());
                    productHolded.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
                    productHoldedRepository.save(productHolded);

                    ProductHoldItemSuccess productHoldItemSuccess = new ProductHoldItemSuccess();
                    productHoldItemSuccess.setProductId(orderItem.getProductId());
                    productHoldItemSuccess.setQuantity(orderItem.getQuantity());
                    productHoldItemSuccess.setUnitPrice(product.getPrice());
                    productHoldItemSuccess.setTotalPrice(productHolded.getTotalPrice());
                    itemSuccesses.add(productHoldItemSuccess);
                }
            }
            if(flagAsFailed){
                productHoldedRepository.updateStatusByOrderId(inbox.getOrderId(), ProductHoldedStatus.TO_RETURN);
                ProductHoldFailedEvent productHoldFailedEvent = new ProductHoldFailedEvent();
                productHoldFailedEvent.setOrderId(inbox.getOrderId());
                productHoldFailedEvent.setItems(itemsFailed);
                productHoldFailedEventKafkaTemplate.send(productHoldFailedTopic, productHoldFailedEvent).get();
            } else{
                ProductHoldSuccessEvent productHoldSuccessEvent = new ProductHoldSuccessEvent();
                productHoldSuccessEvent.setOrderId(inbox.getOrderId());
                productHoldSuccessEvent.setItems(itemSuccesses);
                productHoldSuccessEventKafkaTemplate.send(productHoldSuccessTopic, productHoldSuccessEvent).get();
            }
            inbox.setProcessed(true);
            orderCreatedInboxRepository.save(inbox);
        }
    }
}
