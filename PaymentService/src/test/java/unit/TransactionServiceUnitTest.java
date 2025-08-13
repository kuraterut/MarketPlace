package unit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.kuraterut.paymentservice.service.TransactionService;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUnitTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PaymentAccountRepository paymentAccountRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private PaymentAccount paymentAccount;
    private Transaction transaction;
    private TransactionResponse transactionResponse;

    private final OffsetDateTime testCreatedAtDateTime = OffsetDateTime.of(2025, 8, 12, 0, 0, 0, 0, ZoneOffset.ofHours(1));
    private final OffsetDateTime testUpdatedAtDateTime = OffsetDateTime.of(2025, 8, 12, 0, 0, 0, 0, ZoneOffset.ofHours(1));


    @BeforeEach
    void setUp() {
        paymentAccount = new PaymentAccount();
        paymentAccount.setId(1L);
        paymentAccount.setUserId(100L);

        transaction = new Transaction();
        transaction.setId(10L);
        transaction.setAccount(paymentAccount);

        transactionResponse = new TransactionResponse(1L, 1L, BigDecimal.TEN,
                TransactionType.PAYMENT, "testDescription", TransactionStatus.COMPLETED,
                1L, testCreatedAtDateTime.toString(), testUpdatedAtDateTime.toString());
    }

    @Test
    void createTransaction_success() {
        CreateTransactionRequest request = new CreateTransactionRequest(BigDecimal.TEN, TransactionType.PAYMENT, "testDescription", 1L);

        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(paymentAccount));
        when(transactionMapper.toEntity(request)).thenReturn(transaction);
        when(transactionRepository.saveAndFlush(transaction)).thenReturn(transaction);
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = transactionService.createTransaction(request, 100L);

        assertThat(result).isEqualTo(transactionResponse);
        verify(transactionRepository).saveAndFlush(transaction);
    }

    @Test
    void createTransaction_accountNotFound() {
        when(paymentAccountRepository.findByUserId(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(new CreateTransactionRequest(), 200L))
                .isInstanceOf(PaymentAccountNotFoundException.class);
    }

    @Test
    void getTransactionByIdAndUser_success() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(paymentAccount));
        when(transactionRepository.findByIdAndAccountId(10L, 1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(transactionResponse);

        TransactionResponse result = transactionService.getTransactionById(10L, 100L);

        assertThat(result).isEqualTo(transactionResponse);
    }

    @Test
    void getTransactionByIdAndUser_notFound() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(paymentAccount));
        when(transactionRepository.findByIdAndAccountId(10L, 1L)).thenReturn(Optional.empty());
        when(transactionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(10L, 100L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void getAllTransactionsByUser_success() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> page = new PageImpl<>(List.of(transaction));
        TransactionListResponse listResponse = new TransactionListResponse(List.of(transactionResponse));

        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(paymentAccount));
        when(transactionRepository.findAllByAccountId(1L, pageable)).thenReturn(page);
        when(transactionMapper.toResponses(page)).thenReturn(listResponse);

        TransactionListResponse result = transactionService.getAllTransactions(100L, pageable);

        assertThat(result).isEqualTo(listResponse);
    }
}
