package br.com.fiaprestaurante.restaurantepedido.infrastructure.config;

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
 * Documentação OpenAPI do endpoint GraphQL do contexto de pedidos.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI restaurantePedidoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIAP Restaurante - Pedidos")
                        .version("1.0.0")
                        .description("API GraphQL para criar, confirmar e consultar pedidos do cliente autenticado."))
                .servers(List.of(new Server()
                        .url("http://localhost:8082")
                        .description("Ambiente local via Docker Compose")))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerSecurityScheme()))
                .path("/graphql", new PathItem().post(graphqlOperation()));
    }

    private Operation graphqlOperation() {
        String pedidoFields = "id clienteId restauranteId valorTotal status pagamentoId motivoPendencia createdAt updatedAt";
        MediaType requestMediaType = new MediaType().schema(graphqlRequestSchema())
                .addExamples("CriarPedido", example(
                        "Criar pedido",
                        "Cria pedido para o cliente identificado no JWT.",
                        "mutation CriarPedido($input: CriarPedidoInput!) { criarPedido(input: $input) { " + pedidoFields + " } }",
                        Map.of("input", Map.of(
                                "restauranteId", "00000000-0000-0000-0000-000000000001",
                                "itens", List.of(Map.of(
                                        "produtoId", "00000000-0000-0000-0000-000000000010",
                                        "nome", "Hamburguer Artesanal",
                                        "quantidade", 1,
                                        "preco", 29.90))))))
                .addExamples("ConfirmarPedido", example(
                        "Confirmar pedido",
                        "Confirma o pedido e publica pedido.criado no Kafka.",
                        "mutation ConfirmarPedido($pedidoId: ID!) { confirmarPedido(pedidoId: $pedidoId) { " + pedidoFields + " } }",
                        Map.of("pedidoId", "00000000-0000-0000-0000-000000000100")))
                .addExamples("PedidoPorId", example(
                        "Consultar pedido por ID",
                        "Consulta o status atual do pedido.",
                        "query PedidoPorId($pedidoId: ID!) { pedidoPorId(pedidoId: $pedidoId) { " + pedidoFields + " } }",
                        Map.of("pedidoId", "00000000-0000-0000-0000-000000000100")))
                .addExamples("MeusPedidos", example(
                        "Listar meus pedidos",
                        "Lista os pedidos pertencentes ao cliente autenticado.",
                        "query MeusPedidos { meusPedidos { " + pedidoFields + " } }",
                        Map.of()))
                .addExamples("StatusModulo", example(
                        "Status do módulo",
                        "Consulta o status funcional do serviço.",
                        "query StatusModulo { statusModuloRestaurantePedido { nome implementado descricao } }",
                        Map.of()));

        return new Operation()
                .tags(List.of("GraphQL"))
                .summary("Executar operação GraphQL de pedidos")
                .description("Todas as operações deste endpoint exigem Bearer JWT de um usuário autenticado. "
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
                        "pedidoPorId")))
                .addApiResponse("401", new ApiResponse()
                        .description("Unauthorized - token Bearer enviado é inválido ou expirou e foi rejeitado pelo filtro HTTP."))
                .addApiResponse("403", responseWithExample(
                        "Forbidden - usuário autenticado sem permissão para a operação.",
                        "ErroForbidden",
                        errorExample(
                        "FORBIDDEN - acesso não autorizado",
                        "Acesso negado.",
                        "FORBIDDEN",
                        "meusPedidos")))
                .addApiResponse("404", responseWithExample(
                        "Not Found - pedido solicitado não foi encontrado.",
                        "ErroNotFound",
                        errorExample(
                        "NOT_FOUND - pedido inexistente",
                        "Pedido não encontrado para o identificador informado.",
                        "NOT_FOUND",
                        "pedidoPorId")))
                .addApiResponse("500", responseWithExample(
                        "Internal Server Error - erro inesperado durante a execução.",
                        "ErroInterno",
                        errorExample(
                        "INTERNAL_ERROR - falha inesperada",
                        "Erro interno ao processar a requisicao.",
                        "INTERNAL_ERROR",
                        "pedidoPorId")));
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
                .summary("200 OK - pedido criado")
                .value(Map.of("data", Map.of("criarPedido", Map.of(
                        "id", "00000000-0000-0000-0000-000000000100",
                        "valorTotal", 42.90,
                        "status", "CRIADO"))));
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
