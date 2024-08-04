package rutrob.task.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import rutrob.task.models.ResponseRepo;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RepoControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    private static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("url", wireMockServer::baseUrl);
    }

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort());
        wireMockServer.start();

        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    void shouldReturnNonForkedRepos() {
        stubFor(WireMock.get("/users/test-user/repos")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                      {
                                          "name": "test-repository",
                                          "owner": {
                                              "login": "test-user"
                                          },
                                          "fork": false
                                      },
                                      {
                                          "name": "forked-repo",
                                          "owner": {
                                              "login": "test-user"
                                          },
                                          "fork": true
                                      }
                                ]
                                """)
                        .withStatus(HttpStatus.OK.value())));

        stubFor(WireMock.get("/repos/test-user/test-repository/branches")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                    {
                                      "name": "main",
                                      "commit": {
                                          "sha": "main-sha"
                                      }
                                    }
                                ]
                                """)
                        .withStatus(HttpStatus.OK.value())));

        stubFor(WireMock.get("/repos/test-user/forked-repo/branches")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                    {
                                      "name": "main",
                                      "commit": {
                                          "sha": "main-sha-forked"
                                      }
                                    }
                                ]
                                """)
                        .withStatus(HttpStatus.OK.value())));

        webTestClient.get().uri("/api/repos/test-user")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseRepo.class)
                .hasSize(1)
                .value(responseRepos -> {
                    var responseRepo1 = responseRepos.get(0);
                    assertEquals("test-repository", responseRepo1.name());
                    assertEquals("test-user", responseRepo1.owner());
                    assertEquals(1, responseRepo1.branches().size());

                    var branch1 = responseRepo1.branches().get(0);
                    assertEquals("main", branch1.name());
                    assertEquals("main-sha", branch1.commit().sha());
                });
    }

    @Test
    void shouldReturnEmptyReposList() {
        stubFor(WireMock.get("/users/test-user/repos")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")
                        .withStatus(HttpStatus.OK.value())));

        webTestClient.get().uri("/api/repos/test-user")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseRepo.class)
                .hasSize(0);
    }

    @Test
    void shouldReturnRepoWithNoBranches() {
        stubFor(WireMock.get("/users/test-user/repos")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                [
                                      {
                                          "name": "test-repository",
                                          "owner": {
                                              "login": "test-user"
                                          },
                                          "fork": false
                                      }
                                ]
                                """)
                        .withStatus(HttpStatus.OK.value())));

        stubFor(WireMock.get("/repos/test-user/test-repository/branches")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")
                        .withStatus(HttpStatus.OK.value())));

        webTestClient.get().uri("/api/repos/test-user")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ResponseRepo.class)
                .hasSize(1)
                .value(responseRepos -> {
                    var responseRepo1 = responseRepos.get(0);
                    assertEquals("test-repository", responseRepo1.name());
                    assertEquals("test-user", responseRepo1.owner());
                    assertEquals(0, responseRepo1.branches().size());
                });
    }

    @Test
    void shouldReturnUserNotFound() {
        stubFor(WireMock.get("/users/test-user/repos")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.NOT_FOUND.value())));

        webTestClient.get().uri("/api/repos/test-user")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.message").isEqualTo("User not found");
    }


}
