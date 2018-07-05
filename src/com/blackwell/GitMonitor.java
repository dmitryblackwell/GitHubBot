package com.blackwell;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Monitoring all git activity ones per hour
 */
public class GitMonitor extends Thread {

    private String gitLogin;
    private Twitter twitter;
    private JSONParser parser = new JSONParser();
    private Map<String, Integer> repos = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(GitMonitor.class);

    // strings to make url requests
    private static final String API_GIT = "https://api.github.com/";
    private static final String USERS = "users";
    private static final String REPOS = "repos";
    private static final String COMMIT = "commit";
    private static final String COMMITS = "commits";
    private static final String COMMIT_TO = "Commit to ";

    // constructor with git username and keys to twitter
    GitMonitor(String gitLogin, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        this.gitLogin = gitLogin;
        ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);

        TwitterFactory factory = new TwitterFactory(builder.build());
        twitter = factory.getInstance();

        LOG.debug("GitMonitor initialized;");
    }

    /**
     * updating data in the map with repos
     */
    public void update(){
        LOG.debug("starting update;");
        try {
            checkForRepos();
            getNotTrackedCommits();
        } catch (IOException ex) {
            LOG.error("Exception while reading from https: ", ex);
        } catch (ParseException ex) {
            LOG.error("Exception from parsing json", ex);
        }
        LOG.debug("finish update;");
    }

    private void sendTweet(String tweet){
        try {
            LOG.debug("sending tweet;");
            twitter.updateStatus(tweet);
            LOG.debug("tweet: \""+ tweet +"\" send");
        } catch (TwitterException e) {
            LOG.error("exception while sending tweet", e);
        }
    }

    /**
     * checking repositories
     * @throws IOException reading from https
     * @throws ParseException parsing to json
     */
    private synchronized void checkForRepos() throws IOException, ParseException {
        LOG.debug("start checking for new repositories");
        int reposBefore = repos.size();

        URL url = new URL(API_GIT + USERS +"/"+ gitLogin +"/"+ REPOS);

        JSONArray reposArray = getJsonFileFromURL(url);

        if (reposArray.size() != repos.size()) {
            for (Object repo : reposArray) {
                JSONObject repObj = (JSONObject) repo;
                String repoName = repObj.get("name").toString();
                if(!repos.containsKey(repoName))
                    repos.put(repoName, 0);
            }
        }

        LOG.debug("finish checking for repos, new repos adding " + (repos.size() - reposBefore) +";");
    }

    /**
     * searching for new commits
     * Download all commits and compare them to commits, that already downloaded
     * @return list of untracked commits
     * @throws IOException exception of reading from https
     * @throws ParseException parsing to json error
     */
    private synchronized List<String> getNotTrackedCommits() throws IOException, ParseException {
        LOG.debug("start to searching for not tracked commits");
        List<String> notTrackedCommits = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : repos.entrySet()){
            URL url = new URL(API_GIT + REPOS +"/"+ gitLogin +"/"+ entry.getKey() +"/"+ COMMITS);
            JSONArray jsonArray = getJsonFileFromURL(url);
            int difference = jsonArray.size() - entry.getValue();
            for(int i=0; i<difference; ++i){
                JSONObject commit = (JSONObject) jsonArray.get(i);
                JSONObject commitData = (JSONObject) commit.get(COMMIT);
                String commitMessage = (String) commitData.get("message");

                String message = COMMIT_TO +"#"+ entry.getKey() + "\n\n" +
                        commitMessage;
                notTrackedCommits.add(message);
            }
            entry.setValue(entry.getValue()+difference);
        }
        LOG.debug("finish checking for not tracked commits, total size " + notTrackedCommits.size() +";");
        return notTrackedCommits;
    }

    /**
     *
     * @param url where download json
     * @return JSONArray that downloaded
     */
    private JSONArray getJsonFileFromURL(URL url) throws IOException, ParseException {
        LOG.debug("getting json from url: " + url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line;
        while ( (line = in.readLine()) != null)
            sb.append(line);

        LOG.debug("finish getting json and returning JSONArray");
        return  (JSONArray) parser.parse(sb.toString());
    }

    /**
     * send all commits to twitter
     */
    public void sendAll(){
        LOG.debug("sending all commits to twitter");
        try {
            repos.clear();
            checkForRepos();
            List<String> notTrackedCommits = getNotTrackedCommits();
            for(String message : notTrackedCommits)
                sendTweet(message);
            LOG.debug(notTrackedCommits.size() +" commits was send");
        } catch (IOException | ParseException e) {
            LOG.error("error while sending all commits", e);
        }
    }

    @Override
    public void run() {
        LOG.debug("starting main loop");
        while (true){
            try {
                //checkForRepos();
                List<String> notTrackedCommits = getNotTrackedCommits();
                for(String message : notTrackedCommits)
                    sendTweet(message);

                LOG.debug("start waiting for one hour");
                //break;
                Thread.sleep(60*60*1000);
                LOG.debug("stop waiting");

                if (interrupted()) {
                    LOG.debug("main loop was interrupted");
                    return;
                }

            } catch (Exception e) {
                LOG.error("exception in main loop", e);
            }
        }
    }
}
