package com.yechan.server;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GithubTool {

    private static final Logger log = LoggerFactory.getLogger(GithubTool.class);
    public static final int SIZE = 30;

    @Tool(name = "search_in_github", description = "Search for appropriate results in GitHub")
    public String search(@ToolParam(description = "검색할 키워드") SearchRequest request) {
        String keyword = request.keyword();
        log.info("GithubTool.search() called with keyword: {}", keyword);
        
        GitHub gitHub = null;
        try {
            log.debug("Connecting to GitHub anonymously");
            gitHub = GitHub.connectAnonymously();
        } catch (IOException e) {
            log.error("Error connecting to GitHub", e);
            throw new RuntimeException(e);
        }
        
        log.debug("Searching GitHub repositories with keyword: {}", keyword);
        PagedSearchIterable<GHRepository> repositories = gitHub.searchRepositories().q(keyword).list();

        StringBuilder result = new StringBuilder();
        int count = 0;
        for (GHRepository repository : repositories.withPageSize(SIZE)) {
            count++;
            log.debug("Processing repository: {}", repository.getFullName());
            log.debug("getLanguage: {}",repository.getLanguage());

            result.append("Repo: ").append(repository.getFullName())
                    .append(", Stars: ").append(repository.getStargazersCount())
                    .append(", Description: ").append(repository.getDescription())
                    .append(", URL: ").append(repository.getHtmlUrl())
                    .append(", Owner: ").append(repository.getOwnerName())
                    .append("\n");
        }
        
        log.info("Found {} GitHub repositories for keyword: {}", count, keyword);
        return result.toString();
    }
    public record SearchRequest(String keyword) {}
}
