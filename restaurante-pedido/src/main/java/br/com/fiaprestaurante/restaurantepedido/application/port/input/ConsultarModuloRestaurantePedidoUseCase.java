package br.com.fiaprestaurante.restaurantepedido.application.port.input;

/**
 * Porta de entrada para a query de health-check do módulo
 * {@code restaurante-pedido}.
 *
 * <p>Mantida para retrocompatibilidade da coleção Postman e do GraphiQL —
 * permite confirmar rapidamente que o serviço está respondendo a queries
 * autenticadas.
 */
public interface ConsultarModuloRestaurantePedidoUseCase {

    /**
     * @return descrição livre do estado atual do módulo
     */
    String executar();
}
