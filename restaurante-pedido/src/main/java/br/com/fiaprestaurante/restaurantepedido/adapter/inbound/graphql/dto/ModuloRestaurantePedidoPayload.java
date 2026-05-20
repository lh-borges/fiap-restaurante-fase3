package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto;

/**
 * Payload da query GraphQL {@code statusModuloRestaurantePedido} — health
 * check informativo do módulo.
 *
 * <p>Mantido para retrocompatibilidade da coleção Postman e GraphiQL.
 */
public class ModuloRestaurantePedidoPayload {

    private final String nome = "restaurante-pedido";
    private final boolean implementado = true;
    private final String descricao;

    /**
     * @param descricao descrição livre do status atual (vinda do use case)
     */
    public ModuloRestaurantePedidoPayload(String descricao) {
        this.descricao = descricao;
    }

    /** @return nome curto do módulo */
    public String getNome() {
        return nome;
    }

    /** @return {@code true} quando o módulo está implementado e operacional */
    public boolean isImplementado() {
        return implementado;
    }

    /** @return descrição livre do status atual */
    public String getDescricao() {
        return descricao;
    }
}
