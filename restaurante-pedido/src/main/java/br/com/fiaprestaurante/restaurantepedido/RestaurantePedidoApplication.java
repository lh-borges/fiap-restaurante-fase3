package br.com.fiaprestaurante.restaurantepedido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entrypoint do microsserviço {@code restaurante-pedido}.
 *
 * <p>O serviço é responsável por:
 * <ul>
 *   <li>Criar e confirmar pedidos via GraphQL;</li>
 *   <li>Publicar o evento {@code pedido.criado} no Kafka ao confirmar;</li>
 *   <li>Consumir os eventos {@code pagamento.aprovado} e {@code pagamento.pendente}
 *       para atualizar automaticamente o status do pedido;</li>
 *   <li>Expor consultas (por ID e por cliente autenticado) protegidas por JWT.</li>
 * </ul>
 *
 * <p>Atende os requisitos 4.2, 4.3, 4.5, 4.6, 4.7, 5.2, 5.3 e 5.5 da fase 3
 * do Tech Challenge.
 */
@SpringBootApplication
public class RestaurantePedidoApplication {

    /**
     * Bootstrap padrão do Spring Boot.
     *
     * @param args argumentos de linha de comando (repassados ao Spring)
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantePedidoApplication.class, args);
    }
}
