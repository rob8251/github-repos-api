package rutrob.task.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rutrob.task.exceptions.UserNotFoundException;
import rutrob.task.models.Branch;
import rutrob.task.models.Repo;
import rutrob.task.models.ResponseRepo;

@Service
public class RepoService {

    private final WebClient webClient;

    public RepoService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<ResponseRepo> getReposInfo(String username) {
        return getNonForkedRepos(username)
                .flatMap(repo -> getBranches(username, repo.name())
                        .collectList()
                        .map(branches -> new ResponseRepo(repo.name(), repo.owner().login(), branches)));
    }

    private Flux<Repo> getNonForkedRepos(String username) {
        return webClient.get().uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new UserNotFoundException("User not found")))
                .bodyToFlux(Repo.class)
                .filter(repo -> !repo.fork());
    }

    private Flux<Branch> getBranches(String username, String repo) {
        return webClient.get().uri("/repos/{username}/{repo}/branches", username, repo)
                .retrieve()
                .bodyToFlux(Branch.class);
    }
}
