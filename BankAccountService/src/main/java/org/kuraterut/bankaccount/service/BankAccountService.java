package org.kuraterut.bankaccount.service;

import lombok.RequiredArgsConstructor;
import org.kuraterut.bankaccount.dto.request.CreateBankAccountRequest;
import org.kuraterut.bankaccount.dto.response.BankAccountResponse;
import org.kuraterut.bankaccount.exception.model.AccessDeniedException;
import org.kuraterut.bankaccount.exception.model.BankAccountIsNotEmptyException;
import org.kuraterut.bankaccount.exception.model.BankAccountNotFoundException;
import org.kuraterut.bankaccount.exception.model.UpdateBankAccountException;
import org.kuraterut.bankaccount.mapper.BankAccountMapper;
import org.kuraterut.bankaccount.model.BankAccount;
import org.kuraterut.bankaccount.model.Currency;
import org.kuraterut.bankaccount.repository.BankAccountRepository;
import org.kuraterut.bankaccount.usecases.bankaccount.CreateBankAccountUseCase;
import org.kuraterut.bankaccount.usecases.bankaccount.DeleteBankAccountUseCase;
import org.kuraterut.bankaccount.usecases.bankaccount.GetBankAccountUseCase;
import org.kuraterut.bankaccount.usecases.bankaccount.UpdateBankAccountUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BankAccountService implements CreateBankAccountUseCase, UpdateBankAccountUseCase,
        DeleteBankAccountUseCase, GetBankAccountUseCase {

    //TODO Транзакции
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountMapper bankAccountMapper;

    @Override
    @Transactional
    public BankAccountResponse createBankAccount(CreateBankAccountRequest request, Long userId) {
        BankAccount bankAccount = bankAccountMapper.toEntity(request, userId);
        bankAccount = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.toResponse(bankAccount);
    }

    @Override
    @Transactional
    public void deleteBankAccountById(Long id, Long userId) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found by id: " + id));
        if (!Objects.equals(bankAccount.getUserId(), userId)){
            throw new AccessDeniedException("You do not have permission to delete this bank account");
        }
        BigDecimal balance = bankAccount.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            String message = String.format("Can't delete bank account with id: %d, because balance is not zero: %f%s", id, balance, bankAccount.getCurrency().toString());
            throw new BankAccountIsNotEmptyException(message);
        }
        bankAccountRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBankAccountsByUserId(Long userId) {
        int countOfNotReadyBankAccounts = bankAccountRepository.checkReadyForDeletingByUserId(userId);
        if (countOfNotReadyBankAccounts != 0){
            String message = String.format("There are %d bank accounts, with non zero balance", countOfNotReadyBankAccounts);
            throw new BankAccountIsNotEmptyException(message);
        }
        bankAccountRepository.deleteAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getAllBankAccounts() {
        return bankAccountMapper.toResponses(bankAccountRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountResponse getBankAccountById(Long id, Long userId) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found by id: " + id));
        if (!Objects.equals(bankAccount.getUserId(), userId)){
            throw new AccessDeniedException("You don't have permission to get this bank account");
        }
        return bankAccountMapper.toResponse(bankAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getBankAccountsByUserId(Long userId) {
        return bankAccountMapper.toResponses(bankAccountRepository.findBankAccountByUserId(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getBankAccountsByCurrency(Currency currency, Long userId) {
        return bankAccountMapper.toResponses(bankAccountRepository.findBankAccountByCurrencyAndUserId(currency, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getBankAccountsByIsActive(boolean isActive, Long userId) {
        return bankAccountMapper.toResponses(bankAccountRepository.findBankAccountByActiveAndUserId(isActive, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountResponse> getBankAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Long userId) {
        return bankAccountMapper.toResponses(bankAccountRepository.findBankAccountByBalanceBetweenAndUserId(min, max, userId));
    }

    @Override
    @Transactional
    public BankAccountResponse depositBankAccount(Long id, BigDecimal amount, Long userId) {
        if(!bankAccountRepository.existsById(id)) {
            throw new BankAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!bankAccountRepository.existsBankAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to deposit this account");
        }
        int rows = bankAccountRepository.depositBankAccountById(id, amount);
        if (rows == 0){
            throw new UpdateBankAccountException("Can't deposit account");
        }
        return bankAccountMapper.toResponse(bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    public BankAccountResponse withdrawBankAccount(Long id, BigDecimal amount, Long userId) {
        if(!bankAccountRepository.existsById(id)) {
            throw new BankAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!bankAccountRepository.existsBankAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to withdraw this account");
        }
        int rows = bankAccountRepository.withdrawBankAccountById(id, amount);
        if (rows == 0){
            throw new UpdateBankAccountException("Can't withdraw account");
        }
        return bankAccountMapper.toResponse(bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    public BankAccountResponse activateBankAccount(Long id, Long userId) {
        if(!bankAccountRepository.existsById(id)) {
            throw new BankAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!bankAccountRepository.existsBankAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to activate this account");
        }
        int rows = bankAccountRepository.activateBankAccountById(id);
        if (rows == 0){
            throw new UpdateBankAccountException("Can't activate account");
        }
        return bankAccountMapper.toResponse(bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found by id: " + id)));
    }

    @Override
    @Transactional
    public BankAccountResponse deactivateBankAccount(Long id, Long userId) {
        if(!bankAccountRepository.existsById(id)) {
            throw new BankAccountNotFoundException("Bank account not found by id: " + id);
        }
        if (!bankAccountRepository.existsBankAccountByIdAndUserId(id, userId)) {
            throw new AccessDeniedException("You don't have permission to deactivate this account");
        }
        int rows = bankAccountRepository.deactivateBankAccountById(id);
        if (rows == 0){
            throw new UpdateBankAccountException("Can't deactivate account");
        }
        return bankAccountMapper.toResponse(bankAccountRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account not found by id: " + id)));
    }
}
