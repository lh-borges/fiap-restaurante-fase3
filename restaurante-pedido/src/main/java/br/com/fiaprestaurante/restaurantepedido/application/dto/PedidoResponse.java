package br.com.fiaprestaurante.restaurantepedido.application.dto;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO imutável de saída de um {@link Pedido} — usado em respostas GraphQL e
 * retornos de use cases.
 *
 * <p>O {@code status} é serializado como string para facilitar consumo por
 * clientes externos sem acoplar ao enum interno.
 *
 * @param id              identidade do pedido
 * @param clienteId       identificador do cliente
 * @param restauranteId   identificador do restaurante
 * @param itens           itens que compõem o pedido
 * @param valorTotal      valor total calculado
 * @param status          status atual ({@code CRIADO}, {@code CONFIRMADO}, etc.)
 * @param pagamentoId     identidade do pagamento (ou {@code null})
 * @param motivoPendencia descrição da última pendência (ou {@code null})
 * @param createdAt       instante da criação
 * @param updatedAt       instante da última modificação
 *
 * @author Danilo Fernando
 */
public record PedidoResponse(
        UUID id,
        UUID clienteId,
        UUID restauranteId,
        List<ItemPedidoResponse> itens,
        BigDecimal valorTotal,
        String status,
        UUID pagamentoId,
        String motivoPendencia,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Constrói um {@code PedidoResponse} a partir de uma entidade de domínio.
     *
     * @param pedido entidade fonte; não pode ser {@code null}
     * @return DTO contendo os mesmos dados em forma imutável
     */
    public static PedidoResponse from(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens().stream()
                .map(ItemPedidoResponse::from)
                .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getRestauranteId(),
                itens,
                pedido.getValorTotal(),
                pedido.getStatus().name(),
                pedido.getPagamentoId(),
                pedido.getMotivoPendencia(),
                pedido.getCreatedAt(),
                pedido.getUpdatedAt()
        );
    }
}
