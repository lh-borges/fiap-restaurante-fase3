package br.com.fiaprestaurante.usuarioautenticacao.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.examples.Example;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Descreve o contrato HTTP do endpoint GraphQL no Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usuarioAutenticacaoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FIAP Restaurante - Usuário e Autenticação")
                        .version("1.0.0")
                        .description("API GraphQL responsável pelo cadastro, autenticação e consulta do usuário autenticado."))
                .servers(List.of(new Server()
                        .url("http://localhost:8081")
                        .description("Ambiente local via Docker Compose")))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerSecurityScheme()))
                .path("/graphql", new PathItem().post(graphqlOperation()));
    }

    private Operation graphqlOperation() {
        MediaType requestMediaType = new MediaType().schema(graphqlRequestSchema())
                .addExamples("CadastrarUsuario", example(
                        "Cadastrar usuário",
                        "Mutation pública para cadastrar um novo cliente.",
                        "mutation CadastrarUsuario($input: CadastrarUsuarioInput!) { cadastrarUsuario(input: $input) { id nome email perfil criadoEm } }",
                        Map.of("input", Map.of(
                                "nome", "Cliente FIAP",
                                "email", "cliente@fiap.com",
                                "senha", "senha123",
                                "perfil", "USUARIO"))))
                .addExamples("Login", example(
                        "Login",
                        "Mutation pública que devolve o token JWT.",
                        "mutation Login($input: LoginInput!) { login(input: $input) { token tipoToken expiraEmSegundos usuario { id nome email perfil } } }",
                        Map.of("input", Map.of("email", "usuario@fiap.com", "senha", "usuario123"))))
                .addExamples("Me", example(
                        "Consultar usuário autenticado",
                        "Query protegida: informe o token no botão Authorize.",
                        "query Me { me { id nome email perfil criadoEm } }",
                        Map.of()));

        return new Operation()
                .tags(List.of("GraphQL"))
                .summary("Executar operação GraphQL de autenticação")
                .description("Use os exemplos para cadastro e login. A query me exige Bearer JWT; após realizar login, copie o token para Authorize. "
                        + "Falhas tratadas retornam errors[].extensions.classification e o status HTTP correspondente.")
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
                        "BAD_REQUEST - entrada inválida",
                        "Já existe um usuário cadastrado com o e-mail: usuario@fiap.com",
                        "BAD_REQUEST",
                        "cadastrarUsuario")))
                .addApiResponse("401", responseWithExample(
                        "Unauthorized - credenciais inválidas ou token Bearer rejeitado.",
                        "ErroUnauthorized",
                        errorExample(
                        "UNAUTHORIZED - credenciais inválidas",
                        "Credenciais inválidas.",
                        "UNAUTHORIZED",
                        "login")))
                .addApiResponse("403", responseWithExample(
                        "Forbidden - autenticação ou permissão requerida para a operação.",
                        "ErroForbidden",
                        errorExample(
                                "FORBIDDEN - acesso não autorizado",
                                "Acesso negado.",
                                "FORBIDDEN",
                                "me")))
                .addApiResponse("404", responseWithExample(
                        "Not Found - usuário consultado não foi encontrado.",
                        "ErroNotFound",
                        errorExample(
                        "NOT_FOUND - usuário não encontrado",
                        "Usuário não encontrado.",
                        "NOT_FOUND",
                        "me")))
                .addApiResponse("500", responseWithExample(
                        "Internal Server Error - erro inesperado durante a execução.",
                        "ErroInterno",
                        errorExample(
                        "INTERNAL_ERROR - falha inesperada",
                        "Erro interno ao processar a requisicao.",
                        "INTERNAL_ERROR",
                        "me")));
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
                .summary("200 OK - login realizado")
                .value(Map.of("data", Map.of("login", Map.of(
                        "token", "eyJhbGciOiJSUzI1NiJ9...",
                        "tipoToken", "Bearer",
                        "expiraEmSegundos", 3600))));
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
                .description("Token JWT RS256 obtido na mutation login.");
    }
}
