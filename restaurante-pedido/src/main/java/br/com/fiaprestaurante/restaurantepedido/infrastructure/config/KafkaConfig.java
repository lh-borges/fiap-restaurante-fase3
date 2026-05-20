package br.com.fiaprestaurante.restaurantepedido.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração explícita do Kafka — producer, consumer e listener container.
 *
 * <p>Spring Boot 4.0.5 <strong>não traz autoconfigure de Kafka</strong>
 * (o módulo {@code spring-boot-autoconfigure} não inclui mais
 * {@code KafkaAutoConfiguration}), então é necessário declarar os beans
 * manualmente.
 *
 * <p>O {@link ObjectMapper} usado pelos {@code JsonSerializer}/
 * {@code JsonDeserializer} é customizado para:
 * <ul>
 *   <li>registrar {@link JavaTimeModule} (Spring Kafka 4.0.4 ainda usa
 *       Jackson 2 e {@code Instant} precisa de suporte explícito);</li>
 *   <li>serializar datas em ISO-8601 (não como timestamp numérico);</li>
 *   <li>tolerar propriedades desconhecidas na desserialização (evita
 *       falhas quando o produtor evolui o payload).</li>
 * </ul>
 *
 * @author Danilo Fernando
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    private final String bootstrapServers;
    private final String consumerGroupId;

    /**
     * @param bootstrapServers lista de brokers Kafka (vem de {@code spring.kafka.bootstrap-servers})
     * @param consumerGroupId  group id do consumer (vem de {@code spring.kafka.consumer.group-id})
     */
    public KafkaConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                       @Value("${spring.kafka.consumer.group-id}") String consumerGroupId) {
        this.bootstrapServers = bootstrapServers;
        this.consumerGroupId = consumerGroupId;
    }

    /**
     * Cria um {@link ObjectMapper} configurado com {@link JavaTimeModule}.
     *
     * @return ObjectMapper pronto para uso pelos serializers Kafka
     */
    private ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /**
     * Factory de producers Kafka — usa {@code String} para chaves e
     * {@code JsonSerializer} (com nosso {@code ObjectMapper}) para valores.
     *
     * @return factory configurada
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(new JsonSerializer<>(kafkaObjectMapper()));
        return factory;
    }

    /**
     * {@link KafkaTemplate} principal — usado pelo {@code PedidoKafkaPublisher}.
     *
     * @param producerFactory factory criada por {@link #producerFactory()}
     * @return template Kafka pronto para uso
     */
    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Factory de consumers Kafka — chaves como {@code String}, valores
     * desserializados em {@code HashMap} via {@code JsonDeserializer}
     * (sem type info nos headers, para aceitar payloads de qualquer produtor).
     *
     * @return factory configurada
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        JsonDeserializer<Object> valueDeserializer = new JsonDeserializer<>(Object.class, kafkaObjectMapper(), false);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    /**
     * Factory de listener containers — usada pelo {@code @KafkaListener}
     * nos consumers de pagamento.
     *
     * @param consumerFactory factory de consumers criada por {@link #consumerFactory()}
     * @return factory de containers para listeners concorrentes
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
