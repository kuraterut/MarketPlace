package org.kuraterut.paymentservice.service;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
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
public class PaymentAccountService implements CreatePaymentAccountUseCase, UpdatePaymentAccountUseCase,
        DeletePaymentAccountUseCase, GetPaymentAccountUseCase {

    private final PaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;
    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse createPaymentAccount(Long userId) {
        try {
            PaymentAccount paymentAccount = paymentAccountMapper.toEntity(userId);
            paymentAccount = paymentAccountRepository.save(paymentAccount);
            return paymentAccountMapper.toResponse(paymentAccount);
        } catch (DataIntegrityViolationException | ConstraintViolationException e){
            throw new PaymentAccountAlreadyExistsException("Payment Account is already exists with userId: " + userId);
        }
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deletePaymentAccountById(Long id) {
        PaymentAccount paymentAccount = paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by id: " + id));

        BigDecimal balance = paymentAccount.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            String message = String.format("Can't delete payment account with id: %d, because balance is not zero: %f", id, balance);
            throw new PaymentAccountIsNotEmptyException(message);
        }
        paymentAccountRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deletePaymentAccountByUserId(Long userId) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by userId: " + userId));

        BigDecimal balance = paymentAccount.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            String message = String.format("Can't delete payment account with userId: %d, because balance is not zero: %f", userId, balance);
            throw new PaymentAccountIsNotEmptyException(message);
        }
        paymentAccountRepository.deleteById(paymentAccount.getId());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getAllPaymentAccounts(Pageable pageable) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'user_' + #userId")
    public PaymentAccountResponse getPaymentAccountByUserId(Long userId) {
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by user id: " + userId)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public PaymentAccountResponse getPaymentAccountById(Long id){
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by id: " + id)));
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'active_' + #isActive + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getPaymentAccountsByIsActive(boolean isActive, Pageable pageable) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findAllPaymentAccountByActive(isActive, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'balance_' + #min + '_' + #max + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PaymentAccountListResponse getPaymentAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findAllPaymentAccountByBalanceBetween(min, max, pageable));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse depositPaymentAccountByUserId(Long userId, BigDecimal amount) {
        PaymentAccount account = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by user id: " + userId));
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setAccount(account);
        transaction.setType(TransactionType.DEPOSIT);

        int rows = paymentAccountRepository.depositPaymentAccountByUserId(userId, amount);
        if (rows == 0){
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new UpdatePaymentAccountException("Can't deposit account");
        }
        // Очищаем Persistence Context для данного entity
        entityManager.flush();
        entityManager.clear();

        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by userId: " + userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse withdrawPaymentAccountByUserId(Long userId, BigDecimal amount) {
        PaymentAccount account = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by user id: " + userId));
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setAccount(account);
        transaction.setType(TransactionType.WITHDRAW);

        int rows = paymentAccountRepository.withdrawPaymentAccountByUserId(userId, amount);
        if (rows == 0){
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new UpdatePaymentAccountException("Can't withdraw account");
        }
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by user id: " + userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse activatePaymentAccountById(Long id) {
        if(!paymentAccountRepository.existsById(id)) {
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        int rows = paymentAccountRepository.activatePaymentAccountById(id);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't activate account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse activatePaymentAccountByUserId(Long userId) {
        if(!paymentAccountRepository.existsByUserId(userId)) {
            throw new PaymentAccountNotFoundException("Bank account not found by user id: " + userId);
        }
        int rows = paymentAccountRepository.activatePaymentAccountByUserId(userId);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't activate account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by user id: " + userId)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse deactivatePaymentAccountById(Long id) {
        if(!paymentAccountRepository.existsById(id)) {
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        int rows = paymentAccountRepository.deactivatePaymentAccountById(id);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't deactivate account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public PaymentAccountResponse deactivatePaymentAccountByUserId(Long userId) {
        if(!paymentAccountRepository.existsByUserId(userId)) {
            throw new PaymentAccountNotFoundException("Bank account not found by user id: " + userId);
        }
        int rows = paymentAccountRepository.deactivatePaymentAccountByUserId(userId);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't deactivate account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by user id: " + userId)));
    }
}
