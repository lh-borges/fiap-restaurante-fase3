package br.com.fiaprestaurante.pagamento.infrastructure.scheduler;

import br.com.fiaprestaurante.pagamento.application.port.input.ReprocessarPendentesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReprocessamentoPagamentoWorkerTest {

    @Mock
    private ReprocessarPendentesUseCase reprocessarPendentes;

    private ReprocessamentoPagamentoWorker worker;

    @BeforeEach
    void setUp() {
        worker = new ReprocessamentoPagamentoWorker(reprocessarPendentes, 50);
    }

    @Test
    void deveExecutarUseCaseComBatchSizeConfigurado() {
        worker.reprocessar();

        verify(reprocessarPendentes).executar(50);
    }

    @Test
    void deveCapturarExcecaoParaNaoInterromperScheduler() {
        when(reprocessarPendentes.executar(50)).thenThrow(new RuntimeException("erro"));

        worker.reprocessar();

        verify(reprocessarPendentes).executar(50);
    }
}
