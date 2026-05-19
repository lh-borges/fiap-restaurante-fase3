package br.com.fiaprestaurante.pagamento.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PagamentoPendenteEvent(UUID pedidoId, UUID pagamentoId, String motivo, Instant timestamp) {
}
