package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoGraphQLInput(
        UUID produtoId,
        String nome,
        Integer quantidade,
        BigDecimal preco
) {
}
