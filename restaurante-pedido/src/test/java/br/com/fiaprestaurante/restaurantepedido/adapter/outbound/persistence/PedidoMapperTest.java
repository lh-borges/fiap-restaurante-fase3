package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitarios do {@link PedidoMapper} - cobre conversao em ambos os
 * sentidos (dominio → JPA e JPA → dominio) e o round-trip.
 *
 * @author Danilo Fernando
 */
class PedidoMapperTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private Pedido pedidoDeExemplo() {
        return new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(
                new ItemPedido(PRODUTO_ID, "X-Burger", 2, new BigDecimal("25.90")),
                new ItemPedido(PRODUTO_ID, "Refrigerante", 1, new BigDecimal("7.50"))));
    }

    @Test
    void toEntityDeveCopiarTodosOsCampos() {
        Pedido pedido = pedidoDeExemplo();

        PedidoJpaEntity entity = PedidoMapper.toEntity(pedido);

        assertThat(entity.getId()).isEqualTo(pedido.getId());
        assertThat(entity.getClienteId()).isEqualTo(pedido.getClienteId());
        assertThat(entity.getRestauranteId()).isEqualTo(pedido.getRestauranteId());
        assertThat(entity.getValorTotal()).isEqualByComparingTo(pedido.getValorTotal());
        assertThat(entity.getStatus()).isEqualTo(pedido.getStatus());
        assertThat(entity.getCreatedAt()).isEqualTo(pedido.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(pedido.getUpdatedAt());
        assertThat(entity.getItens()).hasSize(2);
        assertThat(entity.getItens().get(0).getNome()).isEqualTo("X-Burger");
        assertThat(entity.getItens().get(0).getQuantidade()).isEqualTo(2);
    }

    @Test
    void toDomainDeveCopiarTodosOsCampos() {
        UUID id = UUID.randomUUID();
        UUID pagamentoId = UUID.randomUUID();
        Instant criado = Instant.parse("2026-01-01T10:00:00Z");
        Instant atualizado = Instant.parse("2026-01-02T11:00:00Z");
        ItemPedidoJpaEntity itemEntity = new ItemPedidoJpaEntity(
                PRODUTO_ID, "X-Burger", 2, new BigDecimal("25.90"));
        PedidoJpaEntity entity = new PedidoJpaEntity(
                id, CLIENTE_ID, RESTAURANTE_ID, new BigDecimal("51.80"),
                StatusPedido.PAGO, pagamentoId, null, criado, atualizado,
                List.of(itemEntity));

        Pedido pedido = PedidoMapper.toDomain(entity);

        assertThat(pedido.getId()).isEqualTo(id);
        assertThat(pedido.getClienteId()).isEqualTo(CLIENTE_ID);
        assertThat(pedido.getRestauranteId()).isEqualTo(RESTAURANTE_ID);
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("51.80");
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.getPagamentoId()).isEqualTo(pagamentoId);
        assertThat(pedido.getCreatedAt()).isEqualTo(criado);
        assertThat(pedido.getUpdatedAt()).isEqualTo(atualizado);
        assertThat(pedido.getItens()).hasSize(1);
        assertThat(pedido.getItens().get(0).getNome()).isEqualTo("X-Burger");
    }

    @Test
    void roundTripDeveManterTodosOsCamposIguais() {
        Pedido original = pedidoDeExemplo();

        Pedido reconvertido = PedidoMapper.toDomain(PedidoMapper.toEntity(original));

        assertThat(reconvertido.getId()).isEqualTo(original.getId());
        assertThat(reconvertido.getClienteId()).isEqualTo(original.getClienteId());
        assertThat(reconvertido.getRestauranteId()).isEqualTo(original.getRestauranteId());
        assertThat(reconvertido.getValorTotal()).isEqualByComparingTo(original.getValorTotal());
        assertThat(reconvertido.getStatus()).isEqualTo(original.getStatus());
        assertThat(reconvertido.getItens()).hasSize(original.getItens().size());
    }
}
