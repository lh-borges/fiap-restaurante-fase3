package br.com.fiaprestaurante.restauranteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entrypoint do microsservico {@code restaurante-service} — modulo opcional
 * (5.1 da fase 3) que representa a cozinha/producao.
 *
 * <p>O servico eh responsavel por:
 * <ul>
 *   <li>Consumir o evento {@code pedido.pronto-para-cozinha} (publicado pelo
 *       {@code restaurante-pedido} apos a confirmacao do pagamento);</li>
 *   <li>Manter uma fila de pedidos por status (RECEBIDO, EM_PREPARO, PRONTO);</li>
 *   <li>Expor mutations GraphQL para o dono do restaurante iniciar o preparo
 *       e marcar como pronto;</li>
 *   <li>Publicar {@code pedido.em-preparo} e {@code pedido.pronto} para o
 *       {@code restaurante-pedido} refletir o status no Pedido principal.</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
@SpringBootApplication
public class RestauranteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestauranteServiceApplication.class, args);
    }
}
