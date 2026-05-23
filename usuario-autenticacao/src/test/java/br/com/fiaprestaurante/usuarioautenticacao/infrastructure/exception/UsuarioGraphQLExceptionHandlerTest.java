package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.exception;

import br.com.fiaprestaurante.shared.exception.BusinessException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.CredenciaisInvalidasException;
import br.com.fiaprestaurante.usuarioautenticacao.domain.exception.UsuarioNaoEncontradoException;
import graphql.GraphQLError;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioGraphQLExceptionHandlerTest {

    private final UsuarioGraphQLExceptionHandler handler = new UsuarioGraphQLExceptionHandler();

    @Test
    void deveClassificarErrosGraphQlConhecidos() {
        GraphQLError notFound = handler.handleUsuarioNaoEncontrado(
            new UsuarioNaoEncontradoException("id"), null);
        GraphQLError unauthorized = handler.handleCredenciaisInvalidas(
            new CredenciaisInvalidasException(), null);
        GraphQLError badRequest = handler.handleBusinessException(
            new BusinessException("regra invalida"), null);
        GraphQLError invalidArgument = handler.handleIllegalArgument(
            new IllegalArgumentException("uuid invalido"), null);
        GraphQLError authentication = handler.handleAuthentication(
            new BadCredentialsException("token ausente"), null);
        GraphQLError forbidden = handler.handleAccessDenied(
            new AccessDeniedException("sem permissao"), null);
        GraphQLError unexpected = handler.handleUnexpected(
            new IllegalStateException("falha tecnica"), null);

        assertThat(notFound.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(notFound.getMessage()).isEqualTo("Usuário não encontrado: id");
        assertThat(unauthorized.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        assertThat(unauthorized.getMessage()).isEqualTo("Credenciais inválidas.");
        assertThat(badRequest.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(badRequest.getMessage()).isEqualTo("regra invalida");
        assertThat(invalidArgument.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        assertThat(invalidArgument.getMessage()).isEqualTo("Argumento invalido: uuid invalido");
        assertThat(authentication.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        assertThat(authentication.getMessage()).isEqualTo("Autenticacao obrigatoria.");
        assertThat(forbidden.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
        assertThat(forbidden.getMessage()).isEqualTo("Acesso negado.");
        assertThat(unexpected.getErrorType()).isEqualTo(ErrorType.INTERNAL_ERROR);
        assertThat(unexpected.getMessage()).isEqualTo("Erro interno ao processar a requisicao.");
    }
}
