package br.com.fiaprestaurante.pagamento.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PagamentoNaoEncontradoExceptionTest {

    @Test
    void devePreservarMensagemEEstenderBusinessException() {
        PagamentoNaoEncontradoException exception = new PagamentoNaoEncontradoException("pagamento não encontrado");

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("pagamento não encontrado");
    }
}
