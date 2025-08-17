package org.kuraterut.productservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.productservice.model.entity.ProductHolded;
import org.kuraterut.productservice.model.utils.ProductHoldedStatus;
import org.kuraterut.productservice.repository.ProductHoldedRepository;
import org.kuraterut.productservice.repository.ProductRepository;
import org.kuraterut.productservice.usecases.ProductHoldRemoveUseCase;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductHoldRemoveService implements ProductHoldRemoveUseCase {
    private final ProductRepository productRepository;
    private final ProductHoldedRepository productHoldedRepository;

    @Scheduled(fixedRateString = "${scheduling.process-remove-product-hold-rate}")
    @Transactional
    @Override
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void processRemoveProductHolds() {
        log.info("[ProductHoldRemoveService:processRemoveProductHolds] Start processRemoveProductHolds");
        productHoldedRepository.deleteAllByStatus(ProductHoldedStatus.TO_REMOVE);
        log.info("[ProductHoldRemoveService:processRemoveProductHolds] Deleted all holded products by status");
        List<ProductHolded> productHoldedList = productHoldedRepository.findTop100ByStatus(ProductHoldedStatus.TO_RETURN);
        log.info("[ProductHoldRemoveService:processRemoveProductHolds] Found holded products: {}", productHoldedList);
        for (ProductHolded productHolded : productHoldedList) {
            productRepository.raiseStock(productHolded.getProductId(), productHolded.getQuantity());
            log.info("[ProductHoldRemoveService:processRemoveProductHolds] Raise Stock: productId: {}, quantity: {}",
                    productHolded.getProductId(), productHolded.getQuantity());
            productHolded.setStatus(ProductHoldedStatus.TO_REMOVE);
        }
        productHoldedRepository.saveAll(productHoldedList);
        log.info("[ProductHoldRemoveService:processRemoveProductHolds] Saved all holded products");
    }
}
