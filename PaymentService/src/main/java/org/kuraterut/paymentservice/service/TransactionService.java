package org.kuraterut.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.paymentservice.dto.request.CreateTransactionRequest;
import org.kuraterut.paymentservice.dto.response.TransactionListResponse;
import org.kuraterut.paymentservice.dto.response.TransactionResponse;
import org.kuraterut.paymentservice.exception.model.PaymentAccountNotFoundException;
import org.kuraterut.paymentservice.exception.model.TransactionNotFoundException;
import org.kuraterut.paymentservice.logger.PaymentAccountLogs;
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
@Slf4j
public class TransactionService implements GetTransactionUseCase, CreateTransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final PaymentAccountRepository paymentAccountRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public TransactionResponse createTransaction(CreateTransactionRequest request, Long userId) {
        String logPrefix = "[TransactionService:createTransaction]";
        log.info("[TransactionService:createTransaction] Start createTransaction");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:createTransaction] Payment account not found by userId: {}", userId);
                    return new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
                });
        log.info("[TransactionService:createTransaction] Payment account found: {}", paymentAccount);
        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setAccount(paymentAccount);
        transaction = transactionRepository.saveAndFlush(transaction);
        log.info("[TransactionService:createTransaction] Transaction created and saved: {}", transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_transactions_user_' + #userId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public TransactionListResponse getAllTransactionsByUserId(Long userId, Pageable pageable) {
        String logPrefix = "[TransactionService:getAllTransactionsByUserId]";
        log.info("[TransactionService:getAllTransactionsByUserId] Start getAllTransactionsAndUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:getAllTransactionsByUserId] Payment account not found by userId: {}", userId);
                    return new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
                });
        log.info("[TransactionService:getAllTransactionsByUserId] Payment account found: {}", paymentAccount);
        Page<Transaction> transactions = transactionRepository.findAllByAccountId(paymentAccount.getId(), pageable);
        log.info("[TransactionService:getAllTransactionsByUserId] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transaction_by_id_and_user_' + #id + '_' + #userId")
    public TransactionResponse getTransactionByIdAndUserId(Long id, Long userId) {
        String logPrefix = "[TransactionService:getTransactionByIdAndUserId]";
        log.info("[TransactionService:getTransactionByIdAndUserId] Start getTransactionByIdAndUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:getTransactionByIdAndUserId] Payment account not found by userId: {}", userId);
                    return new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
                });
        log.info("[TransactionService:getTransactionByIdAndUserId] Payment account found: {}", paymentAccount);
        log.info("[TransactionService:getTransactionByIdAndUserId] Try find Transaction by Id #{} and Account ID #{}", id, paymentAccount.getId());
        Optional<Transaction> transactionByAccountId = transactionRepository.findByIdAndAccountId(id, paymentAccount.getId());
        log.info("[TransactionService:getTransactionByIdAndUserId] Try find Transaction by Id #{}", id);
        Optional<Transaction> transactionById = transactionRepository.findById(id);
        if(transactionById.isPresent() && transactionByAccountId.isPresent()) {
            log.info("[TransactionService:getTransactionByIdAndUserId] Transaction found: {}", transactionById.get());
            return transactionMapper.toResponse(transactionById.get());
        } else {
            log.warn("[TransactionService:getTransactionByIdAndUserId] Transaction not found by ID: {}", id);
            throw new TransactionNotFoundException("Transaction not found by ID: " + id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_amount_between_' + #min + '_' + #max + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByAmountBetweenAndUserId(BigDecimal min, BigDecimal max, Long userId, Pageable pageable) {
        String logPrefix = "[TransactionService:getTransactionsByAmountBetweenAndUserId]";
        log.info("[TransactionService:getTransactionsByAmountBetweenAndUserId] Start getTransactionsByAmountBetweenAndUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:getTransactionsByAmountBetweenAndUserId] Payment account not found by userId: {}", userId);
                    return new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
                });
        log.info("[TransactionService:getTransactionsByAmountBetweenAndUserId] payment account found: {}", paymentAccount);
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndAmountBetween(paymentAccount.getId(), min, max, pageable);
        log.info("[TransactionService:getTransactionsByAmountBetweenAndUserId] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_type_' + #type.name() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionTypeAndUserId(TransactionType type, Long userId, Pageable pageable) {
        String logPrefix = "[TransactionService:getTransactionsByTransactionTypeAndUserId]";
        log.info("[TransactionService:getTransactionsByTransactionTypeAndUserId] Start getTransactionsByTransactionTypeAndUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:getTransactionsByTransactionTypeAndUserId] Payment account not found by userId: {}", userId);
                    return new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
                });
        log.info("[TransactionService:getTransactionsByTransactionTypeAndUserId] Payment account found: {}", paymentAccount);
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndType(paymentAccount.getId(), type, pageable);
        log.info("[TransactionService:getTransactionsByTransactionTypeAndUserId] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_status_' + #status.name() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionStatusAndUserId(TransactionStatus status, Long userId, Pageable pageable) {
        String logPrefix = "[TransactionService:getTransactionsByTransactionStatusAndUserId]";
        log.info("[TransactionService:getTransactionsByTransactionStatusAndUserId] Start getTransactionsByTransactionStatusAndUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:getTransactionsByTransactionStatusAndUserId] Payment account not found by userId: {}", userId);
                    return new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
                });
        log.info("[TransactionService:getTransactionsByTransactionStatusAndUserId] Payment account found: {}", paymentAccount);
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndStatus(paymentAccount.getId(), status, pageable);
        log.info("[TransactionService:getTransactionsByTransactionStatusAndUserId] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_order_' + #orderId + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable) {
        String logPrefix = "[TransactionService:getTransactionsByOrderIdAndUserId]";
        log.info("[TransactionService:getTransactionsByOrderIdAndUserId] Start getTransactionsByOrderIdAndUserId");
        PaymentAccount paymentAccount = paymentAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:getTransactionsByOrderIdAndUserId] Payment account not found by userId: {}", userId);
                    return new PaymentAccountNotFoundException(PaymentAccountLogs.NOT_FOUND_FORMAT, logPrefix, userId);
                });
        log.info("[TransactionService:getTransactionsByOrderIdAndUserId] Payment account found: {}", paymentAccount);
        Page<Transaction> transactions = transactionRepository.findAllByAccountIdAndOrderId(paymentAccount.getId(), orderId, pageable);
        log.info("[TransactionService:getTransactionsByOrderIdAndUserId] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transaction_by_id_' + #id")
    public TransactionResponse getTransactionById(Long id) {
        log.info("[TransactionService:getTransactionById] Start getTransactionById");
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TransactionService:getTransactionById] Payment account not found by ID: {}", id);
                    return new TransactionNotFoundException("Transaction not found by ID: " + id);
                });
        log.info("[TransactionService:getTransactionById] Payment account found: {}", transaction);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_transactions_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public TransactionListResponse getAllTransactions(Pageable pageable) {
        log.info("[TransactionService:getAllTransactions] Start getAllTransactions");
        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        log.info("[TransactionService:getAllTransactions] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_account_' + #paymentAccountId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByPaymentAccountId(Long paymentAccountId, Pageable pageable) {
        log.info("[TransactionService:getTransactionsByPaymentAccountId] Start getTransactionsByPaymentAccountId");
        Page<Transaction> transactions = transactionRepository.findAllByAccountId(paymentAccountId, pageable);
        log.info("[TransactionService:getTransactionsByPaymentAccountId] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_amount_between_' + #min + '_' + #max + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByAmountBetween(BigDecimal min, BigDecimal max, Pageable pageable) {
        log.info("[TransactionService:getTransactionsByAmountBetween] Start getTransactionsByAmountBetween");
        Page<Transaction> transactions = transactionRepository.findAllByAmountBetween(min, max, pageable);
        log.info("[TransactionService:getTransactionsByAmountBetween] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_type_' + #type.name() + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionType(TransactionType type, Pageable pageable) {
        log.info("[TransactionService:getTransactionsByTransactionType] Start getTransactionsByTransactionType");
        Page<Transaction> transactions = transactionRepository.findAllByType(type, pageable);
        log.info("[TransactionService:getTransactionsByTransactionType] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_status_' + #status.name() + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByTransactionStatus(TransactionStatus status, Pageable pageable) {
        log.info("[TransactionService:getTransactionsByTransactionStatus] Start getTransactionsByTransactionStatus");
        Page<Transaction> transactions = transactionRepository.findAllByStatus(status, pageable);
        log.info("[TransactionService:getTransactionsByTransactionStatus] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'transactions_order_' + #orderId + '_page_' + #pageable.pageNumber")
    public TransactionListResponse getTransactionsByOrderId(Long orderId, Pageable pageable) {
        log.info("[TransactionService:getTransactionsByOrderId] Start getTransactionsByOrderId");
        Page<Transaction> transactions = transactionRepository.findAllByOrderId(orderId, pageable);
        log.info("[TransactionService:getTransactionsByOrderId] Transactions found: {}", transactions);
        return transactionMapper.toResponses(transactions);
    }
}