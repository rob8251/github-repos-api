package rutrob.task.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import rutrob.task.exceptions.UserNotFoundException;
import rutrob.task.models.Branch;
import rutrob.task.models.Commit;
import rutrob.task.models.ResponseRepo;
import rutrob.task.services.RepoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@WebFluxTest(properties = {"spring.main.allow-bean-definition-overriding=true", "spring.profiles.active=test"},
            controllers = RepoController.class)
class RepoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RepoService repoService;

    @Test
    void testGetRepos() {
        var branch = new Branch("main", new Commit("test-sha"));
        var responseRepo = new ResponseRepo("test-repository", "user1234", List.of(branch));

        when(repoService.getReposInfo("user1234"))
                .thenReturn(Flux.just(responseRepo));

        webTestClient.get().uri("/api/repos/user1234")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseRepo.class)
                .hasSize(1)
                .value(responseRepos -> {
                    var responseRepo1 = responseRepos.get(0);
                    assertEquals("test-repository", responseRepo1.name());
                    assertEquals("user1234", responseRepo1.owner());
                    assertEquals(1, responseRepo1.branches().size());

                    var branch1 = responseRepo1.branches().get(0);
                    assertEquals("main", branch1.name());
                    assertEquals("test-sha", branch1.commit().sha());
                });
    }

    @Test
    void testGetReposUserNotFound() {
        when(repoService.getReposInfo("user1234"))
                .thenReturn(Flux.error(new UserNotFoundException("User not found")));

        webTestClient.get().uri("/api/repos/user1234")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.message").isEqualTo("User not found");
    }
}