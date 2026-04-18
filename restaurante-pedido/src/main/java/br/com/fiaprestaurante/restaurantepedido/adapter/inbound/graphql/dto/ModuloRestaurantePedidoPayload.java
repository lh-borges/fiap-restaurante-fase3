package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql.dto;

public class ModuloRestaurantePedidoPayload {

    private final String nome = "restaurante-pedido";
    private final boolean implementado = false;
    private final String descricao;

    public ModuloRestaurantePedidoPayload(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() { return nome; }
    public boolean isImplementado() { return implementado; }
    public String getDescricao() { return descricao; }
}
