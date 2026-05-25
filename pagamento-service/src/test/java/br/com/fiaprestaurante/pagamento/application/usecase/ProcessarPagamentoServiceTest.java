package br.com.fiaprestaurante.pagamento.application.usecase;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoAprovadoEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoPendenteEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;
import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import br.com.fiaprestaurante.pagamento.application.port.output.PaymentEventPublisher;
import br.com.fiaprestaurante.pagamento.application.port.output.PaymentGateway;
import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link ProcessarPagamentoService} - cobrem o fluxo
 * principal de aprovacao, fallback de pendencia e a idempotencia em caso
 * de pedido ja aprovado.
 *
 * @author Danilo Fernando
 */
class ProcessarPagamentoServiceTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final BigDecimal VALOR = new BigDecimal("59.30");

    private PagamentoRepository repository;
    private PaymentGateway gateway;
    private PaymentEventPublisher publisher;
    private ProcessarPagamentoService service;

    @BeforeEach
    void setUp() {
        repository = mock(PagamentoRepository.class);
        gateway = mock(PaymentGateway.class);
        publisher = mock(PaymentEventPublisher.class);
        service = new ProcessarPagamentoService(repository, gateway, publisher);
    }

    private Pagamento jaAprovado() {
        return new Pagamento(UUID.randomUUID(), PEDIDO_ID, VALOR,
                StatusPagamento.APROVADO, 1, null, Instant.now(), Instant.now());
    }

    @Test
    void deveSerIdempotenteQuandoPagamentoJaAprovado() {
        Pagamento existente = jaAprovado();
        when(repository.buscarPorPedidoId(PEDIDO_ID)).thenReturn(Optional.of(existente));

        PagamentoResponse resp = service.executar(new ProcessarPagamentoCommand(PEDIDO_ID, VALOR));

        assertThat(resp.status()).isEqualTo("APROVADO");
        verify(gateway, never()).processar(any(), any());
        verify(repository, never()).salvar(any());
        verify(publisher, never()).publicarPagamentoAprovado(any());
    }

    @Test
    void deveAprovarPublicarEventoESalvarQuandoGatewayRetornaTrue() {
        when(repository.buscarPorPedidoId(PEDIDO_ID)).thenReturn(Optional.empty());
        when(gateway.processar(PEDIDO_ID, VALOR)).thenReturn(true);
        when(repository.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResponse resp = service.executar(new ProcessarPagamentoCommand(PEDIDO_ID, VALOR));

        assertThat(resp.status()).isEqualTo("APROVADO");
        assertThat(resp.tentativas()).isEqualTo(1);
        verify(repository).salvar(any(Pagamento.class));
        verify(publisher).publicarPagamentoAprovado(any(PagamentoAprovadoEvent.class));
        verify(publisher, never()).publicarPagamentoPendente(any());
    }

    @Test
    void deveMarcarPendenteEPublicarQuandoGatewayRetornaFalse() {
        when(repository.buscarPorPedidoId(PEDIDO_ID)).thenReturn(Optional.empty());
        when(gateway.processar(PEDIDO_ID, VALOR)).thenReturn(false);
        when(repository.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResponse resp = service.executar(new ProcessarPagamentoCommand(PEDIDO_ID, VALOR));

        assertThat(resp.status()).isEqualTo("PENDENTE");
        assertThat(resp.motivoFalha()).isNotBlank();
        verify(publisher).publicarPagamentoPendente(any(PagamentoPendenteEvent.class));
        verify(publisher, never()).publicarPagamentoAprovado(any());
    }

    @Test
    void deveMarcarPendenteQuandoGatewayLancaExcecao() {
        when(repository.buscarPorPedidoId(PEDIDO_ID)).thenReturn(Optional.empty());
        when(gateway.processar(PEDIDO_ID, VALOR))
                .thenThrow(new RuntimeException("connection refused"));
        when(repository.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResponse resp = service.executar(new ProcessarPagamentoCommand(PEDIDO_ID, VALOR));

        assertThat(resp.status()).isEqualTo("PENDENTE");
        verify(publisher).publicarPagamentoPendente(any(PagamentoPendenteEvent.class));
    }

    @Test
    void deveIncrementarTentativasNoPagamentoExistentePendente() {
        Pagamento existente = new Pagamento(UUID.randomUUID(), PEDIDO_ID, VALOR,
                StatusPagamento.PENDENTE, 2, "falha anterior", Instant.now(), Instant.now());
        when(repository.buscarPorPedidoId(PEDIDO_ID)).thenReturn(Optional.of(existente));
        when(gateway.processar(eq(PEDIDO_ID), any(BigDecimal.class))).thenReturn(true);
        when(repository.salvar(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));

        PagamentoResponse resp = service.executar(new ProcessarPagamentoCommand(PEDIDO_ID, VALOR));

        assertThat(resp.status()).isEqualTo("APROVADO");
        assertThat(resp.tentativas()).isEqualTo(3);
    }
}
