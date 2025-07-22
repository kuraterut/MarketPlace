package org.kuraterut.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.kuraterut.paymentservice.dto.request.CreatePaymentAccountRequest;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.exception.model.AccessDeniedException;
import org.kuraterut.paymentservice.exception.model.PaymentAccountIsNotEmptyException;
import org.kuraterut.paymentservice.exception.model.PaymentAccountNotFoundException;
import org.kuraterut.paymentservice.exception.model.UpdatePaymentAccountException;
import org.kuraterut.paymentservice.mapper.PaymentAccountMapper;
import org.kuraterut.paymentservice.model.PaymentAccount;
import org.kuraterut.paymentservice.model.Currency;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.usecases.bankaccount.CreatePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.bankaccount.DeletePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.bankaccount.GetPaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.bankaccount.UpdatePaymentAccountUseCase;
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
    public PaymentAccountResponse createBankAccount(CreatePaymentAccountRequest request, Long userId) {
        PaymentAccount paymentAccount = paymentAccountMapper.toEntity(request, userId);
        paymentAccount = paymentAccountRepository.save(paymentAccount);
        return paymentAccountMapper.toResponse(paymentAccount);
    }

    @Override
    @Transactional
    public void deleteBankAccountById(Long id, Long userId) {
        PaymentAccount paymentAccount = paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id));
        if (!Objects.equals(paymentAccount.getUserId(), userId)){
            throw new AccessDeniedException("You do not have permission to delete this bank account");
        }
        BigDecimal balance = paymentAccount.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            String message = String.format("Can't delete bank account with id: %d, because balance is not zero: %f%s", id, balance, paymentAccount.getCurrency().toString());
            throw new PaymentAccountIsNotEmptyException(message);
        }
        paymentAccountRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBankAccountsByUserId(Long userId) {
        int countOfNotReadyBankAccounts = paymentAccountRepository.checkReadyForDeletingByUserId(userId);
        if (countOfNotReadyBankAccounts != 0){
            String message = String.format("There are %d bank accounts, with non zero balance", countOfNotReadyBankAccounts);
            throw new PaymentAccountIsNotEmptyException(message);
        }
        paymentAccountRepository.deleteAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getAllBankAccounts() {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentAccountResponse getBankAccountById(Long id, Long userId) {
        PaymentAccount paymentAccount = paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id));
        if (!Objects.equals(paymentAccount.getUserId(), userId)){
            throw new AccessDeniedException("You don't have permission to get this bank account");
        }
        return paymentAccountMapper.toResponse(paymentAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getBankAccountsByUserId(Long userId) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findPaymentAccountByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getBankAccountsByCurrency(Currency currency, Long userId) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findPaymentAccountByCurrencyAndUserId(currency, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getBankAccountsByIsActive(boolean isActive, Long userId) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findPaymentAccountByActiveAndUserId(isActive, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentAccountResponse> getBankAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Long userId) {
        return paymentAccountMapper.toResponses(paymentAccountRepository.findPaymentAccountByBalanceBetweenAndUserId(min, max, userId));
    }

    @Override
    @Transactional
    public PaymentAccountResponse depositBankAccount(Long id, BigDecimal amount, Long userId) {
        if(!paymentAccountRepository.existsById(id)) {
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!paymentAccountRepository.existsPaymentAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to deposit this account");
        }
        int rows = paymentAccountRepository.depositPaymentAccountById(id, amount);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't deposit account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    public PaymentAccountResponse withdrawBankAccount(Long id, BigDecimal amount, Long userId) {
        if(!paymentAccountRepository.existsById(id)) {
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!paymentAccountRepository.existsPaymentAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to withdraw this account");
        }
        int rows = paymentAccountRepository.withdrawPaymentAccountById(id, amount);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't withdraw account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    public PaymentAccountResponse activateBankAccount(Long id, Long userId) {
        if(!paymentAccountRepository.existsById(id)) {
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!paymentAccountRepository.existsPaymentAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to activate this account");
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
    public PaymentAccountResponse deactivateBankAccount(Long id, Long userId) {
        if(!paymentAccountRepository.existsById(id)) {
            throw new PaymentAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!paymentAccountRepository.existsPaymentAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to deactivate this account");
        }
        int rows = paymentAccountRepository.deactivatePaymentAccountById(id);
        if (rows == 0){
            throw new UpdatePaymentAccountException("Can't deactivate account");
        }
        return paymentAccountMapper.toResponse(paymentAccountRepository.findById(id)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Bank account not found by id: " + id)));
    }
}
