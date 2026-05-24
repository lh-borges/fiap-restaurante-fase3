package br.com.fiaprestaurante.pagamento.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class BeanConfigTest {

    @Test
    void deveCriarRestTemplateComRequestFactorySimples() {
        RestTemplate restTemplate = new BeanConfig().restTemplate(3000, 5000);

        assertThat(restTemplate.getRequestFactory()).isInstanceOf(SimpleClientHttpRequestFactory.class);
    }
}
