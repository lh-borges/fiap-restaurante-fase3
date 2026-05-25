package br.com.fiaprestaurante.restauranteservice.adapter.outbound.persistence;

import br.com.fiaprestaurante.restauranteservice.domain.entity.ItemCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;

import java.util.List;

/**
 * Mapper estatico entre as entidades de dominio
 * ({@link PedidoCozinha}, {@link ItemCozinha}) e suas contrapartes JPA.
 *
 * <p>Mantem a regra de ouro do hexagonal: o dominio nao importa JPA.
 *
 * @author Danilo Fernando
 */
public final class PedidoCozinhaMapper {

    private PedidoCozinhaMapper() {
    }

    public static PedidoCozinhaJpaEntity toEntity(PedidoCozinha pedido) {
        List<ItemCozinhaJpaEntity> itens = pedido.getItens().stream()
                .map(PedidoCozinhaMapper::toEntity)
                .toList();
        return new PedidoCozinhaJpaEntity(
                pedido.getId(),
                pedido.getPedidoId(),
                pedido.getRestauranteId(),
                pedido.getStatus(),
                pedido.getCreatedAt(),
                pedido.getUpdatedAt(),
                pedido.getIniciadoEm(),
                pedido.getFinalizadoEm(),
                itens
        );
    }

    public static PedidoCozinha toDomain(PedidoCozinhaJpaEntity entity) {
        List<ItemCozinha> itens = entity.getItens().stream()
                .map(PedidoCozinhaMapper::toDomain)
                .toList();
        return new PedidoCozinha(
                entity.getId(),
                entity.getPedidoId(),
                entity.getRestauranteId(),
                itens,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getIniciadoEm(),
                entity.getFinalizadoEm()
        );
    }

    private static ItemCozinhaJpaEntity toEntity(ItemCozinha item) {
        return new ItemCozinhaJpaEntity(item.getProdutoId(), item.getNome(), item.getQuantidade());
    }

    private static ItemCozinha toDomain(ItemCozinhaJpaEntity entity) {
        return new ItemCozinha(entity.getProdutoId(), entity.getNome(), entity.getQuantidade());
    }
}
