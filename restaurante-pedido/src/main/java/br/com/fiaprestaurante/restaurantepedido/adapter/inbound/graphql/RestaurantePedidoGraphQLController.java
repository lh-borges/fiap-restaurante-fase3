package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto.ModuloRestaurantePedidoPayload;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.ConsultarModuloRestaurantePedidoUseCase;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class RestaurantePedidoGraphQLController {

    private final ConsultarModuloRestaurantePedidoUseCase useCase;

    public RestaurantePedidoGraphQLController(ConsultarModuloRestaurantePedidoUseCase useCase) {
        this.useCase = useCase;
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public ModuloRestaurantePedidoPayload statusModuloRestaurantePedido() {
        return new ModuloRestaurantePedidoPayload(useCase.executar());
    }
}
