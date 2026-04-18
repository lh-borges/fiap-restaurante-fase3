package br.com.fiaprestaurante.modules.usuarioautenticacao;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UsuarioAutenticacaoGraphQLApiTest {

    private static final String EMAIL_TESTE = "ana@fiap.com";
    private static final String SENHA_TESTE = "123456";

    @Test
    void deveCadastrarAutenticarEConsultarUsuarioAtual() {
        String cadastro = """
            {
              "query": "mutation { cadastrarUsuario(input: { nome: \\"Ana\\", email: \\"%s\\", senha: \\"%s\\", perfil: CLIENTE }) { id nome email perfil } }"
            }
            """.formatted(EMAIL_TESTE, SENHA_TESTE);

        given()
            .contentType(ContentType.JSON)
            .body(cadastro)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.cadastrarUsuario.id", notNullValue())
            .body("data.cadastrarUsuario.nome", equalTo("Ana"))
            .body("data.cadastrarUsuario.email", equalTo(EMAIL_TESTE))
            .body("data.cadastrarUsuario.perfil", equalTo("CLIENTE"));

        String token = obterToken(EMAIL_TESTE, SENHA_TESTE);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("""
                {
                  "query": "{ me { nome email perfil } }"
                }
                """)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.me.nome", equalTo("Ana"))
            .body("data.me.email", equalTo(EMAIL_TESTE))
            .body("data.me.perfil", equalTo("CLIENTE"));
    }

    @Test
    void devePermitirAcessoAoModuloRestaurantePedidoComTokenValido() {
        // Garante que o usuario existe (idempotente: ignora duplicatas)
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "query": "mutation { cadastrarUsuario(input: { nome: \\"Bob\\", email: \\"bob@fiap.com\\", senha: \\"abc123\\", perfil: CLIENTE }) { id } }"
                }
                """)
        .when()
            .post("/graphql");

        String token = obterToken("bob@fiap.com", "abc123");

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("""
                {
                  "query": "{ statusModuloRestaurantePedido { nome implementado descricao } }"
                }
                """)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.statusModuloRestaurantePedido.nome", equalTo("restaurante-pedido"))
            .body("data.statusModuloRestaurantePedido.implementado", equalTo(false))
            .body("data.statusModuloRestaurantePedido.descricao", notNullValue());
    }

    @Test
    void deveNegarAcessoAoModuloRestaurantePedidoSemAutenticacao() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "query": "{ statusModuloRestaurantePedido { nome } }"
                }
                """)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("errors", notNullValue())
            .body("data.statusModuloRestaurantePedido", nullValue());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String obterToken(String email, String senha) {
        return given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "query": "mutation { login(input: { email: \\"%s\\", senha: \\"%s\\" }) { token tipoToken expiraEmSegundos usuario { id email } } }"
                }
                """.formatted(email, senha))
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.login.tipoToken", equalTo("Bearer"))
            .body("data.login.usuario.email", equalTo(email))
            .extract()
            .path("data.login.token");
    }
}

