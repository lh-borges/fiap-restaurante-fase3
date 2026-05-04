package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PedidoResumoCriadoOutput(
        UUID pedidoId,
        BigDecimal valorTotal,
        String status
) {
}
