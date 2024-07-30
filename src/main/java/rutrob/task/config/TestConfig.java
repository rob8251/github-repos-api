package rutrob.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;
import rutrob.task.services.RepoService;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().baseUrl("http://localhost:8081");
    }

    @Bean
    public RepoService repoService(WebClient.Builder webClientBuilder) {
        return new RepoService(webClientBuilder);
    }
}
