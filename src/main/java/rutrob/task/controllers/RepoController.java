package rutrob.task.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import rutrob.task.models.ResponseRepo;
import rutrob.task.services.RepoService;

@RestController
@RequestMapping("/api/repos")
public class RepoController {

    private final RepoService repoService;

    public RepoController(RepoService repoService) {
        this.repoService = repoService;
    }

    @GetMapping("/{username}")
    public Flux<ResponseRepo> getRepos(@PathVariable("username") String username) {
        return repoService.getReposInfo(username);
    }
}
