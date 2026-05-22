package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto;

import br.com.fiaprestaurante.restaurantepedido.TestFixtures;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphQlDtoTest {

    @Test
    void deveCriarInputsGraphQl() {
        ItemPedidoInput item = new ItemPedidoInput(TestFixtures.PRODUTO_ID, "Pizza", 2, new BigDecimal("25.50"));
        CriarPedidoInput input = new CriarPedidoInput(TestFixtures.RESTAURANTE_ID, List.of(item));

        assertThat(input.restauranteId()).isEqualTo(TestFixtures.RESTAURANTE_ID);
        assertThat(input.itens()).containsExactly(item);
        assertThat(item.produtoId()).isEqualTo(TestFixtures.PRODUTO_ID);
        assertThat(item.nome()).isEqualTo("Pizza");
        assertThat(item.quantidade()).isEqualTo(2);
        assertThat(item.preco()).isEqualByComparingTo("25.50");
    }

    @Test
    void deveExporPayloadDeStatusDoModulo() {
        ModuloRestaurantePedidoPayload payload = new ModuloRestaurantePedidoPayload("operacional");

        assertThat(payload.getNome()).isEqualTo("restaurante-pedido");
        assertThat(payload.isImplementado()).isTrue();
        assertThat(payload.getDescricao()).isEqualTo("operacional");
    }
}
