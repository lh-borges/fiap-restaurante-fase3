package br.com.fiaprestaurante.restaurantepedido.application.dto;

import br.com.fiaprestaurante.restaurantepedido.TestFixtures;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoDtoTest {

    @Test
    void deveCriarRecordsDeComandoEEvento() {
        ItemPedidoCommand item = new ItemPedidoCommand(TestFixtures.PRODUTO_ID, "Pizza", 2, new BigDecimal("25.50"));
        CriarPedidoCommand command = new CriarPedidoCommand(TestFixtures.CLIENTE_ID, TestFixtures.RESTAURANTE_ID, List.of(item));
        PedidoCriadoEvent event = new PedidoCriadoEvent(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID,
                new BigDecimal("51.00"), Instant.parse("2026-05-21T10:10:00Z"));

        assertThat(command.clienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
        assertThat(command.restauranteId()).isEqualTo(TestFixtures.RESTAURANTE_ID);
        assertThat(command.itens()).containsExactly(item);
        assertThat(event.pedidoId()).isEqualTo(TestFixtures.PEDIDO_ID);
        assertThat(event.valorTotal()).isEqualByComparingTo("51.00");
    }

    @Test
    void deveConverterItemDeDominioParaResponse() {
        ItemPedido item = TestFixtures.itemPedido();

        ItemPedidoResponse response = ItemPedidoResponse.from(item);

        assertThat(response.produtoId()).isEqualTo(TestFixtures.PRODUTO_ID);
        assertThat(response.nome()).isEqualTo("Pizza");
        assertThat(response.quantidade()).isEqualTo(2);
        assertThat(response.preco()).isEqualByComparingTo("25.50");
        assertThat(response.subtotal()).isEqualByComparingTo("51.00");
    }

    @Test
    void deveConverterPedidoDeDominioParaResponse() {
        Pedido pedido = TestFixtures.pedidoCriado();

        PedidoResponse response = PedidoResponse.from(pedido);

        assertThat(response.id()).isEqualTo(TestFixtures.PEDIDO_ID);
        assertThat(response.clienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
        assertThat(response.restauranteId()).isEqualTo(TestFixtures.RESTAURANTE_ID);
        assertThat(response.valorTotal()).isEqualByComparingTo("51.00");
        assertThat(response.status()).isEqualTo("CRIADO");
        assertThat(response.pagamentoId()).isNull();
        assertThat(response.motivoPendencia()).isNull();
        assertThat(response.createdAt()).isEqualTo(TestFixtures.CREATED_AT);
        assertThat(response.updatedAt()).isEqualTo(TestFixtures.UPDATED_AT);
        assertThat(response.itens()).singleElement()
                .extracting(ItemPedidoResponse::nome)
                .isEqualTo("Pizza");
    }
}
