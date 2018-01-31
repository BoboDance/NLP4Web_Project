package milestone2.NLP4Web_Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.JSONException;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.ConfigurationBuilder;

public class nextTwitterCrawler {
	
	ConfigurationBuilder cb ;
	static String[] user;
	List<TwitterAccounts> users;
	String src;
	String path;
	String domain;
	int pageno;
	int numOfTweets;
	Twitter twitter;
	HashMap<String, HashMap<List<Status>,List<String> > > tweets; 
	int checkFrequ;
	int writeToConsFrequ;
	int maxCnt;
	long start;
	long end;
	long end2;
	static boolean debug;
	boolean write;
	
	public nextTwitterCrawler(List<TwitterAccounts> usersToCrawl, int numTweets) {
		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("Qt0lPWcSiDDpAl7pLHWwNwfVY")
		.setOAuthConsumerSecret("TBc8MPxVezWus9lcDpP3OYf6r3uyKs5qckCYXskDNYKYm4SCCF")
		.setOAuthAccessToken("952170518513373189-o7jeHc3uQRGiYuYBc49tosaUAQRrh2D")
		.setOAuthAccessTokenSecret("w4Q7ETZrmwRIK0dQoI4v5DkD3zpae2cmRMbYC6Y2LBYJl")
		.setJSONStoreEnabled(true)			//stores data in JSON format
		.setIncludeExtAltTextEnabled(false) //dont know
		.setIncludeEntitiesEnabled(false) 	//think whether or nor hashtags and other entitites are included
		.setTrimUserEnabled(true)			//supposed to cut metadata from user - dont know if necessary
		.setTweetModeExtended(true) 		//unshorten long tweets
		;
		
		checkFrequ = 10; 			//Check every x Requests, how many remain. Needed to avoid Limit exhaustion
		writeToConsFrequ = 1000; 	//Write every x entries to console. (e.g. wrote "x" to ...)
		maxCnt = 400;				//Max number of Pages per author (per page 100 tweets, however with retweets, that will get filtered. Usually 10-15 is enough for high output authors.
		numOfTweets = numTweets;
		debug = false;				//Debug-flag. Checks if folder exist. If so crwaled data will be included in new folder(twitterhandle+unixtime of crawl). Also reduces tweets and accounts crawled 
		
		tweets = new HashMap<String, HashMap<List<Status>,List<String> > >();
		twitter = new TwitterFactory(cb.build()).getInstance();
		users = usersToCrawl;
		pageno = 1;
		
	}
	
	public HashMap<String, HashMap<List<Status>,List<String> > > crawlTweets() {
		//start = System.currentTimeMillis()/1000L;
		sleepCheck();
		for(TwitterAccounts user : users) {
			int cnt = users.indexOf(user)+1;
			try {
				HashMap<List<Status>, List<String>> t = getTweetsFromUser(twitter, user.TwitterHandle, cnt);
				tweets.put(user.TwitterHandle, t);
				HashMap.Entry< List<Status>, List<String>>  tmpEntry = t.entrySet().iterator().next();
				List<Status> tmpStat = tmpEntry.getKey();
				user.numOfOriginalTweets=tmpStat.size();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(TwitterException e) {
				e.printStackTrace();
				break;
			}
		}
		return tweets;

	}
	
	private HashMap<List<Status>, List<String>> getTweetsFromUser(Twitter twitter, String user, int num) throws JSONException, TwitterException {
		int pageno = 1;
	    List<Status> statuses = new ArrayList<Status>();
	    List<String> rawJSONObjects = new ArrayList<String>();
	    HashMap<List<Status>, List<String>> kombi = new HashMap<List<Status>, List<String>>(); 
	   
	    System.out.println("Trying to get min " + Integer.toString(numOfTweets) + " Tweets from @" + user + " (" + num + "/" + users.size() + ")");
	    int  cnt =0;
	    
	    while (true) {
	        
	    	int size = numOfTweets;
	        Paging page = new Paging(pageno++, 100);
	        int getNoOfStatusAdded1 = statuses.size();
	        statuses.addAll(twitter.getUserTimeline(user, page));
	        int getNoOfStatusAdded2 = statuses.size();
	        int statusAdded = getNoOfStatusAdded2 - getNoOfStatusAdded1;
	        
	        for(int i = statuses.size()-statusAdded;i<statuses.size();i++) {
	        	if(statuses.get(i).isRetweet()) {
	        		statuses.remove(i);
	        		i--;
	        	}
	        	else {
	        		String s2 = TwitterObjectFactory.getRawJSON(statuses.get(i));
	        		rawJSONObjects.add(s2);
	        	}

	        }
	        cnt++;
	        if(cnt % checkFrequ == 0) {
	        	sleepCheck();
	        }
	        if(cnt % writeToConsFrequ == 0) {
	        	System.out.println("Got " + Integer.toString(statuses.size()) + " Tweets from @" + user + "(cnt: " + Integer.toString(cnt) + ")");
	        }
	        if (statuses.size()-1 >= size || cnt>=maxCnt) {
	        	System.out.println("cnt "+ Integer.toString(cnt) + " bei " + user);
	          	break;
	        }
	      
	    } 
	    kombi.put(statuses, rawJSONObjects);
	    System.out.println("Crawled "+statuses.size() + " Tweets from @" + user);
	    Status latest = statuses.get(0);	    
	    Status oldest = statuses.get(statuses.size()-1);
	    Date first_date = latest.getCreatedAt();
	    Date last_date = oldest.getCreatedAt();
	    first_date.toString();
	    
	    System.out.println("Oldest Tweet: " + last_date.toString()+ " \nLatest Tweet: " + first_date.toString() );
		return kombi;
	}
	
	public void sleepCheck() {
		try {
			RateLimitStatus m = limit("/statuses/user_timeline");
			int limit = m.getRemaining();
			int sleep = m.getSecondsUntilReset()+1;
						
			System.out.println("Remaining Rate Limit: " + limit);			
						
			if(limit <2*checkFrequ+1) {
				System.out.println("Limit reached! Going to sleep for " + Integer.toString(sleep) + " seconds...");
				Thread.sleep((sleep+1)*1000);
			}
			
		} catch (TwitterException e) {
			System.out.println("TwitterException: Maybe Twitter is Down!");
			e.printStackTrace();
			System.out.println("TwitterException: Maybe Twitter is Down!");
			System.exit(-1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private RateLimitStatus limit(String endpoint) throws TwitterException {
		  String family = endpoint.split("/", 3)[1];
		  RateLimitStatus m = twitter.getRateLimitStatus(family).get(endpoint);
		  return m;
		}
	
}
