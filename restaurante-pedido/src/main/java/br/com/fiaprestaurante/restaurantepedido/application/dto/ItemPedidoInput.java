package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoInput(
        UUID produtoId,
        String nome,
        Integer quantidade,
        BigDecimal preco
) {
}
