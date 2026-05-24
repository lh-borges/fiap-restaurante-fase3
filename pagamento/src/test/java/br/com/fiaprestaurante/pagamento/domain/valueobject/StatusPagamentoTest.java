package br.com.fiaprestaurante.pagamento.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusPagamentoTest {

    @Test
    void deveConterStatusEsperados() {
        assertThat(StatusPagamento.values())
                .containsExactly(StatusPagamento.PENDENTE, StatusPagamento.APROVADO, StatusPagamento.RECUSADO);
    }
}
