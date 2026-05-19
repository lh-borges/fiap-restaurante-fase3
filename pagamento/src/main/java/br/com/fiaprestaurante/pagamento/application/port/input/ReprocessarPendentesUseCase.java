package br.com.fiaprestaurante.pagamento.application.port.input;

/**
 * Porta de entrada (use case) acionada periodicamente pelo
 * {@code ReprocessamentoPagamentoWorker} para tentar reprocessar pagamentos
 * que ficaram em {@code PENDENTE} devido a falha temporária do gateway
 * externo.
 *
 * <p>Atende o requisito 4.6 da fase 3: quando o serviço externo voltar a
 * funcionar, os pendentes devem ser reprocessados automaticamente.
 *
 * @author Danilo Fernando
 */
public interface ReprocessarPendentesUseCase {

    /**
     * Tenta reprocessar um lote de pagamentos pendentes.
     *
     * @param batchSize quantidade máxima de pagamentos a reprocessar nesta chamada
     * @return número de pagamentos que terminaram aprovados após o reprocessamento
     */
    int executar(int batchSize);
}
