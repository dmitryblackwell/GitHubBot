package com.blackwell;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Git {
    private Map<String, Integer> repos = new HashMap<>();
    private String username;

    public Git(String username) {
        this.username = username;
    }

    public void update(){
        RepositoryService service = new RepositoryService();
        CommitService commitService = new CommitService();

        try {
            for(Repository repo : service.getRepositories(username))
                repos.put(repo.getName(), commitService.getCommits(repo).size());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, Integer> entry : repos.entrySet())
            sb.append(entry.getKey()).append(": ").append(entry.getValue())
                    .append(System.lineSeparator());
        return sb.toString();
    }

    public List<String> getNotTrackedCommitsMessages(){
        List<String> commitsMessages = new ArrayList<>();
        RepositoryService service = new RepositoryService();

        CommitService commitService = new CommitService();

        try {
            for(Repository repo : service.getRepositories(username)){
                String key = repo.getName();
                Integer value = repos.get(key);
                if (value == null)
                    value = 0;
                RepositoryId repoId = new RepositoryId(username, key);
                List<RepositoryCommit> commits= commitService.getCommits(repoId);
                int difference = commits.size() - value;
                for(int i=0; i<difference; ++i)
                    commitsMessages.add(key+commits.get(i).getCommit().getMessage());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return commitsMessages;
    }
}
