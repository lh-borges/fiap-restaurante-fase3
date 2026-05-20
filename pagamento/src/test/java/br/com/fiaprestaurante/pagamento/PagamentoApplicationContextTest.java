package br.com.fiaprestaurante.pagamento;

import br.com.fiaprestaurante.pagamento.application.port.input.ConsultarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.application.port.input.ProcessarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.application.port.input.ReprocessarPendentesUseCase;
import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import br.com.fiaprestaurante.pagamento.application.port.output.PaymentEventPublisher;
import br.com.fiaprestaurante.pagamento.application.port.output.PaymentGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test que garante que o contexto Spring do {@code pagamento} sobe
 * por inteiro com todos os beans wirados corretamente.
 *
 * <p>Usa H2 em memória (configurado em {@code src/test/resources/application.properties})
 * no lugar do MySQL e {@link EmbeddedKafka} no lugar do broker real, então
 * roda sem dependência externa.
 *
 * @author Danilo Fernando
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"pedido.criado", "pagamento.aprovado", "pagamento.pendente"})
class PagamentoApplicationContextTest {

    @Autowired
    private ProcessarPagamentoUseCase processarPagamento;

    @Autowired
    private ReprocessarPendentesUseCase reprocessarPendentes;

    @Autowired
    private ConsultarPagamentoUseCase consultarPagamento;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    /**
     * Verifica que os principais beans (use cases + ports de saída) estão
     * disponíveis no contexto — falha aqui significa erro de wiring/config.
     */
    @Test
    void deveCarregarTodosOsBeansPrincipais() {
        assertThat(processarPagamento).isNotNull();
        assertThat(reprocessarPendentes).isNotNull();
        assertThat(consultarPagamento).isNotNull();
        assertThat(pagamentoRepository).isNotNull();
        assertThat(paymentGateway).isNotNull();
        assertThat(paymentEventPublisher).isNotNull();
    }
}
