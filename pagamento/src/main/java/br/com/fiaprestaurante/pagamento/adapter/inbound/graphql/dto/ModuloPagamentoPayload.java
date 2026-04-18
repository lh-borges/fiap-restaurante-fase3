package br.com.fiaprestaurante.pagamento.adapter.inbound.graphql.dto;

public class ModuloPagamentoPayload {

    private final String nome = "pagamento";
    private final boolean implementado = false;
    private final String descricao;

    public ModuloPagamentoPayload(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() { return nome; }
    public boolean isImplementado() { return implementado; }
    public String getDescricao() { return descricao; }
}
