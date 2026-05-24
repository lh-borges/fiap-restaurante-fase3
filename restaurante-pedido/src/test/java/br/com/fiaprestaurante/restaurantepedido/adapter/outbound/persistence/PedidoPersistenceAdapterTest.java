package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.persistence;

import br.com.fiaprestaurante.restaurantepedido.TestFixtures;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoPersistenceAdapterTest {

    @Mock
    private PedidoJpaRepository jpaRepository;

    @Test
    void deveMapearPedidoEntreDominioEJpa() {
        Pedido pedido = TestFixtures.pedidoCriado();

        PedidoJpaEntity entity = PedidoMapper.toEntity(pedido);
        Pedido domain = PedidoMapper.toDomain(entity);

        assertThat(entity.getId()).isEqualTo(TestFixtures.PEDIDO_ID);
        assertThat(entity.getClienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
        assertThat(entity.getRestauranteId()).isEqualTo(TestFixtures.RESTAURANTE_ID);
        assertThat(entity.getValorTotal()).isEqualByComparingTo("51.00");
        assertThat(entity.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(entity.getCreatedAt()).isEqualTo(TestFixtures.CREATED_AT);
        assertThat(entity.getUpdatedAt()).isEqualTo(TestFixtures.UPDATED_AT);
        assertThat(entity.getItens()).singleElement()
                .extracting(ItemPedidoJpaEntity::getNome)
                .isEqualTo("Pizza");

        assertThat(domain.getId()).isEqualTo(TestFixtures.PEDIDO_ID);
        assertThat(domain.getClienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
        assertThat(domain.getValorTotal()).isEqualByComparingTo("51.00");
        assertThat(domain.getItens()).singleElement()
                .extracting(item -> item.getProdutoId())
                .isEqualTo(TestFixtures.PRODUTO_ID);
    }

    @Test
    void deveExporGettersDasEntidadesJpa() {
        ItemPedidoJpaEntity item = new ItemPedidoJpaEntity(TestFixtures.PRODUTO_ID, "Pizza", 2, new BigDecimal("25.50"));
        PedidoJpaEntity pedido = new PedidoJpaEntity(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID,
                TestFixtures.RESTAURANTE_ID, new BigDecimal("51.00"), StatusPedido.PENDENTE_PAGAMENTO,
                TestFixtures.PAGAMENTO_ID, "gateway fora", TestFixtures.CREATED_AT, TestFixtures.UPDATED_AT,
                List.of(item));

        assertThat(item.getId()).isNull();
        assertThat(item.getProdutoId()).isEqualTo(TestFixtures.PRODUTO_ID);
        assertThat(item.getNome()).isEqualTo("Pizza");
        assertThat(item.getQuantidade()).isEqualTo(2);
        assertThat(item.getPreco()).isEqualByComparingTo("25.50");

        assertThat(pedido.getPagamentoId()).isEqualTo(TestFixtures.PAGAMENTO_ID);
        assertThat(pedido.getMotivoPendencia()).isEqualTo("gateway fora");
        assertThat(pedido.getItens()).containsExactly(item);
    }

    @Test
    void deveSalvarPedidoViaJpaRepository() {
        PedidoRepositoryAdapter adapter = new PedidoRepositoryAdapter(jpaRepository);
        Pedido pedido = TestFixtures.pedidoCriado();
        when(jpaRepository.save(org.mockito.ArgumentMatchers.any(PedidoJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Pedido salvo = adapter.salvar(pedido);

        assertThat(salvo.getId()).isEqualTo(TestFixtures.PEDIDO_ID);
        verify(jpaRepository).save(org.mockito.ArgumentMatchers.any(PedidoJpaEntity.class));
    }

    @Test
    void deveBuscarPedidoPorIdViaJpaRepository() {
        PedidoRepositoryAdapter adapter = new PedidoRepositoryAdapter(jpaRepository);
        PedidoJpaEntity entity = PedidoMapper.toEntity(TestFixtures.pedidoCriado());
        when(jpaRepository.findById(TestFixtures.PEDIDO_ID)).thenReturn(Optional.of(entity));

        Optional<Pedido> result = adapter.buscarPorId(TestFixtures.PEDIDO_ID);

        assertThat(result).get()
                .extracting(Pedido::getId)
                .isEqualTo(TestFixtures.PEDIDO_ID);
    }

    @Test
    void deveListarPedidosPorClienteViaJpaRepository() {
        PedidoRepositoryAdapter adapter = new PedidoRepositoryAdapter(jpaRepository);
        PedidoJpaEntity entity = PedidoMapper.toEntity(TestFixtures.pedidoCriado());
        when(jpaRepository.findByClienteIdOrderByCreatedAtDesc(TestFixtures.CLIENTE_ID)).thenReturn(List.of(entity));

        List<Pedido> result = adapter.listarPorCliente(TestFixtures.CLIENTE_ID);

        assertThat(result).singleElement()
                .extracting(Pedido::getClienteId)
                .isEqualTo(TestFixtures.CLIENTE_ID);
    }
}
