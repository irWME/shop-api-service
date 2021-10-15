package nl.simpliphi.shopapiservice.config;

import io.github.alikelleci.easysourcing.GatewayBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class EasySourcingConfig {

    @Bean
    public GatewayBuilder gatewayBuilder(@Value("${easysourcing.bootstrap-servers}") String servers) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);

        return new GatewayBuilder(properties);
    }
}
