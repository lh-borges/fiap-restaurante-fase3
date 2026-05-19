package br.com.fiaprestaurante.pagamento.adapter.inbound.graphql;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.port.input.ConsultarPagamentoUseCase;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class PagamentoGraphQLController {

    private final ConsultarPagamentoUseCase consultarPagamento;

    public PagamentoGraphQLController(ConsultarPagamentoUseCase consultarPagamento) {
        this.consultarPagamento = consultarPagamento;
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public PagamentoResponse pagamentoPorPedido(@Argument String pedidoId) {
        return consultarPagamento.porPedidoId(UUID.fromString(pedidoId)).orElse(null);
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('DONO_RESTAURANTE')")
    public List<PagamentoResponse> pagamentosPendentes() {
        return consultarPagamento.pendentes();
    }
}
