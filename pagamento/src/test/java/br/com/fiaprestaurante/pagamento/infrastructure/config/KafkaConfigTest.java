package br.com.fiaprestaurante.pagamento.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConfigTest {

    @Test
    void deveCriarBeansKafkaComPropriedadesBasicas() {
        KafkaConfig config = new KafkaConfig("localhost:9092", "pagamento-service");

        ProducerFactory<Object, Object> producerFactory = config.producerFactory();
        KafkaTemplate<Object, Object> kafkaTemplate = config.kafkaTemplate(producerFactory);
        ConsumerFactory<String, Object> consumerFactory = config.consumerFactory();
        ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory =
                config.kafkaListenerContainerFactory(consumerFactory);

        assertThat(producerFactory.getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        assertThat(consumerFactory.getConfigurationProperties())
                .containsEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                .containsEntry(ConsumerConfig.GROUP_ID_CONFIG, "pagamento-service");
        assertThat(kafkaTemplate).isNotNull();
        assertThat(containerFactory).isNotNull();
    }
}
