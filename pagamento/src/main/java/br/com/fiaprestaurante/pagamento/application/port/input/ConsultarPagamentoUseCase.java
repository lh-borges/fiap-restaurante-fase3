package br.com.fiaprestaurante.pagamento.application.port.input;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultarPagamentoUseCase {

    Optional<PagamentoResponse> porPedidoId(UUID pedidoId);

    List<PagamentoResponse> pendentes();
}
