package org.kuraterut.productservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.productservice.model.utils.ProductHoldedStatus;
import org.kuraterut.productservice.model.event.ProductHoldRemoveEvent;
import org.kuraterut.productservice.model.utils.ProductHoldRemoveEventDetails;
import org.kuraterut.productservice.repository.ProductHoldedRepository;
import org.kuraterut.productservice.usecases.ProductHoldRemoveEventUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductHoldRemoveEventService implements ProductHoldRemoveEventUseCase {
    private final ProductHoldedRepository productHoldedRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka-topics.product-hold-remove}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    @Override
    public void listenProductHoldRemoveEvent(String message, Acknowledgment ack) throws JsonProcessingException {
        log.info("[ProductHoldRemoveEventService:listenProductHoldRemoveEvent] Start listenProductHoldRemoveEvent");
        ProductHoldRemoveEvent event = objectMapper.readValue(message, ProductHoldRemoveEvent.class);
        log.info("[ProductHoldRemoveEventService:listenProductHoldRemoveEvent] ProductHoldRemoveEvent serialized successfully");
        if (event.getDetails() == ProductHoldRemoveEventDetails.TO_REMOVE){
            productHoldedRepository.updateStatusByOrderId(event.getOrderId(), ProductHoldedStatus.TO_REMOVE);
        } else {
            productHoldedRepository.updateStatusByOrderId(event.getOrderId(), ProductHoldedStatus.TO_RETURN);
        }
        log.info("[ProductHoldRemoveEventService:listenProductHoldRemoveEvent] status updated by order ID");
        ack.acknowledge();
        log.info("[ProductHoldRemoveEventService:listenProductHoldRemoveEvent] acknowledged");
    }
}
