package br.com.fiaprestaurante.restaurantepedido;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusPagamentoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConfirmarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.CriarPedidoUseCase;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoEventPublisher;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test que garante que o contexto Spring do {@code restaurante-pedido}
 * sobe por inteiro com todos os beans wirados corretamente.
 *
 * <p>Usa H2 em memoria (configurado em {@code src/test/resources/application.properties})
 * no lugar do MySQL e {@link EmbeddedKafka} no lugar do broker real, entao
 * roda sem dependencia externa.
 *
 * @author Danilo Fernando
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "pedido.criado", "pagamento.aprovado", "pagamento.pendente",
        "pedido.pronto-para-cozinha", "pedido.em-preparo", "pedido.pronto"
})
class RestaurantePedidoApplicationContextTest {

    @Autowired
    private CriarPedidoUseCase criarPedido;

    @Autowired
    private ConfirmarPedidoUseCase confirmarPedido;

    @Autowired
    private ConsultarPedidoUseCase consultarPedido;

    @Autowired
    private AtualizarStatusPagamentoUseCase atualizarStatusPagamento;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoEventPublisher pedidoEventPublisher;

    /**
     * Verifica que os principais beans (use cases + ports de saida) estao
     * disponiveis no contexto - falha aqui significa erro de wiring/config.
     */
    @Test
    void deveCarregarTodosOsBeansPrincipais() {
        assertThat(criarPedido).isNotNull();
        assertThat(confirmarPedido).isNotNull();
        assertThat(consultarPedido).isNotNull();
        assertThat(atualizarStatusPagamento).isNotNull();
        assertThat(pedidoRepository).isNotNull();
        assertThat(pedidoEventPublisher).isNotNull();
    }
}
