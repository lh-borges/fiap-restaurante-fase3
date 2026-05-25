package br.com.fiaprestaurante.restauranteservice.domain.entity;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes do value object {@link ItemCozinha}.
 *
 * @author Danilo Fernando
 */
class ItemCozinhaTest {

    private static final UUID PRODUTO_ID = UUID.randomUUID();

    @Test
    void deveCriarComCamposValidos() {
        ItemCozinha item = new ItemCozinha(PRODUTO_ID, "X-Burger", 2);
        assertThat(item.getProdutoId()).isEqualTo(PRODUTO_ID);
        assertThat(item.getNome()).isEqualTo("X-Burger");
        assertThat(item.getQuantidade()).isEqualTo(2);
    }

    @Test
    void deveRecusarProdutoIdNulo() {
        assertThatThrownBy(() -> new ItemCozinha(null, "X-Burger", 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("produtoId");
    }

    @Test
    void deveRecusarNomeVazio() {
        assertThatThrownBy(() -> new ItemCozinha(PRODUTO_ID, "  ", 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nome");
    }

    @Test
    void deveRecusarQuantidadeZeroOuNegativa() {
        assertThatThrownBy(() -> new ItemCozinha(PRODUTO_ID, "X", 0))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("quantidade");
        assertThatThrownBy(() -> new ItemCozinha(PRODUTO_ID, "X", -1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("quantidade");
    }
}
