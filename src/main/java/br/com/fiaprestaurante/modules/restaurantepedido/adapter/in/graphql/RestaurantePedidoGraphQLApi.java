package br.com.fiaprestaurante.modules.restaurantepedido.adapter.in.graphql;

import br.com.fiaprestaurante.modules.restaurantepedido.adapter.in.graphql.dto.ModuloRestaurantePedidoPayload;
import br.com.fiaprestaurante.modules.restaurantepedido.application.port.in.ConsultarModuloRestaurantePedidoUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ApplicationScoped
public class RestaurantePedidoGraphQLApi {

    private final ConsultarModuloRestaurantePedidoUseCase consultarModuloRestaurantePedidoUseCase;

    @Inject
    public RestaurantePedidoGraphQLApi(ConsultarModuloRestaurantePedidoUseCase consultarModuloRestaurantePedidoUseCase) {
        this.consultarModuloRestaurantePedidoUseCase = consultarModuloRestaurantePedidoUseCase;
    }

    @Query("statusModuloRestaurantePedido")
    @RolesAllowed({ "CLIENTE", "ADMINISTRADOR" })
    @Description("Retorna o status do modulo restaurante/pedido. Requer usuario autenticado.")
    public ModuloRestaurantePedidoPayload statusModuloRestaurantePedido() {
        return new ModuloRestaurantePedidoPayload(consultarModuloRestaurantePedidoUseCase.executar());
    }
}
