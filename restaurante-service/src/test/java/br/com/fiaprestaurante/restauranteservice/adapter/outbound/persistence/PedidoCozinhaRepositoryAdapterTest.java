package br.com.fiaprestaurante.restauranteservice.adapter.outbound.persistence;

import br.com.fiaprestaurante.restauranteservice.domain.entity.ItemCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do {@link PedidoCozinhaRepositoryAdapter} — mocka o
 * {@link PedidoCozinhaJpaRepository} e valida a delegacao + conversao
 * via {@link PedidoCozinhaMapper}.
 *
 * @author Danilo Fernando
 */
class PedidoCozinhaRepositoryAdapterTest {

    private PedidoCozinhaJpaRepository jpa;
    private PedidoCozinhaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpa = mock(PedidoCozinhaJpaRepository.class);
        adapter = new PedidoCozinhaRepositoryAdapter(jpa);
    }

    private PedidoCozinha pedido() {
        return new PedidoCozinha(UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ItemCozinha(UUID.randomUUID(), "X", 1)));
    }

    @Test
    void salvarDeveDelegarAoJpaERetornarOMesmoAgregado() {
        PedidoCozinha p = pedido();
        when(jpa.save(any(PedidoCozinhaJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        PedidoCozinha result = adapter.salvar(p);
        assertThat(result).isSameAs(p);
        verify(jpa).save(any(PedidoCozinhaJpaEntity.class));
    }

    @Test
    void porIdDeveConverterEntityParaDomain() {
        PedidoCozinha p = pedido();
        when(jpa.findById(p.getId())).thenReturn(Optional.of(PedidoCozinhaMapper.toEntity(p)));
        Optional<PedidoCozinha> result = adapter.porId(p.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(p.getId());
    }

    @Test
    void porIdAusenteDeveRetornarEmpty() {
        UUID id = UUID.randomUUID();
        when(jpa.findById(id)).thenReturn(Optional.empty());
        assertThat(adapter.porId(id)).isEmpty();
    }

    @Test
    void listarSemFiltroDeveUsarFindAllByOrderByCreatedAtAsc() {
        when(jpa.findAllByOrderByCreatedAtAsc()).thenReturn(List.of());
        adapter.listar(null);
        verify(jpa).findAllByOrderByCreatedAtAsc();
    }

    @Test
    void listarComFiltroDeveUsarFindByStatusOrderByCreatedAtAsc() {
        when(jpa.findByStatusOrderByCreatedAtAsc(StatusCozinha.PRONTO)).thenReturn(List.of());
        adapter.listar(StatusCozinha.PRONTO);
        verify(jpa).findByStatusOrderByCreatedAtAsc(StatusCozinha.PRONTO);
    }
}
