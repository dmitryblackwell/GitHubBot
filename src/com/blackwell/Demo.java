package com.blackwell;

import org.json.simple.parser.ParseException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Demo{

    private static final Logger LOG = Logger.getLogger(Demo.class);

    public static void main(String[] args) throws IOException, ParseException {
        LOG.debug("Start");
        Thread monitor = new GitMonitor("dmitryblackwell",
                TwitterKeys.ConsumerKey,
                TwitterKeys.ConsumerSecret,
                TwitterKeys.AccessToken,
                TwitterKeys.AccessTokenSecret);

        // updating all repos
        ((GitMonitor) monitor).update();

        // start monitoring git activity ones per hour
        monitor.start();

    }

    /**
     * deleting all tweets from your twitter
     * @param username username of the user
     */
    private static void allTweetDelete(String username){
        LOG.debug("start deleting all tweets;");
        ConfigurationBuilder builder = new ConfigurationBuilder();

        // put in TwitterKeys your tokens
        builder.setDebugEnabled(true)
                .setOAuthConsumerKey(TwitterKeys.ConsumerKey)
                .setOAuthConsumerSecret(TwitterKeys.ConsumerSecret)
                .setOAuthAccessToken(TwitterKeys.AccessToken)
                .setOAuthAccessTokenSecret(TwitterKeys.AccessTokenSecret);

        TwitterFactory factory = new TwitterFactory(builder.build());
        Twitter twitter = factory.getInstance();

        try {
            Paging paging = new Paging(1, 200);
            List<Status> statuses = twitter.getUserTimeline(username,  paging);
            statuses.size(); // !!! do not delete - without this nothing working
            LOG.debug("get timeline for user " + username +";");

            ArrayList<Long> statusesId = new ArrayList<>();
            for(Status s : statuses)
                statusesId.add(s.getId());
            LOG.debug("get all statuses id, total: " + statusesId.size() +";");

            for(int i=0; i<statusesId.size(); ++i) {
                twitter.destroyStatus(statusesId.get(i));
                LOG.debug("Tweets deleted: " + i + "/" + statusesId.size());
            }
        } catch (TwitterException e) {
            LOG.error("get exception when deleting tweet; ", e);
        }

        LOG.debug("finish deleting all tweets;");
    }

}
