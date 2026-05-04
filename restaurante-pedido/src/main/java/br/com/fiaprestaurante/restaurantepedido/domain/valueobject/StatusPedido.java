package br.com.fiaprestaurante.restaurantepedido.domain.valueobject;


/**
 * Enumeração que representa os possíveis estados de um pedido.
 * Define o ciclo de vida do pedido no sistema:
 * AGUARDANDO_CONFIRMACAO → CONFIRMADO ou CANCELADO.
 */
public enum StatusPedido {

    //estado inicial.
    AGUARDANDO_CONFIRMACAO,
    //pedido confirmado.
    CONFIRMADO,
    //pedido cancelado.
    CANCELADO

}
