package br.com.fiaprestaurante.usuarioautenticacao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UsuarioAutenticacaoGraphQLApiTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    private static final String EMAIL_TESTE = "ana.spring@fiap.com";
    private static final String SENHA_TESTE = "123456";

    @Test
    void deveCadastrarAutenticarEConsultarUsuarioAtual() throws Exception {
        // 1) cadastrar
        String cadastro = """
            {"query":"mutation{cadastrarUsuario(input:{nome:\\"Ana\\",email:\\"%s\\",senha:\\"%s\\",perfil:USUARIO}){id nome email perfil}}"}
            """.formatted(EMAIL_TESTE, SENHA_TESTE).strip();

        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.cadastrarUsuario.id").exists())
            .andExpect(jsonPath("$.data.cadastrarUsuario.nome").value("Ana"))
            .andExpect(jsonPath("$.data.cadastrarUsuario.perfil").value("USUARIO"));

        // 2) login → pega token
        String token = obterToken(EMAIL_TESTE, SENHA_TESTE);

        // 3) me com JWT válido
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("{\"query\":\"{me{nome email perfil}}\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.me.nome").value("Ana"))
            .andExpect(jsonPath("$.data.me.perfil").value("USUARIO"));
    }

    @Test
    void deveNegarAcessoSemAutenticacao() throws Exception {
        String result = mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"{me{nome email}}\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errors[0].extensions.classification").value("FORBIDDEN"))
            .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("errors");
    }

    @Test
    void deveTratarCredenciaisInvalidasComoUnauthorizedGraphQL() throws Exception {
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"query":"mutation{login(input:{email:\\"naoexiste@fiap.com\\",senha:\\"errada\\"}){token}}"}
                    """.strip()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errors[0].message").value("Credenciais inválidas."))
            .andExpect(jsonPath("$.errors[0].extensions.classification").value("UNAUTHORIZED"));
    }

    @Test
    void deveTratarSubjectJwtInvalidoComoBadRequestGraphQL() throws Exception {
        mockMvc.perform(post("/graphql")
                .with(jwt().jwt(token -> token.subject("subject-invalido")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"{me{id nome email}}\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].message").value("Argumento invalido: Invalid UUID string: subject-invalido"))
            .andExpect(jsonPath("$.errors[0].extensions.classification").value("BAD_REQUEST"));
    }

    @Test
    void deveTratarUsuarioNaoEncontradoComoNotFoundHttp() throws Exception {
        mockMvc.perform(post("/graphql")
                .with(jwt().jwt(token -> token.subject(UUID.randomUUID().toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"{me{id nome email}}\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errors[0].extensions.classification").value("NOT_FOUND"));
    }

    @Test
    void devePublicarContratoOpenApiDoEndpointGraphql() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.info.title").value("FIAP Restaurante - Usuário e Autenticação"))
            .andExpect(jsonPath("$.paths['/graphql'].post").exists())
            .andExpect(jsonPath("$.paths['/graphql'].post.requestBody.content['application/json'].examples.Login").exists())
            .andExpect(jsonPath("$.paths['/graphql'].post.responses['200'].content['application/json'].examples.Sucesso").exists())
            .andExpect(jsonPath("$.paths['/graphql'].post.responses['400'].content['application/json'].examples.ErroBadRequest").exists())
            .andExpect(jsonPath("$.paths['/graphql'].post.responses['401'].content['application/json'].examples.ErroUnauthorized").exists())
            .andExpect(jsonPath("$.paths['/graphql'].post.responses['403'].content['application/json'].examples.ErroForbidden").exists())
            .andExpect(jsonPath("$.paths['/graphql'].post.responses['404'].content['application/json'].examples.ErroNotFound").exists())
            .andExpect(jsonPath("$.paths['/graphql'].post.responses['500'].content['application/json'].examples.ErroInterno").exists())
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists());
    }

    // --- helpers ---

    private String obterToken(String email, String senha) throws Exception {
        String login = """
            {"query":"mutation{login(input:{email:\\"%s\\",senha:\\"%s\\"}){token}}"}
            """.formatted(email, senha).strip();

        String result = mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        return JsonPath.read(result, "$.data.login.token");
    }
}
