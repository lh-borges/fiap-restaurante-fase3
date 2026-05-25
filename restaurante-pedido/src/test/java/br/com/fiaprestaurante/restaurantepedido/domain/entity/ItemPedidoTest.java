package br.com.fiaprestaurante.restaurantepedido.domain.entity;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitarios da entidade de dominio {@link ItemPedido} - cobrem
 * invariantes do construtor e calculo do subtotal.
 *
 * @author Danilo Fernando
 */
class ItemPedidoTest {

    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    @Test
    void deveCriarItemComTodosOsCampos() {
        ItemPedido item = new ItemPedido(PRODUTO_ID, "X-Burger", 2, new BigDecimal("25.90"));

        assertThat(item.getProdutoId()).isEqualTo(PRODUTO_ID);
        assertThat(item.getNome()).isEqualTo("X-Burger");
        assertThat(item.getQuantidade()).isEqualTo(2);
        assertThat(item.getPreco()).isEqualByComparingTo("25.90");
    }

    @Test
    void deveCalcularSubtotalCorretamente() {
        ItemPedido item = new ItemPedido(PRODUTO_ID, "X-Burger", 3, new BigDecimal("10.00"));

        assertThat(item.subtotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void deveCalcularSubtotalComCasasDecimais() {
        ItemPedido item = new ItemPedido(PRODUTO_ID, "Refrigerante", 2, new BigDecimal("7.55"));

        assertThat(item.subtotal()).isEqualByComparingTo("15.10");
    }

    @Test
    void deveRejeitarProdutoIdNulo() {
        assertThatThrownBy(() -> new ItemPedido(null, "X-Burger", 1, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("produtoId");
    }

    @Test
    void deveRejeitarNomeNulo() {
        assertThatThrownBy(() -> new ItemPedido(PRODUTO_ID, null, 1, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nome");
    }

    @Test
    void deveRejeitarNomeVazio() {
        assertThatThrownBy(() -> new ItemPedido(PRODUTO_ID, "   ", 1, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nome");
    }

    @Test
    void deveRejeitarQuantidadeZero() {
        assertThatThrownBy(() -> new ItemPedido(PRODUTO_ID, "X-Burger", 0, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("quantidade");
    }

    @Test
    void deveRejeitarQuantidadeNegativa() {
        assertThatThrownBy(() -> new ItemPedido(PRODUTO_ID, "X-Burger", -1, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("quantidade");
    }

    @Test
    void deveRejeitarPrecoNulo() {
        assertThatThrownBy(() -> new ItemPedido(PRODUTO_ID, "X-Burger", 1, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("preço");
    }

    @Test
    void deveRejeitarPrecoZero() {
        assertThatThrownBy(() -> new ItemPedido(PRODUTO_ID, "X-Burger", 1, BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("preço");
    }

    @Test
    void deveRejeitarPrecoNegativo() {
        assertThatThrownBy(() -> new ItemPedido(PRODUTO_ID, "X-Burger", 1, new BigDecimal("-1.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("preço");
    }
}
