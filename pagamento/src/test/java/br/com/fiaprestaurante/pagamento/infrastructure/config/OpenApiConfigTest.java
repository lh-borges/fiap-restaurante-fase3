package br.com.fiaprestaurante.pagamento.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void deveDocumentarEndpointGraphqlProtegidoComExemplos() {
        OpenAPI openApi = new OpenApiConfig().pagamentoOpenApi();
        var operation = openApi.getPaths().get("/graphql").getPost();

        assertThat(openApi.getInfo().getTitle()).contains("Pagamento");
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(operation.getSecurity()).isNotEmpty();
        assertThat(operation.getRequestBody().getContent().get("application/json").getExamples())
                .containsKeys("PagamentoPorPedido", "PagamentosPendentes");
        assertThat(operation.getResponses()).containsKeys("200", "400", "401", "403", "404", "500");
        assertThat(operation.getResponses().get("200").getContent().get("application/json").getExamples())
                .containsKey("Sucesso");
        assertThat(operation.getResponses().get("400").getContent().get("application/json").getExamples())
                .containsKey("ErroBadRequest");
        assertThat(operation.getResponses().get("403").getContent().get("application/json").getExamples())
                .containsKey("ErroForbidden");
        assertThat(operation.getResponses().get("404").getContent().get("application/json").getExamples())
                .containsKey("ErroNotFound");
        assertThat(operation.getResponses().get("500").getContent().get("application/json").getExamples())
                .containsKey("ErroInterno");
    }
}
