package br.com.fiaprestaurante.pagamento.application.dto;

import java.time.Instant;
import java.util.UUID;

public record PagamentoAprovadoEvent(UUID pedidoId, UUID pagamentoId, Instant timestamp) {
}
