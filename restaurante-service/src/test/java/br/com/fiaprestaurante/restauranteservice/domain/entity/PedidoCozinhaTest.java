package br.com.fiaprestaurante.restauranteservice.domain.entity;

import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes do agregado {@link PedidoCozinha}.
 *
 * @author Danilo Fernando
 */
class PedidoCozinhaTest {

    private static final UUID PEDIDO_ID = UUID.randomUUID();
    private static final UUID RESTAURANTE_ID = UUID.randomUUID();
    private static final UUID PRODUTO_ID = UUID.randomUUID();

    private List<ItemCozinha> itensValidos() {
        return List.of(new ItemCozinha(PRODUTO_ID, "X-Burger", 1));
    }

    private PedidoCozinha pedidoRecebido() {
        return new PedidoCozinha(PEDIDO_ID, RESTAURANTE_ID, itensValidos());
    }

    @Test
    void deveCriarNoStatusRecebido() {
        PedidoCozinha p = pedidoRecebido();
        assertThat(p.getStatus()).isEqualTo(StatusCozinha.RECEBIDO);
        assertThat(p.getPedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(p.getRestauranteId()).isEqualTo(RESTAURANTE_ID);
        assertThat(p.getItens()).hasSize(1);
        assertThat(p.getCreatedAt()).isNotNull();
        assertThat(p.getUpdatedAt()).isNotNull();
        assertThat(p.getIniciadoEm()).isNull();
        assertThat(p.getFinalizadoEm()).isNull();
    }

    @Test
    void deveRecusarPedidoIdNulo() {
        assertThatThrownBy(() -> new PedidoCozinha(null, RESTAURANTE_ID, itensValidos()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pedidoId");
    }

    @Test
    void deveRecusarItensVazios() {
        assertThatThrownBy(() -> new PedidoCozinha(PEDIDO_ID, RESTAURANTE_ID, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("item");
    }

    @Test
    void deveTransitarParaEmPreparo() {
        PedidoCozinha p = pedidoRecebido();
        p.iniciarPreparo();
        assertThat(p.getStatus()).isEqualTo(StatusCozinha.EM_PREPARO);
        assertThat(p.getIniciadoEm()).isNotNull();
    }

    @Test
    void deveTransitarParaPronto() {
        PedidoCozinha p = pedidoRecebido();
        p.iniciarPreparo();
        p.marcarComoPronto();
        assertThat(p.getStatus()).isEqualTo(StatusCozinha.PRONTO);
        assertThat(p.getFinalizadoEm()).isNotNull();
    }

    @Test
    void naoDeveIniciarPreparoSeNaoEstiverRecebido() {
        PedidoCozinha p = pedidoRecebido();
        p.iniciarPreparo();
        assertThatThrownBy(p::iniciarPreparo)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_PREPARO");
    }

    @Test
    void naoDeveMarcarComoProntoSeNaoEstiverEmPreparo() {
        PedidoCozinha p = pedidoRecebido();
        assertThatThrownBy(p::marcarComoPronto)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("RECEBIDO");
    }

    @Test
    void itensRetornadosSaoImutaveis() {
        PedidoCozinha p = pedidoRecebido();
        assertThatThrownBy(() -> p.getItens().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
