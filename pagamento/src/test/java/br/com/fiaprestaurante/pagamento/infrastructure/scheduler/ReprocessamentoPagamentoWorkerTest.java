package br.com.fiaprestaurante.pagamento.infrastructure.scheduler;

import br.com.fiaprestaurante.pagamento.application.port.input.ReprocessarPendentesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link ReprocessamentoPagamentoWorker} - valida que
 * o tick chama o use case com o batch size correto e que excecoes nao
 * interrompem o agendador (Spring continuaria chamando o metodo).
 *
 * @author Danilo Fernando
 */
class ReprocessamentoPagamentoWorkerTest {

    private ReprocessarPendentesUseCase useCase;
    private ReprocessamentoPagamentoWorker worker;

    @BeforeEach
    void setUp() {
        useCase = mock(ReprocessarPendentesUseCase.class);
        worker = new ReprocessamentoPagamentoWorker(useCase, 20);
    }

    @Test
    void deveChamarUseCaseComOBatchSize() {
        when(useCase.executar(20)).thenReturn(3);

        worker.reprocessar();

        verify(useCase).executar(20);
    }

    @Test
    void excecaoDoUseCaseNaoDevePropagar() {
        when(useCase.executar(20)).thenThrow(new RuntimeException("falha"));

        // nao deve lancar - worker engole a excecao para manter o agendador vivo
        worker.reprocessar();

        verify(useCase).executar(20);
    }
}
