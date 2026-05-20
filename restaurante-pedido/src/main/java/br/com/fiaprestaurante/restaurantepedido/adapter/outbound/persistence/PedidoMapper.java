package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;

import java.util.List;

/**
 * Mapper estático entre as entidades de domínio ({@link Pedido},
 * {@link ItemPedido}) e suas contrapartes JPA ({@link PedidoJpaEntity},
 * {@link ItemPedidoJpaEntity}).
 *
 * <p>Mantém a regra de ouro do hexagonal: o domínio não importa JPA.
 * Conversão manual, simples e auditável — sem dependência de libs externas.
 *
 * @author Danilo Fernando
 */
public final class PedidoMapper {

    private PedidoMapper() {
    }

    /**
     * Converte uma entidade de domínio para a entidade JPA correspondente.
     *
     * @param pedido entidade de domínio (não pode ser {@code null})
     * @return entidade JPA pronta para persistência
     */
    public static PedidoJpaEntity toEntity(Pedido pedido) {
        List<ItemPedidoJpaEntity> itens = pedido.getItens().stream()
                .map(PedidoMapper::toEntity)
                .toList();
        return new PedidoJpaEntity(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getRestauranteId(),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getPagamentoId(),
                pedido.getMotivoPendencia(),
                pedido.getCreatedAt(),
                pedido.getUpdatedAt(),
                itens
        );
    }

    /**
     * Converte uma entidade JPA recuperada do banco em entidade de domínio.
     *
     * <p>Usa o construtor de hidratação do {@link Pedido} (que não aplica
     * validações), pois os dados vêm de uma fonte confiável.
     *
     * @param entity entidade JPA recém-carregada (não pode ser {@code null})
     * @return entidade de domínio reidratada
     */
    public static Pedido toDomain(PedidoJpaEntity entity) {
        List<ItemPedido> itens = entity.getItens().stream()
                .map(PedidoMapper::toDomain)
                .toList();
        return new Pedido(
                entity.getId(),
                entity.getClienteId(),
                entity.getRestauranteId(),
                itens,
                entity.getValorTotal(),
                entity.getStatus(),
                entity.getPagamentoId(),
                entity.getMotivoPendencia(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Converte um item de domínio em sua contraparte JPA.
     *
     * @param item entidade de domínio
     * @return entidade JPA correspondente
     */
    private static ItemPedidoJpaEntity toEntity(ItemPedido item) {
        return new ItemPedidoJpaEntity(item.getProdutoId(), item.getNome(), item.getQuantidade(), item.getPreco());
    }

    /**
     * Converte um item JPA em sua contraparte de domínio.
     *
     * @param entity entidade JPA
     * @return entidade de domínio
     */
    private static ItemPedido toDomain(ItemPedidoJpaEntity entity) {
        return new ItemPedido(entity.getProdutoId(), entity.getNome(), entity.getQuantidade(), entity.getPreco());
    }
}
