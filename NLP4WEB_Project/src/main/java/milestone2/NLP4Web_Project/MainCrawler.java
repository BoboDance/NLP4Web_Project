package milestone2.NLP4Web_Project;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import twitter4j.Status;

public class MainCrawler {

	String src = "src/main/resources/Resources";
	String target = "src/main/resources/Crawled";
	String filename = "TwitterAccountColl_Complete.json";
	//String filename = "convertcsv (9)2.json";
	int numberOfTweetsToCrawl = 1000;
	
	
	public MainCrawler() {
		/*Steps:
		1. Get List of Twitter Accounts from JSON Object
		2. Check which of these match filtercriteria
		3. Check which of these have been crawled already
		4. Start Crawling with first users
			a. 	Check if Limit is reached -> go to sleep
		5. Write to File(s)
		6. Generate Statistics */ 
		//HandlesfromJSON s = new HandlesfromJSON(src, filename);
		
		
		List<TwitterAccounts> tAcc = new HandlesfromJSON(src, filename).getList();
		List<String> domains = new ArrayList<String>();
		int cntRemoved1 = tAcc.size();
		tAcc = filterAccounts(tAcc);
		
		System.out.println("Removed " + (cntRemoved1 - tAcc.size()) + " authors from list. They didn't match filter criteria");
		
		for(TwitterAccounts user: tAcc) {
			if(!domains.contains(user.Domain)) {
				domains.add(user.Domain);
			}
		}
			
		nextTwitterCrawler crawler = new nextTwitterCrawler(tAcc, numberOfTweetsToCrawl);
		
		long start = System.currentTimeMillis();
		
		//Returns HashMap with <Twitterhandle, Hashmap(Status/Tweets, Tweets as JSON String) > 
		HashMap< String, HashMap< List<Status>,List<String> > > tweets = crawler.crawlTweets();
		
		long endCrawl = System.currentTimeMillis();
		WriteToFile writer = new WriteToFile(domains, tweets, tAcc, target, 50);
		writer.write();
		System.out.println("Done Writing. Generating Stats...");
		
		long endWrite = System.currentTimeMillis();
		GenStats stats = new GenStats(tweets, tAcc, domains, start, endCrawl, endWrite);
		stats.generateStats();
		System.out.println("DONE!");
	}
	
	public List<TwitterAccounts> filterAccounts(List<TwitterAccounts> t) {
		//This Method filters the Users that will be crawled
		//currently, 1000 original tweets per user are anticipated. To get these Accounts need to have more than 1000 tweets (from twitter account site of user) which include retweet etc.
		// therefor threshold is set at 1000 tweets
		int limit = 3000;
		List<TwitterAccounts> tmpDel = new ArrayList<TwitterAccounts>();
		for(TwitterAccounts tAcc:  t) {
			if((tAcc.Domain!="FunMix" && !tAcc.Domain.equals("FunMix")) && (tAcc.numOfTweetsInK*1000 < limit||!tAcc.isSingleAuthor)) {
				tmpDel.add(tAcc);
			}
		}
		t.removeAll(tmpDel);
		return t;
		
	}
	
	
	//This Class is supposed to replace TwitterCrawler as Main Class.  
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MainCrawler mc = new MainCrawler();

	}

}
