package br.com.fiaprestaurante.pagamento.application.port.input;

public interface ReprocessarPendentesUseCase {

    int executar(int batchSize);
}
