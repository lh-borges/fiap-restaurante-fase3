package br.com.fiaprestaurante.pagamento.infrastructure.scheduler;

import br.com.fiaprestaurante.pagamento.application.port.input.ReprocessarPendentesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReprocessamentoPagamentoWorker {

    private static final Logger log = LoggerFactory.getLogger(ReprocessamentoPagamentoWorker.class);

    private final ReprocessarPendentesUseCase reprocessarPendentes;
    private final int batchSize;

    public ReprocessamentoPagamentoWorker(ReprocessarPendentesUseCase reprocessarPendentes,
                                          @Value("${pagamento.reprocess.batch-size}") int batchSize) {
        this.reprocessarPendentes = reprocessarPendentes;
        this.batchSize = batchSize;
    }

    @Scheduled(
            initialDelayString = "${pagamento.reprocess.initial-delay-ms}",
            fixedDelayString = "${pagamento.reprocess.fixed-delay-ms}"
    )
    public void reprocessar() {
        try {
            int aprovados = reprocessarPendentes.executar(batchSize);
            if (aprovados > 0) {
                log.info("Worker reprocessou pendentes: {} aprovado(s) neste ciclo", aprovados);
            }
        } catch (Exception e) {
            log.error("Falha no ciclo de reprocessamento: {}", e.getMessage(), e);
        }
    }
}
