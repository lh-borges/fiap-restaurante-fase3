package br.com.fiaprestaurante.pagamento.application.usecase;

import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;
import br.com.fiaprestaurante.pagamento.application.port.input.ProcessarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.application.port.input.ReprocessarPendentesUseCase;
import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReprocessarPendentesService implements ReprocessarPendentesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReprocessarPendentesService.class);

    private final PagamentoRepository pagamentoRepository;
    private final ProcessarPagamentoUseCase processarPagamento;

    public ReprocessarPendentesService(PagamentoRepository pagamentoRepository,
                                       ProcessarPagamentoUseCase processarPagamento) {
        this.pagamentoRepository = pagamentoRepository;
        this.processarPagamento = processarPagamento;
    }

    @Override
    public int executar(int batchSize) {
        List<Pagamento> pendentes = pagamentoRepository.listarPendentes(batchSize);
        if (pendentes.isEmpty()) {
            return 0;
        }
        log.info("Reprocessando {} pagamento(s) pendente(s)", pendentes.size());

        int aprovados = 0;
        for (Pagamento pendente : pendentes) {
            try {
                var response = processarPagamento.executar(
                        new ProcessarPagamentoCommand(pendente.getPedidoId(), pendente.getValor())
                );
                if ("APROVADO".equals(response.status())) {
                    aprovados++;
                }
            } catch (Exception e) {
                log.warn("Falha ao reprocessar pagamento pedidoId={}: {}", pendente.getPedidoId(), e.getMessage());
            }
        }
        log.info("Reprocessamento concluído: {}/{} aprovados", aprovados, pendentes.size());
        return aprovados;
    }
}
