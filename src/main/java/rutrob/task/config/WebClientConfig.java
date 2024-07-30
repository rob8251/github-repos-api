package rutrob.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;
import rutrob.task.services.RepoService;

@Configuration
@Profile("github")
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().baseUrl("https://api.github.com");
    }

    @Bean
    public RepoService repoService(WebClient.Builder webClientBuilder) {
        return new RepoService(webClientBuilder);
    }
}
