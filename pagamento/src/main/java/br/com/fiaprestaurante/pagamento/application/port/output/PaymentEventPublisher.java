package br.com.fiaprestaurante.pagamento.application.port.output;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoAprovadoEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoPendenteEvent;

public interface PaymentEventPublisher {

    void publicarPagamentoAprovado(PagamentoAprovadoEvent event);

    void publicarPagamentoPendente(PagamentoPendenteEvent event);
}
