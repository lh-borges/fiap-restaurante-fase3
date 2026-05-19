package br.com.fiaprestaurante.pagamento.application.port.input;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;

public interface ProcessarPagamentoUseCase {

    PagamentoResponse executar(ProcessarPagamentoCommand command);
}
