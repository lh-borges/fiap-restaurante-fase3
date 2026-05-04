package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoOutput(
        UUID id,
        String clienteId,
        String clienteNome,
        UUID restauranteId,
        List<ItemPedidoInput> itens,
        BigDecimal valorTotal,
        String status,
        LocalDateTime criadoEm
) {
}
