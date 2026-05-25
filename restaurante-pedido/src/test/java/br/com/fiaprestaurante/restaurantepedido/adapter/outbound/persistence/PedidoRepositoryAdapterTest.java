package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link PedidoRepositoryAdapter} - usa mock do
 * Spring Data {@link PedidoJpaRepository} para validar a delegacao e a
 * conversao por meio do {@link PedidoMapper}.
 *
 * @author Danilo Fernando
 */
class PedidoRepositoryAdapterTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private PedidoJpaRepository jpaRepository;
    private PedidoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(PedidoJpaRepository.class);
        adapter = new PedidoRepositoryAdapter(jpaRepository);
    }

    private Pedido novoPedido() {
        return new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(
                new ItemPedido(PRODUTO_ID, "X-Burger", 1, new BigDecimal("25.90"))));
    }

    @Test
    void salvarDeveConverterParaJpaPersistirEReconverterParaDominio() {
        Pedido pedido = novoPedido();
        when(jpaRepository.save(any(PedidoJpaEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Pedido salvo = adapter.salvar(pedido);

        verify(jpaRepository).save(any(PedidoJpaEntity.class));
        assertThat(salvo.getId()).isEqualTo(pedido.getId());
        assertThat(salvo.getClienteId()).isEqualTo(pedido.getClienteId());
    }

    @Test
    void buscarPorIdDeveRetornarPedidoQuandoExistir() {
        Pedido pedido = novoPedido();
        when(jpaRepository.findById(pedido.getId()))
                .thenReturn(Optional.of(PedidoMapper.toEntity(pedido)));

        Optional<Pedido> resultado = adapter.buscarPorId(pedido.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(pedido.getId());
    }

    @Test
    void buscarPorIdDeveRetornarVazioQuandoNaoExistir() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Pedido> resultado = adapter.buscarPorId(id);

        assertThat(resultado).isEmpty();
    }

    @Test
    void listarPorClienteDeveConverterCadaEntity() {
        Pedido p1 = novoPedido();
        Pedido p2 = novoPedido();
        when(jpaRepository.findByClienteIdOrderByCreatedAtDesc(CLIENTE_ID))
                .thenReturn(List.of(PedidoMapper.toEntity(p1), PedidoMapper.toEntity(p2)));

        List<Pedido> resultado = adapter.listarPorCliente(CLIENTE_ID);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Pedido::getClienteId).containsOnly(CLIENTE_ID);
    }
}
