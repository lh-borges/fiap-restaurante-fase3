package br.com.fiaprestaurante.pagamento.application.dto;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,
        UUID pedidoId,
        BigDecimal valor,
        String status,
        int tentativas,
        String motivoFalha,
        Instant createdAt,
        Instant updatedAt
) {

    public static PagamentoResponse from(Pagamento pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getValor(),
                pagamento.getStatus().name(),
                pagamento.getTentativas(),
                pagamento.getMotivoFalha(),
                pagamento.getCreatedAt(),
                pagamento.getUpdatedAt()
        );
    }
}
