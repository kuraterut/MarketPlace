package org.kuraterut.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.kuraterut.paymentservice.dto.request.CreateTransactionRequest;
import org.kuraterut.paymentservice.dto.response.TransactionListResponse;
import org.kuraterut.paymentservice.dto.response.TransactionResponse;
import org.kuraterut.paymentservice.exception.model.PaymentAccountNotFoundException;
import org.kuraterut.paymentservice.exception.model.TransactionNotFoundException;
import org.kuraterut.paymentservice.mapper.TransactionMapper;
import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.kuraterut.paymentservice.model.entity.Transaction;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.repository.TransactionRepository;
import org.kuraterut.paymentservice.usecases.transaction.CreateTransactionUseCase;
import org.kuraterut.paymentservice.usecases.transaction.GetTransactionUseCase;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "transactions")
public class TransactionService implements GetTransactionUseCase, CreateTransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final PaymentAccountRepository paymentAccountRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public TransactionResponse createTransaction(CreateTransactionRequest request, Long userId) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Account not found by user ID: " + userId));
        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setAccount(paymentAccount);
        transaction = transactionRepository.save(transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_transactions_user_' + #userId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public TransactionListResponse getAllTransactions(Long userId, Pageable pageable) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Account not found by ID: " + userId));
        Page<Transaction> transactions = transactionRepository.findAllByAccountId(paymentAccount.getId(), pageable);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transaction_by_id_and_user_' + #id + '_' + #userId")
    public TransactionResponse getTransactionById(Long id, Long userId) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Account not found by ID: " + userId));
        Optional<Transaction> transactionByAccountId = transactionRepository.findByIdAndAccountId(id, paymentAccount.getId());
        Optional<Transaction> transactionById = transactionRepository.findById(id);
        if(transactionById.isPresent() && transactionByAccountId.isPresent()) {
            return transactionMapper.toResponse(transactionById.get());
        } else {
            throw new TransactionNotFoundException("Transaction not found by ID: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_amount_between_' + #min + '_' + #max + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByAmountBetween(BigDecimal min, BigDecimal max, Long userId, Pageable pageable) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Account not found by ID: " + userId));
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndAmountBetween(paymentAccount.getId(), min, max, pageable);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_type_' + #type.name() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionType(TransactionType type, Long userId, Pageable pageable) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Account not found by ID: " + userId));
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndType(paymentAccount.getId(), type, pageable);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_status_' + #status.name() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionStatus(TransactionStatus status, Long userId, Pageable pageable) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Account not found by ID: " + userId));
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndStatus(paymentAccount.getId(), status, pageable);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_order_' + #orderId + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByOrderId(Long orderId, Long userId, Pageable pageable) {
        PaymentAccount paymentAccount = paymentAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new PaymentAccountNotFoundException("Account not found by ID: " + userId));
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndOrderId(paymentAccount.getId(), orderId, pageable);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transaction_by_id_' + #id")
    public TransactionResponse getTransactionById(Long id) {
        return transactionMapper.toResponse(transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found by ID: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_transactions_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public TransactionListResponse getAllTransactions(Pageable pageable) {
        return transactionMapper.toResponses(transactionRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_account_' + #paymentAccountId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByPaymentAccountId(Long paymentAccountId, Pageable pageable) {
        return transactionMapper.toResponses(transactionRepository.findAllByAccountId(paymentAccountId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_amount_between_' + #min + '_' + #max + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByAmountBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        return transactionMapper.toResponses(transactionRepository.findAllByAmountBetween(min, max, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_type_' + #type.name() + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionType(TransactionType type, Pageable pageable) {
        return transactionMapper.toResponses(transactionRepository.findAllByType(type, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_status_' + #status.name() + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionStatus(TransactionStatus status, Pageable pageable) {
        return transactionMapper.toResponses(transactionRepository.findAllByStatus(status, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_order_' + #orderId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByOrderId(Long orderId, Pageable pageable) {
        return transactionMapper.toResponses(transactionRepository.findAllByOrderId(orderId, pageable));
    }
}