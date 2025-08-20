package org.kuraterut.paymentservice.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.paymentservice.dto.response.PaymentAccountListResponse;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.exception.model.*;
import org.kuraterut.paymentservice.logger.PaymentAccountLogs;
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


    private PaymentAccount findPaymentAccount(Long userId, String logPrefix){
        return paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(PaymentAccountLogs.NOT_FOUND, logPrefix, userId);
                    return new PaymentAccountNotFoundException("%s Payment account not found by id: %d", logPrefix, userId);
                });
    }



    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse createPaymentAccount(Long userId) {
        String logPrefix = "[PaymentAccountService:createPaymentAccount]";
        try {
            log.info("{} Start createPaymentAccount", logPrefix);
            PaymentAccount paymentAccount = paymentAccountMapper.toEntity(userId);
            paymentAccount = paymentAccountRepository.saveAndFlush(paymentAccount);
            log.info("{} Payment Account created and saved", logPrefix);
            return paymentAccountMapper.toResponse(paymentAccount);
        } catch (DataIntegrityViolationException | ConstraintViolationException e){
            log.warn("{} Payment Account Already Exists with id: {}", logPrefix, userId);
            throw new PaymentAccountAlreadyExistsException("%s Payment Account is already exists with id: %d", logPrefix, userId);
        }
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deletePaymentAccountByUserId(Long userId) {
        String logPrefix = "[PaymentAccountService:deletePaymentAccountByUserId]";
        log.info("{} Start deletePaymentAccountByUserId", logPrefix);
        PaymentAccount paymentAccount = findPaymentAccount(userId, logPrefix);
        log.info(PaymentAccountLogs.FOUND, logPrefix, paymentAccount);
        BigDecimal balance = paymentAccount.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            String message = String.format("%s Can't delete payment account with id: %d, because balance is not zero: %f", logPrefix, userId, balance);
            log.warn("{} {}", logPrefix, message);
            throw new PaymentAccountIsNotEmptyException("%s %s", logPrefix, message);
        }
        paymentAccountRepository.deleteById(paymentAccount.getId());
        log.info("{} Payment Account Deleted", logPrefix);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getAllPaymentAccounts(Pageable pageable) {
        String logPrefix = "[PaymentAccountService:getAllPaymentAccounts]";
        log.info("{} Start getAllPaymentAccounts", logPrefix);
        Page<PaymentAccount> page = paymentAccountRepository.findAll(pageable);
        log.info(PaymentAccountLogs.FOUND_LIST, logPrefix, page);
        return paymentAccountMapper.toResponses(page);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'user_' + #userId")
    public PaymentAccountResponse getPaymentAccountByUserId(Long userId) {
        String logPrefix = "[PaymentAccountService:getPaymentAccountByUserId]";
        log.info("{} Start getPaymentAccountByUserId", logPrefix);
        PaymentAccount paymentAccount = findPaymentAccount(userId, logPrefix);
        log.info(PaymentAccountLogs.FOUND, logPrefix, paymentAccount);
        return paymentAccountMapper.toResponse(paymentAccount);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'active_' + #isActive + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getPaymentAccountsByIsActive(boolean isActive, Pageable pageable) {
        String logPrefix = "[PaymentAccountService:getPaymentAccountsByIsActive]";
        log.info("{} Start getPaymentAccountsByIsActive", logPrefix);
        Page<PaymentAccount> paymentAccounts = paymentAccountRepository.findAllPaymentAccountByActive(isActive, pageable);
        log.info(PaymentAccountLogs.FOUND_LIST, logPrefix, paymentAccounts);
        return paymentAccountMapper.toResponses(paymentAccounts);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'balance_' + #min + '_' + #max + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getPaymentAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        String logPrefix = "[PaymentAccountService:getPaymentAccountsByBalanceBetween]";
        log.info("{} Start getPaymentAccountsByBalanceBetween", logPrefix);
        Page<PaymentAccount> paymentAccounts = paymentAccountRepository.findAllPaymentAccountByBalanceBetween(min, max, pageable);
        log.info(PaymentAccountLogs.FOUND_LIST, logPrefix, paymentAccounts);
        return paymentAccountMapper.toResponses(paymentAccounts);
    }
//TODO После внедрения userId как первичного ключа исправить кэширование
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse depositPaymentAccountByUserId(Long userId, BigDecimal amount) {
        String logPrefix = "[PaymentAccountService:depositPaymentAccountByUserId]";
        log.info("{} Start depositPaymentAccountByUserId", logPrefix);
        PaymentAccount account = findPaymentAccount(userId, logPrefix);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setAccount(account);
        transaction.setType(TransactionType.DEPOSIT);

        log.info("{} Try Deposit Payment Account", logPrefix);
        int rows = paymentAccountRepository.depositPaymentAccount(userId, amount);
        if (rows == 0){
            log.warn("{} Can't Deposit Payment Account", logPrefix);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new UpdatePaymentAccountException("%s Can't deposit account", logPrefix);
        }
        entityManager.flush();
        entityManager.clear();
        log.info("{} Deposit Payment Account Successfully", logPrefix);

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        log.info(PaymentAccountLogs.TRANSACTION_SAVED, logPrefix, transaction);
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse withdrawPaymentAccountByUserId(Long userId, BigDecimal amount) {
        String logPrefix = "[PaymentAccountService:withdrawPaymentAccountByUserId]";
        log.info("{} Start withdrawPaymentAccountByUserId", logPrefix);
        PaymentAccount account = findPaymentAccount(userId, logPrefix);
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setAccount(account);
        transaction.setType(TransactionType.WITHDRAW);

        log.info("{} Try Withdraw Payment Account", logPrefix);
        int rows = paymentAccountRepository.withdrawPaymentAccount(userId, amount);
        if (rows == 0){
            log.warn("{} Can't Withdraw Payment Account", logPrefix);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new UpdatePaymentAccountException("%s Can't withdraw account", logPrefix);
        }
        entityManager.flush();
        entityManager.clear();
        log.info("{} Withdraw Payment Account Successfully", logPrefix);

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        log.info(PaymentAccountLogs.TRANSACTION_SAVED, logPrefix);
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId)));
    }



    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse activatePaymentAccountByUserId(Long userId) {
        String logPrefix = "[PaymentAccountService:activatePaymentAccountByUserId]";
        log.info("{} Start activatePaymentAccountByUserId", logPrefix);
        if(!paymentAccountRepository.existsById(userId)) {
            log.warn(PaymentAccountLogs.NOT_FOUND, logPrefix, userId);
            throw new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
        }
        log.info("{} Try Activate Payment Account", logPrefix);
        int rows = paymentAccountRepository.activatePaymentAccount(userId);
        if (rows == 0){
            log.warn("{} Can't Activate Payment Account", logPrefix);
            throw new UpdatePaymentAccountException("%s Can't activate account", logPrefix);
        }
        entityManager.flush();
        entityManager.clear();
        log.info("{} Payment Account Successfully Activated", logPrefix);

        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse deactivatePaymentAccountByUserId(Long userId) {
        String logPrefix = "[PaymentAccountService:deactivatePaymentAccountByUserId]";
        log.info("{} Start deactivatePaymentAccountByUserId", logPrefix);
        if(!paymentAccountRepository.existsById(userId)) {
            log.warn(PaymentAccountLogs.NOT_FOUND, logPrefix, userId);
            throw new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
        }
        log.info("{} Try Deactivate Payment Account", logPrefix);
        int rows = paymentAccountRepository.deactivatePaymentAccount(userId);
        if (rows == 0){
            log.warn("{} Can't Deactivate Payment Account", logPrefix);
            throw new UpdatePaymentAccountException("%s Can't deactivate account", logPrefix);
        }
        entityManager.flush();
        entityManager.clear();
        log.info("{} Payment Account Successfully Deactivated", logPrefix);

        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId)));
    }
}
