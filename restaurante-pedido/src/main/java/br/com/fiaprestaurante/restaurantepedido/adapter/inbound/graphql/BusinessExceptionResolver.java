package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.graphql;

import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

/**
 * Resolver de exceções para o GraphQL — traduz {@link BusinessException} (e
 * subclasses) em {@link GraphQLError} com classificação adequada.
 *
 * <p>Sem este resolver, exceções de regra de negócio chegariam ao cliente
 * como erro interno (5xx no equivalente HTTP), atrapalhando o consumo das
 * APIs em ferramentas como Postman.
 *
 * @author Danilo Fernando
 */
@Component
public class BusinessExceptionResolver extends DataFetcherExceptionResolverAdapter {

    /**
     * Mapeia exceções de negócio para tipos de erro GraphQL.
     *
     * @param ex   exceção lançada pelo data fetcher
     * @param env  ambiente do data fetcher (contém o caminho do campo)
     * @return {@link GraphQLError} adequado, ou {@code null} para delegar
     *         ao tratamento default do framework
     */
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof PedidoNaoEncontradoException) {
            return GraphQLError.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        }
        if (ex instanceof BusinessException) {
            return GraphQLError.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        }
        return null;
    }
}
