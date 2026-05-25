package br.com.fiaprestaurante.pagamento.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Documentação OpenAPI do endpoint GraphQL do contexto de pagamento.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pagamentoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIAP Restaurante - Pagamento")
                        .version("1.0.0")
                        .description("API GraphQL de consulta do pagamento processado de modo assíncrono via Kafka."))
                .servers(List.of(new Server()
                        .url("http://localhost:8083")
                        .description("Ambiente local via Docker Compose")))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerSecurityScheme()))
                .path("/graphql", new PathItem().post(graphqlOperation()));
    }

    private Operation graphqlOperation() {
        String fields = "id pedidoId valor status tentativas motivoFalha createdAt updatedAt";
        MediaType requestMediaType = new MediaType().schema(graphqlRequestSchema())
                .addExamples("PagamentoPorPedido", example(
                        "Pagamento por pedido",
                        "Consulta o pagamento criado após o evento pedido.criado.",
                        "query PagamentoPorPedido($pedidoId: ID!) { pagamentoPorPedido(pedidoId: $pedidoId) { " + fields + " } }",
                        Map.of("pedidoId", "00000000-0000-0000-0000-000000000100")))
                .addExamples("PagamentosPendentes", example(
                        "Listar pendentes",
                        "Lista pagamentos aguardando reprocessamento automático.",
                        "query PagamentosPendentes { pagamentosPendentes { " + fields + " } }",
                        Map.of()));

        return new Operation()
                .tags(List.of("GraphQL"))
                .summary("Executar operação GraphQL de pagamentos")
                .description("As consultas exigem Bearer JWT. O pagamento é criado após a confirmação do pedido ser processada pelo Kafka. "
                        + "Falhas tratadas retornam errors[].extensions.classification e o status HTTP correspondente.")
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .requestBody(new RequestBody()
                        .required(true)
                        .content(new Content().addMediaType("application/json", requestMediaType)))
                .responses(graphqlResponses());
    }

    private ObjectSchema graphqlRequestSchema() {
        ObjectSchema request = new ObjectSchema();
        request.addProperty("query", new StringSchema().description("Documento GraphQL a ser executado."));
        request.addProperty("variables", new ObjectSchema().description("Variáveis utilizadas na operação."));
        request.addProperty("operationName", new StringSchema().description("Nome opcional da operação."));
        request.addRequiredItem("query");
        return request;
    }

    private ObjectSchema graphqlResponseSchema() {
        ObjectSchema response = new ObjectSchema();
        response.addProperty("data", new ObjectSchema().description("Dados retornados pela operação quando bem-sucedida."));
        response.addProperty("errors", new ArraySchema()
                .items(graphqlErrorSchema())
                .description("Erros GraphQL classificados semanticamente, acompanhados do status HTTP correspondente."));
        return response;
    }

    private ApiResponses graphqlResponses() {
        return new ApiResponses().addApiResponse("200", responseWithExample(
                        "OK - execução GraphQL concluída com sucesso.",
                        "Sucesso",
                        successExample()))
                .addApiResponse("400", responseWithExample(
                        "Bad Request - entrada, corpo JSON ou documento GraphQL inválido.",
                        "ErroBadRequest",
                        errorExample(
                        "BAD_REQUEST - pedidoId inválido",
                        "Argumento invalido: Invalid UUID string: id-invalido",
                        "BAD_REQUEST",
                        "pagamentoPorPedido")))
                .addApiResponse("401", new ApiResponse()
                        .description("Unauthorized - token Bearer enviado é inválido ou expirou e foi rejeitado pelo filtro HTTP."))
                .addApiResponse("403", responseWithExample(
                        "Forbidden - usuário autenticado sem permissão para a operação.",
                        "ErroForbidden",
                        errorExample(
                        "FORBIDDEN - acesso não autorizado",
                        "Acesso negado.",
                        "FORBIDDEN",
                        "pagamentosPendentes")))
                .addApiResponse("404", responseWithExample(
                        "Not Found - pagamento solicitado não foi encontrado.",
                        "ErroNotFound",
                        errorExample(
                        "NOT_FOUND - pagamento inexistente",
                        "Pagamento não encontrado para o pedido informado.",
                        "NOT_FOUND",
                        "pagamentoPorPedido")))
                .addApiResponse("500", responseWithExample(
                        "Internal Server Error - erro inesperado durante a execução.",
                        "ErroInterno",
                        errorExample(
                        "INTERNAL_ERROR - falha inesperada",
                        "Erro interno ao processar a requisicao.",
                        "INTERNAL_ERROR",
                        "pagamentoPorPedido")));
    }

    private ApiResponse responseWithExample(String description, String exampleName, Example example) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType()
                        .schema(graphqlResponseSchema())
                        .addExamples(exampleName, example)));
    }

    private ObjectSchema graphqlErrorSchema() {
        ObjectSchema error = new ObjectSchema();
        error.addProperty("message", new StringSchema().description("Mensagem da falha."));
        error.addProperty("path", new ArraySchema().items(new StringSchema()).description("Campo GraphQL que falhou."));
        error.addProperty("extensions", new ObjectSchema()
                .addProperty("classification", new StringSchema()
                        .description("Código semântico: BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND ou INTERNAL_ERROR.")));
        return error;
    }

    private Example successExample() {
        return new Example()
                .summary("200 OK - pagamento aprovado")
                .value(Map.of("data", Map.of("pagamentoPorPedido", Map.of(
                        "id", "10000000-0000-0000-0000-000000000001",
                        "pedidoId", "00000000-0000-0000-0000-000000000100",
                        "valor", 42.90,
                        "status", "APROVADO",
                        "tentativas", 1))));
    }

    private Example errorExample(String summary, String message, String classification, String path) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put(path, null);
        return new Example()
                .summary(summary)
                .description("A resposta conserva o corpo GraphQL e usa status HTTP compatível com a classificação.")
                .value(Map.of(
                        "errors", List.of(Map.of(
                                "message", message,
                                "path", List.of(path),
                                "extensions", Map.of("classification", classification))),
                        "data", data));
    }

    private Example example(String summary, String description, String query, Map<String, Object> variables) {
        return new Example()
                .summary(summary)
                .description(description)
                .value(Map.of("query", query, "variables", variables));
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Token JWT RS256 obtido no serviço usuario-autenticacao.");
    }
}
