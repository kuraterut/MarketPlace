package unit;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuraterut.paymentservice.dto.response.PaymentAccountListResponse;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.exception.model.PaymentAccountAlreadyExistsException;
import org.kuraterut.paymentservice.exception.model.PaymentAccountIsNotEmptyException;
import org.kuraterut.paymentservice.exception.model.PaymentAccountNotFoundException;
import org.kuraterut.paymentservice.exception.model.UpdatePaymentAccountException;
import org.kuraterut.paymentservice.mapper.PaymentAccountMapper;
import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.kuraterut.paymentservice.model.entity.Transaction;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.repository.TransactionRepository;
import org.kuraterut.paymentservice.service.PaymentAccountService;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentAccountServiceUnitTest {

    @Mock
    private PaymentAccountRepository paymentAccountRepository;
    @Mock
    private PaymentAccountMapper paymentAccountMapper;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private PaymentAccountService paymentAccountService;

    private PaymentAccount account;
    private PaymentAccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        account = new PaymentAccount();
        account.setId(1L);
        account.setUserId(100L);
        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);

        accountResponse = new PaymentAccountResponse();
        accountResponse.setId(1L);
        accountResponse.setUserId(100L);
        accountResponse.setBalance(BigDecimal.ZERO);
        accountResponse.setActive(true);
    }

    @Test
    void createPaymentAccount_success() {
        when(paymentAccountMapper.toEntity(100L)).thenReturn(account);
        when(paymentAccountRepository.saveAndFlush(account)).thenReturn(account);
        when(paymentAccountMapper.toResponse(account)).thenReturn(accountResponse);

        PaymentAccountResponse result = paymentAccountService.createPaymentAccount(100L);

        assertThat(result).isEqualTo(accountResponse);
        verify(paymentAccountRepository).saveAndFlush(account);
    }

    @Test
    void createPaymentAccount_alreadyExists() {
        when(paymentAccountMapper.toEntity(100L)).thenReturn(account);
        when(paymentAccountRepository.saveAndFlush(account))
                .thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> paymentAccountService.createPaymentAccount(100L))
                .isInstanceOf(PaymentAccountAlreadyExistsException.class);
    }

    @Test
    void deletePaymentAccountById_success() {
        when(paymentAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        paymentAccountService.deletePaymentAccountById(1L);

        verify(paymentAccountRepository).deleteById(1L);
    }

    @Test
    void deletePaymentAccountById_notFound() {
        when(paymentAccountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentAccountService.deletePaymentAccountById(1L))
                .isInstanceOf(PaymentAccountNotFoundException.class);
    }

    @Test
    void deletePaymentAccountById_notEmpty() {
        account.setBalance(BigDecimal.TEN);
        when(paymentAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> paymentAccountService.deletePaymentAccountById(1L))
                .isInstanceOf(PaymentAccountIsNotEmptyException.class);
    }

    @Test
    void getAllPaymentAccounts_success() {
        var pageable = PageRequest.of(0, 10);
        when(paymentAccountRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(account)));
        var listResponse = new PaymentAccountListResponse();
        when(paymentAccountMapper.toResponses(any())).thenReturn(listResponse);

        PaymentAccountListResponse result = paymentAccountService.getAllPaymentAccounts(pageable);

        assertThat(result).isEqualTo(listResponse);
    }

    @Test
    void getPaymentAccountByUserId_success() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(account));
        when(paymentAccountMapper.toResponse(account)).thenReturn(accountResponse);

        var result = paymentAccountService.getPaymentAccountByUserId(100L);

        assertThat(result).isEqualTo(accountResponse);
    }

    @Test
    void getPaymentAccountByUserId_notFound() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentAccountService.getPaymentAccountByUserId(100L))
                .isInstanceOf(PaymentAccountNotFoundException.class);
    }

    @Test
    void depositPaymentAccountByUserId_success() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(account));
        when(paymentAccountRepository.depositPaymentAccountByUserId(100L, BigDecimal.TEN)).thenReturn(1);
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(account));
        when(paymentAccountMapper.toResponse(account)).thenReturn(accountResponse);

        var result = paymentAccountService.depositPaymentAccountByUserId(100L, BigDecimal.TEN);

        assertThat(result).isEqualTo(accountResponse);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void depositPaymentAccountByUserId_fail() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(account));
        when(paymentAccountRepository.depositPaymentAccountByUserId(100L, BigDecimal.TEN)).thenReturn(0);

        assertThatThrownBy(() -> paymentAccountService.depositPaymentAccountByUserId(100L, BigDecimal.TEN))
                .isInstanceOf(UpdatePaymentAccountException.class);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void withdrawPaymentAccountByUserId_success() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(account));
        when(paymentAccountRepository.withdrawPaymentAccountByUserId(100L, BigDecimal.ONE)).thenReturn(1);
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(account));
        when(paymentAccountMapper.toResponse(account)).thenReturn(accountResponse);

        var result = paymentAccountService.withdrawPaymentAccountByUserId(100L, BigDecimal.ONE);

        assertThat(result).isEqualTo(accountResponse);
    }

    @Test
    void withdrawPaymentAccountByUserId_fail() {
        when(paymentAccountRepository.findByUserId(100L)).thenReturn(Optional.of(account));
        when(paymentAccountRepository.withdrawPaymentAccountByUserId(100L, BigDecimal.ONE)).thenReturn(0);

        assertThatThrownBy(() -> paymentAccountService.withdrawPaymentAccountByUserId(100L, BigDecimal.ONE))
                .isInstanceOf(UpdatePaymentAccountException.class);
    }
}