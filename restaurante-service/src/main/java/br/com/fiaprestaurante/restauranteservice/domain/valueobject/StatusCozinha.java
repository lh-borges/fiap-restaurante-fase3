package br.com.fiaprestaurante.restauranteservice.domain.valueobject;

/**
 * Estados possiveis de um pedido na fila da cozinha.
 *
 * <p>Transicoes validas:
 * <ul>
 *   <li>{@link #RECEBIDO} - estado inicial, criado a partir do evento
 *       {@code pedido.pronto-para-cozinha} (publicado pelo restaurante-pedido
 *       apos confirmacao do pagamento);</li>
 *   <li>{@link #RECEBIDO} -> {@link #EM_PREPARO} (cozinheiro inicia o preparo
 *       via mutation GraphQL);</li>
 *   <li>{@link #EM_PREPARO} -> {@link #PRONTO} (cozinheiro marca como pronto;
 *       estado terminal).</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
public enum StatusCozinha {

    /** Pedido recem-recebido pela cozinha, aguardando inicio do preparo. */
    RECEBIDO,

    /** Pedido em producao na cozinha. */
    EM_PREPARO,

    /** Pedido finalizado pela cozinha; pronto para entrega/retirada. */
    PRONTO
}
