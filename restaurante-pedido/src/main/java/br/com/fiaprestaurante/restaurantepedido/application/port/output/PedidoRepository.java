package br.com.fiaprestaurante.restaurantepedido.application.port.output;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para persistência de pedidos.
 *
 * <p>Mantém a camada de aplicação independente do mecanismo concreto de
 * persistência. A implementação default é {@code PedidoRepositoryAdapter}
 * (JPA/MySQL), mas qualquer outro backend pode ser plugado sem alterar os
 * use cases.
 *
 * @author Danilo Fernando
 */
public interface PedidoRepository {

    /**
     * Persiste um novo pedido ou atualiza um existente.
     *
     * @param pedido entidade a ser persistida (não pode ser {@code null})
     * @return a entidade reidratada após a persistência
     */
    Pedido salvar(Pedido pedido);

    /**
     * Busca um pedido pelo seu identificador.
     *
     * @param pedidoId identificador do pedido
     * @return {@link Optional} com o pedido, ou vazio se não encontrado
     */
    Optional<Pedido> buscarPorId(UUID pedidoId);

    /**
     * Lista todos os pedidos do cliente informado, ordenados pela data de
     * criação (mais recentes primeiro).
     *
     * @param clienteId identificador do cliente
     * @return lista (possivelmente vazia) de pedidos do cliente
     */
    List<Pedido> listarPorCliente(UUID clienteId);
}
