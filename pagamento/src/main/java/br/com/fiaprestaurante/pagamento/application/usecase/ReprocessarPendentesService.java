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

/**
 * Implementação de {@link ReprocessarPendentesUseCase} — varre o repositório
 * em busca de pagamentos PENDENTE e os reprocessa via
 * {@link ProcessarPagamentoUseCase} (reusando exatamente o mesmo fluxo do
 * caminho principal).
 *
 * <p>Chamada periodicamente pelo {@code ReprocessamentoPagamentoWorker}.
 * Atende o requisito 4.6 da fase 3.
 *
 * @author Danilo Fernando
 */
@Service
public class ReprocessarPendentesService implements ReprocessarPendentesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReprocessarPendentesService.class);

    private final PagamentoRepository pagamentoRepository;
    private final ProcessarPagamentoUseCase processarPagamento;

    /**
     * @param pagamentoRepository fonte dos pagamentos pendentes
     * @param processarPagamento  use case principal que será reusado para cada pendente
     */
    public ReprocessarPendentesService(PagamentoRepository pagamentoRepository,
                                       ProcessarPagamentoUseCase processarPagamento) {
        this.pagamentoRepository = pagamentoRepository;
        this.processarPagamento = processarPagamento;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Cada pagamento é processado isoladamente — uma exceção em um
     * elemento não interrompe os demais.
     */
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
