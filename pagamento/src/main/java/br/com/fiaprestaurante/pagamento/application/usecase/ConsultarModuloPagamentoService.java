package br.com.fiaprestaurante.pagamento.application.usecase;

import br.com.fiaprestaurante.pagamento.application.port.input.ConsultarModuloPagamentoUseCase;
import org.springframework.stereotype.Service;

@Service
public class ConsultarModuloPagamentoService implements ConsultarModuloPagamentoUseCase {

    @Override
    public String executar() {
        return "Módulo de pagamento estruturado para evolução das regras de negócio.";
    }
}
