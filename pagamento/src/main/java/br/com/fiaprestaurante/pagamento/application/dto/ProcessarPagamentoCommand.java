package br.com.fiaprestaurante.pagamento.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessarPagamentoCommand(UUID pedidoId, BigDecimal valorTotal) {
}
