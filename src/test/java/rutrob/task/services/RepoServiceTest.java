package rutrob.task.services;


import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import rutrob.task.exceptions.UserNotFoundException;
import rutrob.task.models.Branch;
import rutrob.task.models.Commit;
import rutrob.task.models.ResponseRepo;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true", "spring.profiles.active=test"})
@WireMockTest(httpPort = 8081)
class RepoServiceTest {

    @Autowired
    private RepoService repoService;

    @Test
    public void testGetReposInfo() {
        var branch = new Branch("main", new Commit("test-sha"));

        stubFor(get(urlEqualTo("/users/user1/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                      "name": "repo1",
                                      "owner": {
                                          "login": "user1"
                                      },
                                      "fork": false
                                  }
                                ]
                                """)
                        .withStatus(HttpStatus.OK.value())));

        stubFor(get(urlEqualTo("/repos/user1/repo1/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                      "name": "main",
                                      "commit": {
                                          "sha": "test-sha"
                                      }
                                  }
                                ]
                                """)
                        .withStatus(HttpStatus.OK.value())));

        Flux<ResponseRepo> responseRepos = repoService.getReposInfo("user1");
        StepVerifier.create(responseRepos)
                .expectNext(new ResponseRepo("repo1", "user1", List.of(branch)))
                .verifyComplete();
    }

    @Test
    void testGetReposInfoNotFound() {
        stubFor(get(urlEqualTo("/users/user1/repos"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        Flux<ResponseRepo> responseRepos = repoService.getReposInfo("user1");
        StepVerifier.create(responseRepos)
                .expectError(UserNotFoundException.class)
                .verify();
    }
}