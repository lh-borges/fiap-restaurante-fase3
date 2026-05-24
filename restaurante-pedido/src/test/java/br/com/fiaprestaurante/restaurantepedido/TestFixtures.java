package br.com.fiaprestaurante.restaurantepedido;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TestFixtures {

    public static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID OUTRO_CLIENTE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID RESTAURANTE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID PRODUTO_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID PEDIDO_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID PAGAMENTO_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final Instant CREATED_AT = Instant.parse("2026-05-21T10:00:00Z");
    public static final Instant UPDATED_AT = Instant.parse("2026-05-21T10:05:00Z");

    private TestFixtures() {
    }

    public static ItemPedido itemPedido() {
        return new ItemPedido(PRODUTO_ID, "Pizza", 2, new BigDecimal("25.50"));
    }

    public static Pedido pedidoCriado() {
        return new Pedido(PEDIDO_ID, CLIENTE_ID, RESTAURANTE_ID, List.of(itemPedido()),
                new BigDecimal("51.00"), StatusPedido.CRIADO, null, null, CREATED_AT, UPDATED_AT);
    }

    public static Pedido pedidoConfirmado() {
        return new Pedido(PEDIDO_ID, CLIENTE_ID, RESTAURANTE_ID, List.of(itemPedido()),
                new BigDecimal("51.00"), StatusPedido.CONFIRMADO, null, null, CREATED_AT, UPDATED_AT);
    }
}
