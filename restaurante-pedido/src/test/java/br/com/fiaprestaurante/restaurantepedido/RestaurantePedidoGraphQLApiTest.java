package br.com.fiaprestaurante.restaurantepedido;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RestaurantePedidoGraphQLApiTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    @WithMockUser(authorities = {"USUARIO"})
    void deveRetornarStatusComUsuarioAutenticado() throws Exception {
        mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"{statusModuloRestaurantePedido{nome implementado descricao}}\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.statusModuloRestaurantePedido.nome").value("restaurante-pedido"))
            .andExpect(jsonPath("$.data.statusModuloRestaurantePedido.implementado").value(false));
    }

    @Test
    void deveNegarAcessoSemAutenticacao() throws Exception {
        String result = mockMvc.perform(post("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"{statusModuloRestaurantePedido{nome}}\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("errors");
    }
}
