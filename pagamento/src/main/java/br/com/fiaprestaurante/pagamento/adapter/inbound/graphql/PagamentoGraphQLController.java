package br.com.fiaprestaurante.pagamento.adapter.inbound.graphql;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.port.input.ConsultarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.domain.exception.PagamentoNaoEncontradoException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * Adapter de entrada GraphQL — expõe as queries de consulta de pagamento.
 *
 * <p>Schema correspondente em {@code src/main/resources/graphql/schema.graphqls}.
 *
 * <p>Autorização via {@link PreAuthorize} usando a claim {@code groups} do
 * JWT emitido pelo {@code usuario-autenticacao}. Sem token válido, a
 * resposta vem como {@code UNAUTHORIZED}.
 *
 * @author Danilo Fernando
 */
@Controller
public class PagamentoGraphQLController {

    private final ConsultarPagamentoUseCase consultarPagamento;

    /**
     * @param consultarPagamento porta de entrada para consultas
     */
    public PagamentoGraphQLController(ConsultarPagamentoUseCase consultarPagamento) {
        this.consultarPagamento = consultarPagamento;
    }

    /**
     * Query GraphQL {@code pagamentoPorPedido(pedidoId: ID!)}.
     *
     * <p>Acessível por usuários autenticados com authority {@code USUARIO} ou
     * {@code DONO_RESTAURANTE}.
     *
     * @param pedidoId identificador do pedido em formato UUID (string)
     * @return o pagamento correspondente
     * @throws PagamentoNaoEncontradoException quando o pagamento não existe
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public PagamentoResponse pagamentoPorPedido(@Argument String pedidoId) {
        return consultarPagamento.porPedidoId(UUID.fromString(pedidoId))
                .orElseThrow(() -> new PagamentoNaoEncontradoException(
                        "Pagamento não encontrado para o pedido informado."));
    }

    /**
     * Query GraphQL {@code pagamentosPendentes: [Pagamento!]!}.
     *
     * <p>Restrita ao perfil {@code DONO_RESTAURANTE}, pois envolve dados
     * operacionais de múltiplos pedidos.
     *
     * @return lista de pagamentos em status {@code PENDENTE}
     */
    @QueryMapping
    @PreAuthorize("hasAnyAuthority('DONO_RESTAURANTE')")
    public List<PagamentoResponse> pagamentosPendentes() {
        return consultarPagamento.pendentes();
    }
}
