package br.com.fiaprestaurante.restaurantepedido.domain.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;

import java.util.UUID;

/**
 * Exceção de domínio lançada quando um pedido não é encontrado no sistema.
 * Estende BusinessException do módulo shared para padronização
 * do tratamento de erros entre os módulos do projeto.
 */
public class PedidoNaoEncontradoException extends BusinessException {

    public PedidoNaoEncontradoException(UUID id) {
        super("Pedido não encontrado com id: " + id);
    }
}
