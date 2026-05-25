package br.com.fiaprestaurante.pagamento.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.server.WebGraphQlHandler;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.graphql.server.webmvc.GraphQlHttpHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Comparator;
import java.util.Objects;

/**
 * Faz o transporte HTTP refletir as classificacoes de erro do endpoint GraphQL.
 */
@Configuration
public class GraphQlHttpStatusConfig {

    @Bean
    public GraphQlHttpHandler graphQlHttpHandler(WebGraphQlHandler webGraphQlHandler) {
        return new StatusAwareGraphQlHttpHandler(webGraphQlHandler);
    }

    private static final class StatusAwareGraphQlHttpHandler extends GraphQlHttpHandler {

        private StatusAwareGraphQlHttpHandler(WebGraphQlHandler webGraphQlHandler) {
            super(webGraphQlHandler);
        }

        @Override
        protected HttpStatus selectResponseStatus(WebGraphQlResponse response, MediaType responseMediaType) {
            HttpStatus frameworkStatus = super.selectResponseStatus(response, responseMediaType);
            if (frameworkStatus != HttpStatus.OK) {
                return frameworkStatus;
            }

            return response.getErrors().stream()
                    .map(GraphQlHttpStatusConfig::statusFor)
                    .filter(Objects::nonNull)
                    .max(Comparator.comparingInt(HttpStatus::value))
                    .orElseGet(() -> response.getErrors().isEmpty() || response.getExecutionResult().isDataPresent()
                            ? HttpStatus.OK
                            : HttpStatus.BAD_REQUEST);
        }
    }

    private static HttpStatus statusFor(ResponseError error) {
        if (!(error.getErrorType() instanceof ErrorType errorType)) {
            return null;
        }
        return switch (errorType) {
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
