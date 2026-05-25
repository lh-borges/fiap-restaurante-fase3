package br.com.fiaprestaurante.pagamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entrypoint do microsserviço {@code pagamento}.
 *
 * <p>O serviço é responsável por:
 * <ul>
 *   <li>Consumir o tópico Kafka {@code pedido.criado};</li>
 *   <li>Chamar o gateway externo (procpag) com políticas de resiliência
 *       (Circuit Breaker + Retry + Timeout + Fallback);</li>
 *   <li>Persistir o resultado no MySQL e publicar nos tópicos
 *       {@code pagamento.aprovado} ou {@code pagamento.pendente};</li>
 *   <li>Reprocessar periodicamente pagamentos pendentes
 *       ({@link org.springframework.scheduling.annotation.EnableScheduling});</li>
 *   <li>Expor queries GraphQL protegidas por JWT.</li>
 * </ul>
 *
 * <p>Atende os requisitos 4.4 a 4.7 e 5.2, 5.3, 5.4 e 5.5 da fase 3
 * do Tech Challenge.
 *
 * @author Danilo Fernando
 */
@SpringBootApplication
@EnableScheduling
public class PagamentoApplication {

    /**
     * Bootstrap padrão do Spring Boot.
     *
     * @param args argumentos de linha de comando (repassados ao Spring)
     */
    public static void main(String[] args) {
        SpringApplication.run(PagamentoApplication.class, args);
    }
}
