package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.CredenciaisInvalidasException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * Tratamento global de excecoes para resolvers GraphQL do modulo usuario-autenticacao.
 */
@ControllerAdvice
public class UsuarioGraphQLExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(UsuarioGraphQLExceptionHandler.class);

    @GraphQlExceptionHandler(UsuarioNaoEncontradoException.class)
    public GraphQLError handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException exception,
                                                   DataFetchingEnvironment environment) {
        return error(environment, ErrorType.NOT_FOUND, exception.getMessage());
    }

    @GraphQlExceptionHandler(CredenciaisInvalidasException.class)
    public GraphQLError handleCredenciaisInvalidas(CredenciaisInvalidasException exception,
                                                   DataFetchingEnvironment environment) {
        return error(environment, ErrorType.UNAUTHORIZED, exception.getMessage());
    }

    @GraphQlExceptionHandler(BusinessException.class)
    public GraphQLError handleBusinessException(BusinessException exception,
                                                DataFetchingEnvironment environment) {
        return error(environment, ErrorType.BAD_REQUEST, exception.getMessage());
    }

    @GraphQlExceptionHandler(IllegalArgumentException.class)
    public GraphQLError handleIllegalArgument(IllegalArgumentException exception,
                                              DataFetchingEnvironment environment) {
        return error(environment, ErrorType.BAD_REQUEST, "Argumento invalido: " + exception.getMessage());
    }

    @GraphQlExceptionHandler(AuthenticationException.class)
    public GraphQLError handleAuthentication(AuthenticationException exception,
                                             DataFetchingEnvironment environment) {
        return error(environment, ErrorType.UNAUTHORIZED, "Autenticacao obrigatoria.");
    }

    @GraphQlExceptionHandler(AccessDeniedException.class)
    public GraphQLError handleAccessDenied(AccessDeniedException exception,
                                           DataFetchingEnvironment environment) {
        return error(environment, ErrorType.FORBIDDEN, "Acesso negado.");
    }

    @GraphQlExceptionHandler(Exception.class)
    public GraphQLError handleUnexpected(Exception exception, DataFetchingEnvironment environment) {
        log.error("Erro inesperado em resolver GraphQL de usuario-autenticacao", exception);
        return error(environment, ErrorType.INTERNAL_ERROR, "Erro interno ao processar a requisicao.");
    }

    private GraphQLError error(DataFetchingEnvironment environment, ErrorType errorType, String message) {
        GraphqlErrorBuilder<?> builder = environment != null
            ? GraphqlErrorBuilder.newError(environment)
            : GraphqlErrorBuilder.newError();
        return builder
            .errorType(errorType)
            .message(message)
            .build();
    }
}
