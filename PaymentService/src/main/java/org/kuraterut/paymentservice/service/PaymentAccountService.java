package org.kuraterut.paymentservice.service;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.exception.model.*;
import org.kuraterut.paymentservice.mapper.PaymentAccountMapper;
import org.kuraterut.paymentservice.model.PaymentAccount;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.usecases.paymentaccount.CreatePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.DeletePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.GetPaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.UpdatePaymentAccountUseCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentAccountService implements CreatePaymentAccountUseCase, UpdatePaymentAccountUseCase,
        DeletePaymentAccountUseCase, GetPaymentAccountUseCase {

    //TODO Транзакции
    private final PaymentAccountRepository paymentAccountRepository;
    private final PaymentAccountMapper paymentAccountMapper;

    @Override
    @Transactional
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
    public List<PaymentAccountResponse> getAllPaymentAccounts() {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentAccountResponse getPaymentAccountByUserId(Long userId) {
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by user id: " + userId)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentAccountResponse getPaymentAccountById(Long id){
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Payment account not found by id: " + id)));
    }


    @Override
    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getPaymentAccountsByIsActive(boolean isActive) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findAllPaymentAccountByActive(isActive));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getPaymentAccountsByBalanceBetween(BigDecimal min, BigDecimal max) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findAllPaymentAccountByBalanceBetween(min, max));
    }

    @Override
    @Transactional
    public PaymentAccountResponse depositPaymentAccountByUserId(Long userId, BigDecimal amount) {
        if(!paymentAccountRepository.existsByUserId(userId)) {
            throw new PaymentAccountNotFoundException("Bank account not found by user id: " + userId);
        }
        int rows = paymentAccountRepository.depositPaymentAccountByUserId(userId, amount);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't deposit account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by userId: " + userId)));
    }

    @Override
    @Transactional
    public PaymentAccountResponse withdrawPaymentAccountByUserId(Long userId, BigDecimal amount) {
        if(!paymentAccountRepository.existsByUserId(userId)) {
            throw new PaymentAccountNotFoundException("Bank account not found by user id: " + userId);
        }
        int rows = paymentAccountRepository.withdrawPaymentAccountByUserId(userId, amount);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't withdraw account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by user id: " + userId)));
    }

    @Override
    @Transactional
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
