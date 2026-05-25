package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link CriarPedidoService} - usa mock do
 * {@link PedidoRepository} para isolar a logica de orquestracao do
 * use case.
 *
 * @author Danilo Fernando
 */
class CriarPedidoServiceTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private PedidoRepository pedidoRepository;
    private CriarPedidoService service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        service = new CriarPedidoService(pedidoRepository);
    }

    @Test
    void deveCriarPedidoCalculandoValorTotalEPersistir() {
        CriarPedidoCommand command = new CriarPedidoCommand(CLIENTE_ID, RESTAURANTE_ID, List.of(
                new ItemPedidoCommand(PRODUTO_ID, "X-Burger", 2, new BigDecimal("25.90")),
                new ItemPedidoCommand(PRODUTO_ID, "Refrigerante", 1, new BigDecimal("7.50"))
        ));
        when(pedidoRepository.salvar(org.mockito.ArgumentMatchers.any(Pedido.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponse resposta = service.executar(command);

        assertThat(resposta.id()).isNotNull();
        assertThat(resposta.status()).isEqualTo("CRIADO");
        assertThat(resposta.valorTotal()).isEqualByComparingTo("59.30");
        assertThat(resposta.itens()).hasSize(2);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        org.mockito.Mockito.verify(pedidoRepository).salvar(captor.capture());
        assertThat(captor.getValue().getClienteId()).isEqualTo(CLIENTE_ID);
        assertThat(captor.getValue().getRestauranteId()).isEqualTo(RESTAURANTE_ID);
    }

    @Test
    void devePropagarExcecaoDeDominioQuandoItemInvalido() {
        CriarPedidoCommand command = new CriarPedidoCommand(CLIENTE_ID, RESTAURANTE_ID, List.of(
                new ItemPedidoCommand(PRODUTO_ID, "X-Burger", 0, new BigDecimal("25.90"))
        ));

        assertThatThrownBy(() -> service.executar(command))
                .isInstanceOf(BusinessException.class);
    }
}
