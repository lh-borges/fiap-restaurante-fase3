package br.com.fiaprestaurante.restauranteservice.adapter.inbound.graphql;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.port.input.ConsultarFilaCozinhaUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.input.IniciarPreparoUseCase;
import br.com.fiaprestaurante.restauranteservice.application.port.input.MarcarComoProntoUseCase;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * Adapter de entrada GraphQL — expoe mutations e queries de cozinha.
 *
 * <p>Schema correspondente em {@code src/main/resources/graphql/schema.graphqls}.
 *
 * <p>Todas as operacoes sao restritas ao perfil {@code DONO_RESTAURANTE}
 * (apenas o restaurante manipula sua propria fila de cozinha).
 *
 * @author Danilo Fernando
 */
@Controller
public class RestauranteServiceGraphQLController {

    private final ConsultarFilaCozinhaUseCase consultarFila;
    private final IniciarPreparoUseCase iniciarPreparo;
    private final MarcarComoProntoUseCase marcarComoPronto;

    public RestauranteServiceGraphQLController(ConsultarFilaCozinhaUseCase consultarFila,
                                               IniciarPreparoUseCase iniciarPreparo,
                                               MarcarComoProntoUseCase marcarComoPronto) {
        this.consultarFila = consultarFila;
        this.iniciarPreparo = iniciarPreparo;
        this.marcarComoPronto = marcarComoPronto;
    }

    /**
     * Query {@code filaCozinha(status: String): [PedidoCozinha!]!}.
     *
     * @param status valor opcional do enum {@link StatusCozinha} para filtrar
     * @return lista (possivelmente vazia) de pedidos ordenados por createdAt
     */
    @QueryMapping
    @PreAuthorize("hasAuthority('DONO_RESTAURANTE')")
    public List<PedidoCozinhaResponse> filaCozinha(@Argument String status) {
        return consultarFila.listar(parseStatusOuNull(status));
    }

    /**
     * Query {@code pedidoCozinhaPorId(pedidoCozinhaId: ID!): PedidoCozinha}.
     */
    @QueryMapping
    @PreAuthorize("hasAuthority('DONO_RESTAURANTE')")
    public PedidoCozinhaResponse pedidoCozinhaPorId(@Argument String pedidoCozinhaId) {
        return consultarFila.porId(UUID.fromString(pedidoCozinhaId)).orElse(null);
    }

    /**
     * Mutation {@code iniciarPreparo(pedidoCozinhaId: ID!): PedidoCozinha!}.
     */
    @MutationMapping
    @PreAuthorize("hasAuthority('DONO_RESTAURANTE')")
    public PedidoCozinhaResponse iniciarPreparo(@Argument String pedidoCozinhaId) {
        // Forca a extracao do JWT — se nao autenticado, lanca BusinessException
        // (defesa em profundidade alem do @PreAuthorize).
        AuthenticatedUser.id();
        return iniciarPreparo.executar(UUID.fromString(pedidoCozinhaId));
    }

    /**
     * Mutation {@code marcarComoPronto(pedidoCozinhaId: ID!): PedidoCozinha!}.
     */
    @MutationMapping
    @PreAuthorize("hasAuthority('DONO_RESTAURANTE')")
    public PedidoCozinhaResponse marcarComoPronto(@Argument String pedidoCozinhaId) {
        AuthenticatedUser.id();
        return marcarComoPronto.executar(UUID.fromString(pedidoCozinhaId));
    }

    private static StatusCozinha parseStatusOuNull(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StatusCozinha.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status invalido: " + status);
        }
    }
}
