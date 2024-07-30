package rutrob.task.models;

import java.util.List;

public record ResponseRepo(String name, String owner, List<Branch> branches) {
}
