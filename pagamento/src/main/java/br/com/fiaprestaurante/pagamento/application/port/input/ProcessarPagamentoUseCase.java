package br.com.fiaprestaurante.pagamento.application.port.input;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;

/**
 * Porta de entrada (use case) responsável por processar o pagamento de um
 * pedido recém-criado.
 *
 * <p>É invocada principalmente pelo consumer Kafka do tópico
 * {@code pedido.criado}, mas pode ser reutilizada por outros pontos de
 * entrada (REST, GraphQL, testes) sem alteração na implementação.
 *
 * <p>A operação é idempotente em relação ao {@code pedidoId}: chamadas
 * subsequentes para o mesmo pedido já APROVADO retornam o pagamento existente
 * sem reabrir o fluxo.
 *
 * @author Danilo Fernando
 */
public interface ProcessarPagamentoUseCase {

    /**
     * Executa o fluxo de processamento de pagamento.
     *
     * @param command dados do pedido a ser processado (id e valor total)
     * @return o estado final do pagamento ({@code APROVADO} ou {@code PENDENTE})
     */
    PagamentoResponse executar(ProcessarPagamentoCommand command);
}
