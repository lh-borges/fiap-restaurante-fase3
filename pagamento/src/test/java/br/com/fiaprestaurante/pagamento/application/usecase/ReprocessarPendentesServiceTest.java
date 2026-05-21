package br.com.fiaprestaurante.pagamento.application.usecase;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;
import br.com.fiaprestaurante.pagamento.application.port.input.ProcessarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ReprocessarPendentesServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private ProcessarPagamentoUseCase processarPagamento;

    private ReprocessarPendentesService service;

    @BeforeEach
    void setUp() {
        service = new ReprocessarPendentesService(pagamentoRepository, processarPagamento);
    }

    @Test
    void deveRetornarZeroQuandoNaoHaPendentes() {
        when(pagamentoRepository.listarPendentes(10)).thenReturn(List.of());

        int aprovados = service.executar(10);

        assertThat(aprovados).isZero();
        verifyNoInteractions(processarPagamento);
    }

    @Test
    void deveReprocessarPendentesEContarAprovados() {
        Pagamento primeiro = pagamento(UUID.randomUUID(), new BigDecimal("10.00"));
        Pagamento segundo = pagamento(UUID.randomUUID(), new BigDecimal("20.00"));
        when(pagamentoRepository.listarPendentes(10)).thenReturn(List.of(primeiro, segundo));
        when(processarPagamento.executar(new ProcessarPagamentoCommand(primeiro.getPedidoId(), primeiro.getValor())))
                .thenReturn(response(primeiro, "APROVADO"));
        when(processarPagamento.executar(new ProcessarPagamentoCommand(segundo.getPedidoId(), segundo.getValor())))
                .thenReturn(response(segundo, "PENDENTE"));

        int aprovados = service.executar(10);

        assertThat(aprovados).isEqualTo(1);
        ArgumentCaptor<ProcessarPagamentoCommand> captor = ArgumentCaptor.forClass(ProcessarPagamentoCommand.class);
        verify(processarPagamento, times(2)).executar(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(ProcessarPagamentoCommand::pedidoId)
                .containsExactly(primeiro.getPedidoId(), segundo.getPedidoId());
    }

    @Test
    void deveContinuarQuandoUmPendenteFalha() {
        Pagamento primeiro = pagamento(UUID.randomUUID(), new BigDecimal("10.00"));
        Pagamento segundo = pagamento(UUID.randomUUID(), new BigDecimal("20.00"));
        when(pagamentoRepository.listarPendentes(10)).thenReturn(List.of(primeiro, segundo));
        when(processarPagamento.executar(new ProcessarPagamentoCommand(primeiro.getPedidoId(), primeiro.getValor())))
                .thenThrow(new RuntimeException("erro"));
        when(processarPagamento.executar(new ProcessarPagamentoCommand(segundo.getPedidoId(), segundo.getValor())))
                .thenReturn(response(segundo, "APROVADO"));

        int aprovados = service.executar(10);

        assertThat(aprovados).isEqualTo(1);
    }

    private static Pagamento pagamento(UUID pedidoId, BigDecimal valor) {
        return new Pagamento(UUID.randomUUID(), pedidoId, valor, StatusPagamento.PENDENTE, 1, "falha",
                Instant.parse("2026-05-21T10:00:00Z"), Instant.parse("2026-05-21T10:01:00Z"));
    }

    private static PagamentoResponse response(Pagamento pagamento, String status) {
        return new PagamentoResponse(pagamento.getId(), pagamento.getPedidoId(), pagamento.getValor(), status,
                pagamento.getTentativas(), pagamento.getMotivoFalha(), pagamento.getCreatedAt(), pagamento.getUpdatedAt());
    }
}
