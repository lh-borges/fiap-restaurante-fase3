package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void deveDocumentarEndpointGraphqlComExemplosEJwt() {
        OpenAPI openApi = new OpenApiConfig().usuarioAutenticacaoOpenApi();

        assertThat(openApi.getInfo().getTitle()).contains("Autenticação");
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(openApi.getPaths()).containsKey("/graphql");
        var operation = openApi.getPaths().get("/graphql").getPost();
        assertThat(operation.getRequestBody().getContent().get("application/json").getExamples())
                .containsKeys("CadastrarUsuario", "Login", "Me");
        assertThat(operation.getResponses()).containsKeys("200", "400", "401", "403", "404", "500");
        assertThat(operation.getResponses().get("200").getContent().get("application/json").getExamples())
                .containsKey("Sucesso");
        assertThat(operation.getResponses().get("400").getContent().get("application/json").getExamples())
                .containsKey("ErroBadRequest");
        assertThat(operation.getResponses().get("401").getContent().get("application/json").getExamples())
                .containsKey("ErroUnauthorized");
        assertThat(operation.getResponses().get("403").getContent().get("application/json").getExamples())
                .containsKey("ErroForbidden");
        assertThat(operation.getResponses().get("404").getContent().get("application/json").getExamples())
                .containsKey("ErroNotFound");
        assertThat(operation.getResponses().get("500").getContent().get("application/json").getExamples())
                .containsKey("ErroInterno");
    }
}
