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
