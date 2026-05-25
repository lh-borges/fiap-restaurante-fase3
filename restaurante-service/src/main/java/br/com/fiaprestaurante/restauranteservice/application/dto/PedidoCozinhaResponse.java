package br.com.fiaprestaurante.restauranteservice.application.dto;

import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resposta de leitura para um pedido na cozinha. Espelha o agregado
 * {@link PedidoCozinha} com os instantes serializados em ISO-8601.
 *
 * @author Danilo Fernando
 */
public record PedidoCozinhaResponse(
        UUID id,
        UUID pedidoId,
        UUID restauranteId,
        List<ItemCozinhaResponse> itens,
        String status,
        String createdAt,
        String updatedAt,
        String iniciadoEm,
        String finalizadoEm
) {

    /**
     * Converte um agregado de dominio na sua resposta GraphQL/JSON.
     *
     * @param pedido entidade de dominio
     * @return DTO de leitura
     */
    public static PedidoCozinhaResponse from(PedidoCozinha pedido) {
        List<ItemCozinhaResponse> itens = pedido.getItens().stream()
                .map(i -> new ItemCozinhaResponse(i.getProdutoId(), i.getNome(), i.getQuantidade()))
                .toList();
        return new PedidoCozinhaResponse(
                pedido.getId(),
                pedido.getPedidoId(),
                pedido.getRestauranteId(),
                itens,
                pedido.getStatus().name(),
                pedido.getCreatedAt().toString(),
                pedido.getUpdatedAt().toString(),
                toIsoOrNull(pedido.getIniciadoEm()),
                toIsoOrNull(pedido.getFinalizadoEm())
        );
    }

    private static String toIsoOrNull(Instant instant) {
        return instant == null ? null : instant.toString();
    }
}
