package br.com.fiaprestaurante.pagamento.application.port.output;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoAprovadoEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoPendenteEvent;

/**
 * Porta de saída para publicação de eventos de pagamento em mensageria.
 *
 * <p>Abstrai o broker (Kafka, RabbitMQ, etc.) do use case. A implementação
 * padrão é {@code PaymentKafkaPublisher} e publica nos tópicos
 * {@code pagamento.aprovado} e {@code pagamento.pendente}, conforme o
 * requisito 5.3 da fase 3.
 *
 * @author Danilo Fernando
 */
public interface PaymentEventPublisher {

    /**
     * Publica evento de pagamento aprovado para que consumidores (notavelmente
     * o {@code restaurante-pedido}) atualizem o status do pedido para PAGO.
     *
     * @param event evento contendo {@code pedidoId}, {@code pagamentoId} e timestamp
     */
    void publicarPagamentoAprovado(PagamentoAprovadoEvent event);

    /**
     * Publica evento de pagamento pendente para que o {@code restaurante-pedido}
     * marque o pedido como PENDENTE_PAGAMENTO e o worker de reprocessamento
     * o considere no próximo ciclo.
     *
     * @param event evento contendo {@code pedidoId}, {@code pagamentoId}, motivo e timestamp
     */
    void publicarPagamentoPendente(PagamentoPendenteEvent event);
}
