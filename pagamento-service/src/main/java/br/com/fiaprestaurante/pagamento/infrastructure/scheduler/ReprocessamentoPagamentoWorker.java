package br.com.fiaprestaurante.pagamento.infrastructure.scheduler;

import br.com.fiaprestaurante.pagamento.application.port.input.ReprocessarPendentesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Worker agendado que dispara o reprocessamento de pagamentos PENDENTE.
 *
 * <p>Atende o requisito 4.6 da fase 3: quando o serviço externo (procpag)
 * volta a funcionar, este worker garante que os pagamentos que ficaram
 * pendentes serão reprocessados automaticamente, sem intervenção manual.
 *
 * <p>Periodicidade definida via {@code application.properties}:
 * <ul>
 *   <li>{@code pagamento.reprocess.initial-delay-ms} — espera inicial após startup;</li>
 *   <li>{@code pagamento.reprocess.fixed-delay-ms} — intervalo entre execuções (default 30s);</li>
 *   <li>{@code pagamento.reprocess.batch-size} — máximo de pendentes por ciclo.</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
@Component
public class ReprocessamentoPagamentoWorker {

    private static final Logger log = LoggerFactory.getLogger(ReprocessamentoPagamentoWorker.class);

    private final ReprocessarPendentesUseCase reprocessarPendentes;
    private final int batchSize;

    /**
     * @param reprocessarPendentes use case que faz a varredura e reprocessamento
     * @param batchSize            tamanho máximo do lote por ciclo
     */
    public ReprocessamentoPagamentoWorker(ReprocessarPendentesUseCase reprocessarPendentes,
                                          @Value("${pagamento.reprocess.batch-size}") int batchSize) {
        this.reprocessarPendentes = reprocessarPendentes;
        this.batchSize = batchSize;
    }

    /**
     * Método agendado — invoca o use case e registra resultado.
     *
     * <p>Qualquer exceção é capturada para evitar que o scheduler do Spring
     * suspenda o agendamento. O próximo ciclo tentará novamente.
     */
    @Scheduled(
            initialDelayString = "${pagamento.reprocess.initial-delay-ms}",
            fixedDelayString   = "${pagamento.reprocess.fixed-delay-ms}"
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
