package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarModuloRestaurantePedidoUseCase;
import org.springframework.stereotype.Service;

/**
 * Implementação de {@link ConsultarModuloRestaurantePedidoUseCase} — retorna
 * uma descrição estática do status atual do módulo.
 */
@Service
public class ConsultarModuloRestaurantePedidoService implements ConsultarModuloRestaurantePedidoUseCase {

    /** {@inheritDoc} */
    @Override
    public String executar() {
        return "Módulo restaurante/pedido operacional — criação, confirmação, consultas e integração Kafka ativos.";
    }
}
