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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessarPagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private PaymentEventPublisher eventPublisher;

    private ProcessarPagamentoService service;

    @BeforeEach
    void setUp() {
        service = new ProcessarPagamentoService(pagamentoRepository, paymentGateway, eventPublisher);
    }

    @Test
    void deveRetornarPagamentoJaAprovadoSemChamarGateway() {
        UUID pedidoId = UUID.randomUUID();
        Pagamento aprovado = pagamento(StatusPagamento.APROVADO, pedidoId, 1, null);
        when(pagamentoRepository.buscarPorPedidoId(pedidoId)).thenReturn(Optional.of(aprovado));

        PagamentoResponse response = service.executar(new ProcessarPagamentoCommand(pedidoId, new BigDecimal("25.00")));

        assertThat(response.status()).isEqualTo("APROVADO");
        assertThat(response.id()).isEqualTo(aprovado.getId());
        verifyNoInteractions(paymentGateway, eventPublisher);
        verify(pagamentoRepository, never()).salvar(any());
    }

    @Test
    void deveCriarPagamentoAprovadoEPublicarEvento() {
        UUID pedidoId = UUID.randomUUID();
        when(pagamentoRepository.buscarPorPedidoId(pedidoId)).thenReturn(Optional.empty());
        when(paymentGateway.processar(pedidoId, new BigDecimal("25.00"))).thenReturn(true);
        when(pagamentoRepository.salvar(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagamentoResponse response = service.executar(new ProcessarPagamentoCommand(pedidoId, new BigDecimal("25.00")));

        assertThat(response.status()).isEqualTo("APROVADO");
        assertThat(response.tentativas()).isEqualTo(1);
        assertThat(response.motivoFalha()).isNull();

        ArgumentCaptor<PagamentoAprovadoEvent> eventCaptor = ArgumentCaptor.forClass(PagamentoAprovadoEvent.class);
        verify(eventPublisher).publicarPagamentoAprovado(eventCaptor.capture());
        verify(eventPublisher, never()).publicarPagamentoPendente(any());
        assertThat(eventCaptor.getValue().pedidoId()).isEqualTo(pedidoId);
        assertThat(eventCaptor.getValue().pagamentoId()).isEqualTo(response.id());
        assertThat(eventCaptor.getValue().timestamp()).isNotNull();
    }

    @Test
    void deveMarcarComoPendenteQuandoGatewayRetornaFalha() {
        UUID pedidoId = UUID.randomUUID();
        Pagamento existente = pagamento(StatusPagamento.PENDENTE, pedidoId, 2, "falha anterior");
        when(pagamentoRepository.buscarPorPedidoId(pedidoId)).thenReturn(Optional.of(existente));
        when(paymentGateway.processar(pedidoId, new BigDecimal("25.00"))).thenReturn(false);
        when(pagamentoRepository.salvar(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagamentoResponse response = service.executar(new ProcessarPagamentoCommand(pedidoId, new BigDecimal("25.00")));

        assertThat(response.status()).isEqualTo("PENDENTE");
        assertThat(response.tentativas()).isEqualTo(3);
        assertThat(response.motivoFalha()).isEqualTo("Falha ao processar pagamento no serviço externo");

        ArgumentCaptor<PagamentoPendenteEvent> eventCaptor = ArgumentCaptor.forClass(PagamentoPendenteEvent.class);
        verify(eventPublisher).publicarPagamentoPendente(eventCaptor.capture());
        verify(eventPublisher, never()).publicarPagamentoAprovado(any());
        assertThat(eventCaptor.getValue().pedidoId()).isEqualTo(pedidoId);
        assertThat(eventCaptor.getValue().motivo()).isEqualTo(response.motivoFalha());
    }

    @Test
    void deveMarcarComoPendenteQuandoGatewayLancaExcecao() {
        UUID pedidoId = UUID.randomUUID();
        when(pagamentoRepository.buscarPorPedidoId(pedidoId)).thenReturn(Optional.empty());
        when(paymentGateway.processar(pedidoId, new BigDecimal("25.00"))).thenThrow(new RuntimeException("timeout"));
        when(pagamentoRepository.salvar(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PagamentoResponse response = service.executar(new ProcessarPagamentoCommand(pedidoId, new BigDecimal("25.00")));

        assertThat(response.status()).isEqualTo("PENDENTE");
        assertThat(response.tentativas()).isEqualTo(1);
        verify(eventPublisher).publicarPagamentoPendente(any(PagamentoPendenteEvent.class));
    }

    private static Pagamento pagamento(StatusPagamento status, UUID pedidoId, int tentativas, String motivoFalha) {
        return new Pagamento(
                UUID.randomUUID(),
                pedidoId,
                new BigDecimal("25.00"),
                status,
                tentativas,
                motivoFalha,
                Instant.parse("2026-05-21T10:00:00Z"),
                Instant.parse("2026-05-21T10:01:00Z")
        );
    }
}
