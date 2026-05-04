package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.util.List;
import java.util.UUID;

public record CriarPedidoInput(
        UUID restauranteId,
        List<ItemPedidoInput> itens
) {
}
