package br.com.fiaprestaurante.pagamento.adapter.inbound.graphql;

import br.com.fiaprestaurante.pagamento.adapter.inbound.graphql.dto.ModuloPagamentoPayload;
import br.com.fiaprestaurante.pagamento.application.port.input.ConsultarModuloPagamentoUseCase;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class PagamentoGraphQLController {

    private final ConsultarModuloPagamentoUseCase useCase;

    public PagamentoGraphQLController(ConsultarModuloPagamentoUseCase useCase) {
        this.useCase = useCase;
    }

    @QueryMapping
    @PreAuthorize("hasAnyAuthority('USUARIO', 'DONO_RESTAURANTE')")
    public ModuloPagamentoPayload statusModuloPagamento() {
        return new ModuloPagamentoPayload(useCase.executar());
    }
}
