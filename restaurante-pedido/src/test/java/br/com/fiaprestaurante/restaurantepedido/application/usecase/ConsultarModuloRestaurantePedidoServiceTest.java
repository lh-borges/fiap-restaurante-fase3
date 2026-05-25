package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste unitario do {@link ConsultarModuloRestaurantePedidoService} -
 * valida a string fixa retornada como health/info do modulo.
 *
 * @author Danilo Fernando
 */
class ConsultarModuloRestaurantePedidoServiceTest {

    @Test
    void deveRetornarDescricaoNaoVazia() {
        ConsultarModuloRestaurantePedidoService service =
                new ConsultarModuloRestaurantePedidoService();

        String descricao = service.executar();

        assertThat(descricao).isNotBlank();
        assertThat(descricao).containsIgnoringCase("restaurante");
    }
}
