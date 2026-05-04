package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto;

import java.util.List;
import java.util.UUID;

public record CriarPedidoGraphQLInput(
        UUID restauranteId,
        List<ItemPedidoGraphQLInput> itens
) {
}
