package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarModuloRestaurantePedidoUseCase;
import org.springframework.stereotype.Service;

@Service
public class ConsultarModuloRestaurantePedidoService implements ConsultarModuloRestaurantePedidoUseCase {

    @Override
    public String executar() {
        return "Módulo restaurante/pedido estruturado — regras de negócio em desenvolvimento.";
    }
}
