package br.com.fiaprestaurante.pagamento.application.usecase;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.port.input.ConsultarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConsultarPagamentoService implements ConsultarPagamentoUseCase {

    private final PagamentoRepository pagamentoRepository;

    public ConsultarPagamentoService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PagamentoResponse> porPedidoId(UUID pedidoId) {
        return pagamentoRepository.buscarPorPedidoId(pedidoId).map(PagamentoResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagamentoResponse> pendentes() {
        return pagamentoRepository.listarPendentes(Integer.MAX_VALUE)
                .stream()
                .map(PagamentoResponse::from)
                .toList();
    }
}
