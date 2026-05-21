package br.com.fiaprestaurante.pagamento.adapter.outbound.persistence;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoRepositoryAdapterTest {

    @Mock
    private PagamentoJpaRepository jpaRepository;

    private PagamentoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PagamentoRepositoryAdapter(jpaRepository);
    }

    @Test
    void deveSalvarConvertendoEntreDomainEJpa() {
        Pagamento pagamento = pagamento(StatusPagamento.PENDENTE);
        PagamentoJpaEntity entitySalva = PagamentoMapper.toEntity(pagamento);
        when(jpaRepository.save(org.mockito.ArgumentMatchers.any(PagamentoJpaEntity.class))).thenReturn(entitySalva);

        Pagamento salvo = adapter.salvar(pagamento);

        ArgumentCaptor<PagamentoJpaEntity> captor = ArgumentCaptor.forClass(PagamentoJpaEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(pagamento.getId());
        assertThat(salvo.getId()).isEqualTo(pagamento.getId());
        assertThat(salvo.getStatus()).isEqualTo(StatusPagamento.PENDENTE);
    }

    @Test
    void deveBuscarPorPedidoId() {
        Pagamento pagamento = pagamento(StatusPagamento.APROVADO);
        when(jpaRepository.findByPedidoId(pagamento.getPedidoId()))
                .thenReturn(Optional.of(PagamentoMapper.toEntity(pagamento)));

        Optional<Pagamento> encontrado = adapter.buscarPorPedidoId(pagamento.getPedidoId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(pagamento.getId());
    }

    @Test
    void deveListarPendentesLimitandoPageSizeEntreUmEMil() {
        when(jpaRepository.findByStatusOrderByCreatedAtAsc(
                org.mockito.ArgumentMatchers.eq(StatusPagamento.PENDENTE),
                org.mockito.ArgumentMatchers.any(Pageable.class)
        )).thenReturn(List.of(PagamentoMapper.toEntity(pagamento(StatusPagamento.PENDENTE))));

        List<Pagamento> pendentes = adapter.listarPendentes(2000);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findByStatusOrderByCreatedAtAsc(
                org.mockito.ArgumentMatchers.eq(StatusPagamento.PENDENTE),
                captor.capture()
        );
        assertThat(captor.getValue().getPageSize()).isEqualTo(1000);
        assertThat(pendentes).hasSize(1);
    }

    @Test
    void deveUsarPageSizeMinimoUm() {
        when(jpaRepository.findByStatusOrderByCreatedAtAsc(
                org.mockito.ArgumentMatchers.eq(StatusPagamento.PENDENTE),
                org.mockito.ArgumentMatchers.any(Pageable.class)
        )).thenReturn(List.of());

        adapter.listarPendentes(0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findByStatusOrderByCreatedAtAsc(
                org.mockito.ArgumentMatchers.eq(StatusPagamento.PENDENTE),
                captor.capture()
        );
        assertThat(captor.getValue().getPageSize()).isEqualTo(1);
    }

    private static Pagamento pagamento(StatusPagamento status) {
        return new Pagamento(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("40.00"), status, 1, null,
                Instant.parse("2026-05-21T10:00:00Z"), Instant.parse("2026-05-21T10:01:00Z"));
    }
}
