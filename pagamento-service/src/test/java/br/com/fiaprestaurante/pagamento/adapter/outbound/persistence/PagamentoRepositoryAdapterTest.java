package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link PagamentoRepositoryAdapter} - usa mock do
 * Spring Data {@link PagamentoJpaRepository} para validar a delegacao
 * e a paginacao do listarPendentes.
 *
 * @author Danilo Fernando
 */
class PagamentoRepositoryAdapterTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    private PagamentoJpaRepository jpaRepository;
    private PagamentoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(PagamentoJpaRepository.class);
        adapter = new PagamentoRepositoryAdapter(jpaRepository);
    }

    @Test
    void salvarDeveConverterPersistirEReconverter() {
        Pagamento p = new Pagamento(PEDIDO_ID, new BigDecimal("59.30"));
        when(jpaRepository.save(any(PagamentoJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Pagamento salvo = adapter.salvar(p);

        verify(jpaRepository).save(any(PagamentoJpaEntity.class));
        assertThat(salvo.getId()).isEqualTo(p.getId());
        assertThat(salvo.getPedidoId()).isEqualTo(p.getPedidoId());
    }

    @Test
    void buscarPorPedidoIdDeveDelegarParaJpa() {
        Pagamento p = new Pagamento(PEDIDO_ID, BigDecimal.TEN);
        when(jpaRepository.findByPedidoId(PEDIDO_ID))
                .thenReturn(Optional.of(PagamentoMapper.toEntity(p)));

        Optional<Pagamento> resp = adapter.buscarPorPedidoId(PEDIDO_ID);

        assertThat(resp).isPresent();
        assertThat(resp.get().getPedidoId()).isEqualTo(PEDIDO_ID);
    }

    @Test
    void listarPendentesDevePassarPageableComLimite() {
        when(jpaRepository.findByStatusOrderByCreatedAtAsc(eq(StatusPagamento.PENDENTE), any(Pageable.class)))
                .thenReturn(List.of(PagamentoMapper.toEntity(new Pagamento(PEDIDO_ID, BigDecimal.TEN))));

        List<Pagamento> resp = adapter.listarPendentes(20);

        assertThat(resp).hasSize(1);
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findByStatusOrderByCreatedAtAsc(eq(StatusPagamento.PENDENTE), captor.capture());
        assertThat(captor.getValue()).isEqualTo(PageRequest.of(0, 20));
    }

    @Test
    void listarPendentesDeveClampearLimiteSuperior() {
        when(jpaRepository.findByStatusOrderByCreatedAtAsc(eq(StatusPagamento.PENDENTE), any(Pageable.class)))
                .thenReturn(List.of());

        adapter.listarPendentes(10_000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findByStatusOrderByCreatedAtAsc(eq(StatusPagamento.PENDENTE), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(1000);
    }

    @Test
    void listarPendentesDeveClampearLimiteInferior() {
        when(jpaRepository.findByStatusOrderByCreatedAtAsc(eq(StatusPagamento.PENDENTE), any(Pageable.class)))
                .thenReturn(List.of());

        adapter.listarPendentes(0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findByStatusOrderByCreatedAtAsc(eq(StatusPagamento.PENDENTE), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(1);
    }
}
