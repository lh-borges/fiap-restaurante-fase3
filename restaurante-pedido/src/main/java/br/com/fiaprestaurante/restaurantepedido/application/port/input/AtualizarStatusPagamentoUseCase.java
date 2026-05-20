package br.com.fiaprestaurante.restaurantepedido.application.port.input;

import java.util.UUID;

/**
 * Porta de entrada para a atualização automática do status do pedido em
 * resposta a eventos do microsserviço de pagamento (requisitos 4.6 e 4.7).
 *
 * <p>Invocada pelos consumers Kafka dos tópicos {@code pagamento.aprovado} e
 * {@code pagamento.pendente}.
 *
 * @author Danilo Fernando
 */
public interface AtualizarStatusPagamentoUseCase {

    /**
     * Marca o pedido como PAGO após confirmação do gateway.
     *
     * <p>Idempotente: chamadas repetidas com o mesmo {@code pagamentoId}
     * não causam efeitos colaterais.
     *
     * @param pedidoId    identificador do pedido aprovado
     * @param pagamentoId identidade do pagamento que aprovou a cobrança
     */
    void marcarComoPago(UUID pedidoId, UUID pagamentoId);

    /**
     * Marca o pedido como PENDENTE_PAGAMENTO quando o gateway está
     * indisponível.
     *
     * @param pedidoId identificador do pedido pendente
     * @param motivo   descrição da falha do gateway (logada e armazenada)
     */
    void marcarComoPendente(UUID pedidoId, String motivo);
}
