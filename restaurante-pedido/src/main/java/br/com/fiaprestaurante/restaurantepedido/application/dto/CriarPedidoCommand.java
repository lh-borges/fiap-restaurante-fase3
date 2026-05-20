package br.com.fiaprestaurante.restaurantepedido.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Comando de entrada do caso de uso {@code CriarPedidoUseCase}.
 *
 * <p>O {@code clienteId} <strong>não vem da requisição GraphQL</strong> —
 * é extraído do token JWT pelo controller, atendendo ao requisito 5.2 ("o ID
 * do cliente deve vir do token").
 *
 * @param clienteId     identificador do cliente autenticado (do JWT)
 * @param restauranteId identificador do restaurante onde o pedido foi feito
 * @param itens         itens que compõem o pedido
 *
 * @author Danilo Fernando
 */
public record CriarPedidoCommand(UUID clienteId, UUID restauranteId, List<ItemPedidoCommand> itens) {
}
