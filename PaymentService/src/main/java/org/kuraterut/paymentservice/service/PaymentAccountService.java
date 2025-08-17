package org.kuraterut.paymentservice.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.paymentservice.dto.response.PaymentAccountListResponse;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.exception.model.*;
import org.kuraterut.paymentservice.mapper.PaymentAccountMapper;
import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.kuraterut.paymentservice.model.entity.Transaction;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.repository.TransactionRepository;
import org.kuraterut.paymentservice.usecases.paymentaccount.CreatePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.DeletePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.GetPaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.UpdatePaymentAccountUseCase;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "payment_accounts")
@Slf4j
public class PaymentAccountService implements CreatePaymentAccountUseCase, UpdatePaymentAccountUseCase,
        DeletePaymentAccountUseCase, GetPaymentAccountUseCase {
//TODO Сделать фильтрацию с Criteria API
    //TODO При нахождении сущности логировать найденную сущность, {}, entity
    private final PaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;
    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse createPaymentAccount(Long userId) {
        try {
            log.info("[PaymentAccountService:createPaymentAccount] Start createPaymentAccount");
            PaymentAccount paymentAccount = paymentAccountMapper.toEntity(userId);
            paymentAccount = paymentAccountRepository.saveAndFlush(paymentAccount);
            log.info("[PaymentAccountService:createPaymentAccount] Payment Account created and saved");
            return paymentAccountMapper.toResponse(paymentAccount);
        } catch (DataIntegrityViolationException | ConstraintViolationException e){
            log.warn("[PaymentAccountService:createPaymentAccount] Payment Account Already Exists with userId: {}", userId);
            throw new PaymentAccountAlreadyExistsException("Payment Account is already exists with userId: " + userId);
        }
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deletePaymentAccountById(Long id) {
        log.info("[PaymentAccountService:deletePaymentAccountById] Start deletePaymentAccountById");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[PaymentAccountService:deletePaymentAccountById] Payment Account Not Found with id: {}", id);
                    return new PaymentAccountNotFoundException("Payment account not found by id: " + id);
                });
        log.info("[PaymentAccountService:deletePaymentAccountById] Payment Account Found");
        BigDecimal balance = paymentAccount.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            String message = String.format("Can't delete payment account with id: %d, because balance is not zero: %f", id, balance);
            log.warn("[PaymentAccountService:deletePaymentAccountById] {}", message);
            throw new PaymentAccountIsNotEmptyException(message);
        }
        paymentAccountRepository.deleteById(id);
        log.info("[PaymentAccountService:deletePaymentAccountById] Payment Account Deleted");
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deletePaymentAccountByUserId(Long userId) {
        log.info("[PaymentAccountService:deletePaymentAccountByUserId] Start deletePaymentAccountByUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[PaymentAccountService:deletePaymentAccountByUserId] Payment Account Not Found with id: {}", userId);
                    return new PaymentAccountNotFoundException("Payment account not found by userId: " + userId);
                });
        log.info("[PaymentAccountService:deletePaymentAccountByUserId] Payment Account Found");
        BigDecimal balance = paymentAccount.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            String message = String.format("Can't delete payment account with userId: %d, because balance is not zero: %f", userId, balance);
            log.warn("[PaymentAccountService:deletePaymentAccountByUserId] {}", message);
            throw new PaymentAccountIsNotEmptyException(message);
        }
        paymentAccountRepository.deleteById(paymentAccount.getId());
        log.info("[PaymentAccountService:deletePaymentAccountByUserId] Payment Account Deleted");
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getAllPaymentAccounts(Pageable pageable) {
        log.info("[PaymentAccountService:getAllPaymentAccounts] Start getAllPaymentAccounts");
        Page<PaymentAccount> page = paymentAccountRepository.findAll(pageable);
        log.info("[PaymentAccountService:getAllPaymentAccounts] Payment Accounts Found: {}", page);
        return paymentAccountMapper.toResponses(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'user_' + #userId")
    public PaymentAccountResponse getPaymentAccountByUserId(Long userId) {
        log.info("[PaymentAccountService:getPaymentAccountByUserId] Start getPaymentAccountByUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[PaymentAccountService:getPaymentAccountByUserId] Payment Account Not Found with id: {}", userId);
                    return new PaymentAccountNotFoundException("Payment account not found by user id: " + userId);
                });
        log.info("[PaymentAccountService:getPaymentAccountByUserId] Payment Account Found");
        return paymentAccountMapper.toResponse(paymentAccount);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public PaymentAccountResponse getPaymentAccountById(Long id){
        log.info("[PaymentAccountService:getPaymentAccountById] Start getPaymentAccountById");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[PaymentAccountService:getPaymentAccountById] Payment Account Not Found with id: {}", id);
                    return new PaymentAccountNotFoundException("Payment account not found by id: " + id);
                });
        log.info("[PaymentAccountService:getPaymentAccountById] Payment Account Found");
        return paymentAccountMapper.toResponse(paymentAccount);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'active_' + #isActive + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getPaymentAccountsByIsActive(boolean isActive, Pageable pageable) {
        log.info("[PaymentAccountService:getPaymentAccountsByIsActive] Start getPaymentAccountsByIsActive");
        Page<PaymentAccount> paymentAccounts = paymentAccountRepository.findAllPaymentAccountByActive(isActive, pageable);
        log.info("[PaymentAccountService:getPaymentAccountsByIsActive] Payment Accounts Found: {}", paymentAccounts);
        return paymentAccountMapper.toResponses(paymentAccounts);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'balance_' + #min + '_' + #max + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getPaymentAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        log.info("[PaymentAccountService:getPaymentAccountsByBalanceBetween] Start getPaymentAccountsByBalanceBetween");
        Page<PaymentAccount> paymentAccounts = paymentAccountRepository.findAllPaymentAccountByBalanceBetween(min, max, pageable);
        log.info("[PaymentAccountService:getPaymentAccountsByBalanceBetween] Payment Accounts Found: {}", paymentAccounts);
        return paymentAccountMapper.toResponses(paymentAccounts);
    }
//TODO После внедрения userId как первичного ключа исправить кэширование
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse depositPaymentAccountByUserId(Long userId, BigDecimal amount) {
        log.info("[PaymentAccountService:depositPaymentAccountByUserId] Start depositPaymentAccountByUserId");
        PaymentAccount account = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[PaymentAccountService:depositPaymentAccountByUserId] Payment Account Not Found with userId: {}", userId);
                    return new PaymentAccountNotFoundException("Payment account not found by user id: " + userId);
                });

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setAccount(account);
        transaction.setType(TransactionType.DEPOSIT);

        log.info("[PaymentAccountService:depositPaymentAccountByUserId] Try Deposit Payment Account");
        int rows = paymentAccountRepository.depositPaymentAccountByUserId(userId, amount);
        if (rows == 0){
            log.warn("[PaymentAccountService:depositPaymentAccountByUserId] Can't Deposit Payment Account");
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new UpdatePaymentAccountException("Can't deposit account");
        }
        entityManager.flush();
        entityManager.clear();
        log.info("[PaymentAccountService:depositPaymentAccountByUserId] Deposit Payment Account Successfully");

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        log.info("[PaymentAccountService:depositPaymentAccountByUserId] Transaction Saved Successfully");
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by userId: " + userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse withdrawPaymentAccountByUserId(Long userId, BigDecimal amount) {
        log.info("[PaymentAccountService:withdrawPaymentAccountByUserId] Start withdrawPaymentAccountByUserId");
        PaymentAccount account = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("[PaymentAccountService:withdrawPaymentAccountByUserId] Payment Account Not Found with userId: {}", userId);
                    return new PaymentAccountNotFoundException("Payment account not found by user id: " + userId);
                });
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setAccount(account);
        transaction.setType(TransactionType.WITHDRAW);

        log.info("[PaymentAccountService:withdrawPaymentAccountByUserId] Try Withdraw Payment Account");
        int rows = paymentAccountRepository.withdrawPaymentAccountByUserId(userId, amount);
        if (rows == 0){
            log.warn("[PaymentAccountService:withdrawPaymentAccountByUserId] Can't Withdraw Payment Account");
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new UpdatePaymentAccountException("Can't withdraw account");
        }
        entityManager.flush();
        entityManager.clear();
        log.info("[PaymentAccountService:withdrawPaymentAccountByUserId] Withdraw Payment Account Successfully");

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        log.info("[PaymentAccountService:withdrawPaymentAccountByUserId] Transaction Saved Successfully");
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by user id: " + userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse activatePaymentAccountById(Long id) {
        log.info("[PaymentAccountService:activatePaymentAccountById] Start activatePaymentAccountById");
        if(!paymentAccountRepository.existsById(id)) {
            log.warn("[PaymentAccountService:activatePaymentAccountById] Payment Account Not Found with id: {}", id);
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        log.info("[PaymentAccountService:activatePaymentAccountById] Try Activate Payment Account");
        int rows = paymentAccountRepository.activatePaymentAccountById(id);
        if (rows == 0){
            log.warn("[PaymentAccountService:activatePaymentAccountById] Can't Activate Payment Account");
            throw new UpdatePaymentAccountException("Can't activate account");
        }
        entityManager.flush();
        entityManager.clear();
        log.info("[PaymentAccountService:activatePaymentAccountById] Payment Account Successfully Activated");
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse activatePaymentAccountByUserId(Long userId) {
        log.info("[PaymentAccountService:activatePaymentAccountByUserId] Start activatePaymentAccountByUserId");
        if(!paymentAccountRepository.existsByUserId(userId)) {
            log.warn("[PaymentAccountService:activatePaymentAccountByUserId] Payment Account Not Found with userId: {}", userId);
            throw new PaymentAccountNotFoundException("Bank account not found by user id: " + userId);
        }
        log.info("[PaymentAccountService:activatePaymentAccountByUserId] Try Activate Payment Account");
        int rows = paymentAccountRepository.activatePaymentAccountByUserId(userId);
        if (rows == 0){
            log.warn("[PaymentAccountService:activatePaymentAccountByUserId] Can't Activate Payment Account");
            throw new UpdatePaymentAccountException("Can't activate account");
        }
        entityManager.flush();
        entityManager.clear();
        log.info("[PaymentAccountService:activatePaymentAccountByUserId] Payment Account Successfully Activated");

        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by user id: " + userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse deactivatePaymentAccountById(Long id) {
        log.info("[PaymentAccountService:deactivatePaymentAccountById] Start deactivatePaymentAccountById");
        if(!paymentAccountRepository.existsById(id)) {
            log.warn("[PaymentAccountService:deactivatePaymentAccountById] Payment Account Not Found with id: {}", id);
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        log.info("[PaymentAccountService:deactivatePaymentAccountById] Try Deactivate Payment Account");
        int rows = paymentAccountRepository.deactivatePaymentAccountById(id);
        if (rows == 0){
            log.warn("[PaymentAccountService:deactivatePaymentAccountById] Can't Deactivate Payment Account");
            throw new UpdatePaymentAccountException("Can't deactivate account");
        }
        entityManager.flush();
        entityManager.clear();
        log.info("[PaymentAccountService:deactivatePaymentAccountById] Payment Account Successfully Deactivated");

        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse deactivatePaymentAccountByUserId(Long userId) {
        log.info("[PaymentAccountService:deactivatePaymentAccountByUserId] Start deactivatePaymentAccountByUserId");
        if(!paymentAccountRepository.existsByUserId(userId)) {
            log.warn("[PaymentAccountService:deactivatePaymentAccountByUserId] Payment Account Not Found with userId: {}", userId);
            throw new PaymentAccountNotFoundException("Bank account not found by user id: " + userId);
        }
        log.info("[PaymentAccountService:deactivatePaymentAccountByUserId] Try Deactivate Payment Account");
        int rows = paymentAccountRepository.deactivatePaymentAccountByUserId(userId);
        if (rows == 0){
            log.warn("[PaymentAccountService:deactivatePaymentAccountByUserId] Can't Deactivate Payment Account");
            throw new UpdatePaymentAccountException("Can't deactivate account");
        }
        entityManager.flush();
        entityManager.clear();
        log.info("[PaymentAccountService:deactivatePaymentAccountByUserId] Payment Account Successfully Deactivated");

        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by user id: " + userId)));
    }
}
