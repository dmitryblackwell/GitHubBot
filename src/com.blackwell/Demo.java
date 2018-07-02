package com.blackwell;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.io.IOException;

public class Demo {
    public static void main(String[] args) {
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token("5a29bb2abf1ff93ef308");
        try {
            GitHubRequest req = new GitHubRequest();
            req.setUri("https://api.github.com/repos/dmitryblackwell/EpamPractice/commits");
            GitHubResponse res = client.get(req);
            System.out.println(res.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }


//        Git git = new Git("dmitryblackwell");
//        git.update();
//        System.out.println(git);
//
//        for(String s : git.getNotTrackedCommitsMessages())
//            System.out.println(s);

    }
}
