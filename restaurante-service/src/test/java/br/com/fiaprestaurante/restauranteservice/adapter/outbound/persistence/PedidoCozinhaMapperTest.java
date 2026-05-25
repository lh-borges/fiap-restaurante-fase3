package br.com.fiaprestaurante.restauranteservice.adapter.outbound.persistence;

import br.com.fiaprestaurante.restauranteservice.domain.entity.ItemCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de round-trip do {@link PedidoCozinhaMapper}.
 *
 * @author Danilo Fernando
 */
class PedidoCozinhaMapperTest {

    @Test
    void roundTripDevePreservarTodosOsCampos() {
        UUID id = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        UUID restauranteId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-25T12:00:00Z");
        PedidoCozinha original = new PedidoCozinha(
                id,
                pedidoId,
                restauranteId,
                List.of(new ItemCozinha(UUID.randomUUID(), "X-Burger", 3)),
                StatusCozinha.EM_PREPARO,
                now,
                now,
                now,
                null
        );

        PedidoCozinhaJpaEntity entity = PedidoCozinhaMapper.toEntity(original);
        PedidoCozinha recuperado = PedidoCozinhaMapper.toDomain(entity);

        assertThat(recuperado.getId()).isEqualTo(id);
        assertThat(recuperado.getPedidoId()).isEqualTo(pedidoId);
        assertThat(recuperado.getRestauranteId()).isEqualTo(restauranteId);
        assertThat(recuperado.getStatus()).isEqualTo(StatusCozinha.EM_PREPARO);
        assertThat(recuperado.getCreatedAt()).isEqualTo(now);
        assertThat(recuperado.getUpdatedAt()).isEqualTo(now);
        assertThat(recuperado.getIniciadoEm()).isEqualTo(now);
        assertThat(recuperado.getFinalizadoEm()).isNull();
        assertThat(recuperado.getItens()).hasSize(1);
        assertThat(recuperado.getItens().get(0).getNome()).isEqualTo("X-Burger");
        assertThat(recuperado.getItens().get(0).getQuantidade()).isEqualTo(3);
    }
}
