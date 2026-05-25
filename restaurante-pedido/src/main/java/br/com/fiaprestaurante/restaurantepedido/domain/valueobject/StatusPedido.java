package br.com.fiaprestaurante.restaurantepedido.domain.valueobject;

/**
 * Estados possíveis de um pedido ao longo do seu ciclo de vida.
 *
 * <p>Transições válidas:
 * <ul>
 *   <li>{@link #CRIADO} → {@link #CONFIRMADO} (cliente confirma o pedido após
 *       receber o valor total calculado);</li>
 *   <li>{@link #CRIADO}/{@link #CONFIRMADO} → {@link #PENDENTE_PAGAMENTO}
 *       (gateway de pagamento indisponível — requisito 4.5);</li>
 *   <li>{@link #CONFIRMADO}/{@link #PENDENTE_PAGAMENTO} → {@link #PAGO}
 *       (gateway autorizou — requisito 4.6/4.7);</li>
 *   <li>{@link #CRIADO}/{@link #CONFIRMADO}/{@link #PENDENTE_PAGAMENTO} →
 *       {@link #CANCELADO} (estado terminal de desistência).</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
public enum StatusPedido {

    /** Pedido recém-criado, aguardando confirmação do cliente. */
    CRIADO,

    /** Pedido confirmado pelo cliente, pagamento ainda não processado. */
    CONFIRMADO,

    /** Gateway de pagamento indisponível; aguarda reprocessamento automático. */
    PENDENTE_PAGAMENTO,

    /** Pagamento aprovado pelo gateway externo. */
    PAGO,

    /** Cozinha (restaurante-service) recebeu o pedido e iniciou o preparo. */
    EM_PREPARO,

    /** Cozinha finalizou o preparo; pedido pronto para entrega/retirada. */
    PRONTO,

    /** Pedido cancelado; estado terminal sem cobrança. */
    CANCELADO
}
