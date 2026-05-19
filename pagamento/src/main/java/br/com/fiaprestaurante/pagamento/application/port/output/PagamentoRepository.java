package br.com.fiaprestaurante.pagamento.application.port.output;

import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagamentoRepository {

    Pagamento salvar(Pagamento pagamento);

    Optional<Pagamento> buscarPorPedidoId(UUID pedidoId);

    List<Pagamento> listarPendentes(int limite);
}
