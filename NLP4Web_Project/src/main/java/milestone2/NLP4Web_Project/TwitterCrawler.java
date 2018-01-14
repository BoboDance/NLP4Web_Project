package milestone2.NLP4Web_Project;
 

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Collator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterCrawler {
	ConfigurationBuilder cb ;
	static String[] user;
	List<String> users;
	String src;
	String path;
	String domain;
	int pageno;
	int numOfTweets;
	Twitter twitter;
	HashMap<String, List<Status>> tweets; 
	int checkFrequ;
	int writeToConsFrequ;
	int maxCnt;
	long start;
	long end;
	long end2;
	static boolean debug;
	
	private TwitterCrawler() {
		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("Qt0lPWcSiDDpAl7pLHWwNwfVY")
		.setOAuthConsumerSecret("TBc8MPxVezWus9lcDpP3OYf6r3uyKs5qckCYXskDNYKYm4SCCF")
		.setOAuthAccessToken("952170518513373189-o7jeHc3uQRGiYuYBc49tosaUAQRrh2D")
		.setOAuthAccessTokenSecret("w4Q7ETZrmwRIK0dQoI4v5DkD3zpae2cmRMbYC6Y2LBYJl")
		.setJSONStoreEnabled(true)
		;
		
		checkFrequ = 20; 		//Check every x Requests, how many remain. Needed to avoid Limit exhaustion
		writeToConsFrequ = 50; 	//Write every x entries to console. (e.g. wrote "x" to ...)
		maxCnt = 400;			//Max number of Pages per author (per page 100 tweets, however with retweets, that will get filtered. Usually 10-15 is enough for high output authors.
		debug = false;			//Debug-flag. Omits writing crawled tweets in files 
		
		tweets = new HashMap<String, List<Status>>();
		twitter = new TwitterFactory(cb.build()).getInstance();
		users = new ArrayList<String>();
		pageno = 1;
		numOfTweets = 1000;		//Minimum of tweets per author. Maximum is at + 100
		domain = "Politics";
		path = "src/main/resources/";
		src = path+domain;
		user = new String [] {
				"realDonaldTrump", "BarackObama", "ChuckGrassley", "RepJaredPolis", "BorisJohnson", 
				"clairecmc", "ChrisChristie", "jahimes", "jeremycorbyn", "CarolineLucas",
				"David_Cameron", "BernieSanders", "RonPaul", "SpeakerRyan", "mike_pence",
				"DavidLammy", "timfarron", "Ed_Miliband", "ChukaUmunna", "tom_watson"};
		
		//Quick an dirty. Could be optimized(e.g. read from file instead of String array)
		for(String s:user) {
			users.add(s);
		}
		Collections.sort(users, Collator.getInstance());
				
	}
	

	public static void main(String[] args) {
		TwitterCrawler tc = new TwitterCrawler();	
		//for debugging: this array is used to give a subset of authors to pipeline
//		String[] tmpusers = new String [] {
//				"theresa_may", "jeremycorbyn",
//				"David_Cameron", "BernieSanders", "RonPaul", "SpeakerRyan", "mike_pence"};
		tc.checkExisting(); //Checks which users have been crawled already. If you want to update tweets of a user, you need to delete the corresponding file
		tc.crawlTweets(debug); 	//Crawls and safes into files
		tc.genStats();			//generates statistics of 
		//tc.devGenStats();
		System.out.println("######### Done! #########");
		
	}
	
	public void crawlTweets(boolean debug) {
		start = System.currentTimeMillis()/1000L;
		sleepCheck();
		for(String user : users) {
			try {
				List<Status> t = getTweetsFromUser(twitter, user);
				tweets.put(user, t);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(TwitterException e) {
				e.printStackTrace();
				break;
			}
		}
		end = System.currentTimeMillis()/1000L;
		if(!debug) {
			writeToFile(src, tweets);
		}
		end2 = System.currentTimeMillis()/1000L;
	}
	
	
	public void sleepCheck() {
		try {
			RateLimitStatus m = limit("/statuses/user_timeline");
			int limit = m.getRemaining();
			int sleep = m.getSecondsUntilReset()+1;
			
			
			if(limit <checkFrequ+1) {
				System.out.println("Limit reached! Going to sleep for " + Integer.toString(sleep) + " seconds...");
				Thread.sleep((sleep)*1000);
			}
			
		} catch (TwitterException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public RateLimitStatus limit(String endpoint) throws TwitterException {
		  String family = endpoint.split("/", 3)[1];
		  RateLimitStatus m = twitter.getRateLimitStatus(family).get(endpoint);
		  return m;
		}
	
	

	
	public List getTweetsFromUser (Twitter twitter, String user) throws JSONException, TwitterException {
	    int pageno = 1;
	    List<Status> statuses = new ArrayList<Status>();
	    System.out.println("Trying to get min " + Integer.toString(numOfTweets) + " Tweets from @" + user);
	    int  cnt =0;
	    while (true) {
	        //int size = statuses.size(); //For max tweets possible (ca. 3200)
	    	int size = numOfTweets;
	        Paging page = new Paging(pageno++, 100);
	        statuses.addAll(twitter.getUserTimeline(user, page));
	        for(int i = statuses.size()-100;i<statuses.size();i++) {
	        	if(statuses.get(i).isRetweet()) {
	        		statuses.remove(i);
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
	  
	    System.out.println("Crawled "+statuses.size() + " Tweets from @" + user);
	    Status latest = statuses.get(0);	    
	    Status oldest = statuses.get(statuses.size()-1);
	    Date first_date = latest.getCreatedAt();
	    Date last_date = oldest.getCreatedAt();
	    first_date.toString();
	    
	    System.out.println("Oldest Tweet: " + last_date.toString()+ " \nLatest Tweet: " + first_date.toString() );
		return statuses;
	}
	
	public void writeToFile(String source, HashMap<String, List<Status>> tweets) {
    			
		for(Map.Entry<String, List<Status>> entry : tweets.entrySet()) {
			String key =entry.getKey();
			List<Status> userTweets = entry.getValue();		
		
	    	if(isDirectory(source, key)) {            
	            userTweets.forEach(a -> {
				    try {
				    	int cnt = userTweets.indexOf(a);			    	
				    	String filename = source + "/" + key + "/" + Integer.toString(cnt) + ".txt";
		
				    	FileWriter writer = new FileWriter(filename);
				    	if(cnt%writeToConsFrequ==0) {
				    		String temp = "Wrote \"" + Integer.toString(cnt) + "\" to folder " +  source + "/" + key + "/" + "!";
				    		System.out.println(temp);
				    	}
				        
				        StringBuilder sb = new StringBuilder();
				        sb.append(a.toString());
				        
				        writer.write(sb.toString());
				        writer.close();
				    } catch (IOException e) {
				        System.err.println(e.getMessage());
				    }
				});
	    	}
	    	else {
	    		System.out.println("###########################");
	        	System.out.println("Error: Did not save "+ key+ " to files!");
	        	System.out.println("isDirectory(String, String) returned false.");
	        	System.out.println("Probably due to an Error in itself, resulting in the faliure to crate at least one necessary folder!");
	        	System.out.println("###########################");
	    	}
		}
	}

	 public boolean isDirectory(String source, String direc) {
		 int errorcode =0;	
		 try {
	    		File fl = new File (source+"/"+direc);
	    		if(!fl.isDirectory()) {
	    			errorcode++;//1
	    			if(!fl.isFile()) {
	    				errorcode++;//2
	    				if(fl.mkdirs()) {
	    					return true;
	    				}
	    			}
	    		}
	    	}
	    	catch(Exception e){
	    		System.out.println("###########################");
	        	System.out.println("Error" + Integer.toString(errorcode) + ": Did not create folder!");
	        	System.out.println("Probably due to Mistake in either source: \"" + source +  "\" or direc \"" +direc+"\".");
	        	System.out.println(e);
	        	System.out.println("###########################");
	    	}
	    	return false;
	    }
	 
	 public void genStats() {
		 //ToDo generate avg tweets per author, num of authors, tweets total, avg chars per tweet
		 if(users.isEmpty()) {
			 return;
		 }
		 double avgTweetsPerAuthor;
		 int numAuthors = user.length;;
		 int tweetsTotal = 0;
		 double avgCharsPerTweet = 0;
		 int charsTotal=0;
		 long deltaTimeCrawl = end-start;
		 long deltaTimeWrite = end2-end;
		 
		 List<StatContainer> tweetsPerAuthor = new ArrayList<StatContainer>();
		 
		 for(int i=0; i< users.size(); i++) {
			 List<Status> st = tweets.get(users.get(i));
			 int cntchar = 0;
			 for(Status a : st) {
				 cntchar += a.getText().length();
			 }
			 StatContainer s = new StatContainer(users.get(i),st.size(), cntchar);
			 tweetsTotal += st.size();
			 tweetsPerAuthor.add(s);
			 charsTotal += cntchar; 
		 }
		 avgCharsPerTweet = charsTotal/tweetsTotal; 
		 avgTweetsPerAuthor = (tweetsTotal/numAuthors);
		 DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
		 Date date = new Date();
		 String sNow = (dateFormat.format(date)); //2016/11/16 12:08:43
		 
		 
		 final String LF = System.getProperty("line.separator");
		 final String TAB = System.getProperty("\t");
		 StringBuilder sb  = new StringBuilder();
		 Formatter fm = new Formatter(sb, Locale.US);
		 
		 String formatArg = "%1$-30s %2$,10d";
		 String formatArg1 = "%1$-30s %2$,13.2f";
		 String formatArg1a = "%1$-30s %2$,13d";
		 String formatArg2 = "%1$-20s %2$-20s %3$-20s %4$-20s";
		 String formatArg3 = "%1$-20s %2$-,20d %3$-,20d %4$-,20f";
		 
		 sb.append("Statistics for Domain " + domain);
		 sb.append(LF);
		 sb.append(sNow);
		 sb.append(LF);
		 sb.append("General:");
		 sb.append(LF);
		 fm.format(formatArg1a, "Time spent Crawling (s):", deltaTimeCrawl);
		 sb.append(LF);
		 fm.format(formatArg1a, "Time spent Writing (s):", deltaTimeWrite);
		 sb.append(LF);
		 fm.format(formatArg, "Number of Authors: ", numAuthors);
		 sb.append(LF);
		 fm.format(formatArg, "Total Tweets: ", tweetsTotal);
		 sb.append(LF);		 
		 fm.format(formatArg, "Total Chars: ", charsTotal);
		 sb.append(LF);
		 fm.format(formatArg1, "Average Tweets per Author: ", avgTweetsPerAuthor);
		 sb.append(LF);
		 fm.format(formatArg1, "Average Chars per Tweet: ", avgCharsPerTweet);
		 sb.append(LF);
		 sb.append("*INFO: Embedded links probably count as chars*");
		 sb.append(LF);
		 sb.append(LF);
		 sb.append("Authors:");
		 sb.append(LF);
		 fm.format(formatArg2, "Name", "#Tweets", "#Chars", "Chars/Tweet");
		 for(StatContainer s : tweetsPerAuthor) {
			 sb.append(LF);
			 fm.format(formatArg3, s.getName(), s.getTweets(), s.getchars(), s.getchars()/s.getTweets());
		 }
		
		 fm.close();
		 
	    try {
	    	String fname = src + "/Statistics.txt";
	    	File fl = new File(src);
	    	if(!fl.isDirectory()) {
	    		fl.mkdirs();
	    	}
	    	FileWriter writer2 = new FileWriter(fname); 
			writer2.write(sb.toString());
			writer2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    System.out.println("Statistics saved to " + src + "/Statistics.txt");
		System.out.println(sb.toString()); 
		 
	 }
	 public void checkExisting() {
		 //Method to check if the to be crawled users have been crawled already
		 // if so, it Deletes it from user list 
		 //if there are no tweets saved, it ignores the entry (so the pipeline will then crawl theses tweets.)
		 System.out.println("Checking for existing Users...");
		 File fl = new File(path+"/"+domain);
		 if(fl.isDirectory()) {
			 String[] existingDirs = fl.list();
			 for(String s : existingDirs) {
				 if(users.contains(s)) {
					 users.remove(s);
					 System.out.println("Removed " + s + " from crawling list. If you want to update tweets, delete corresponding file!");
				 }
			 }		
			 System.out.println("############ Done Checking! ############");
		 }
	 }
	 
//	 public void devGenStats() {
//		 //Development Method for Genstats
//		 float avgTweetsPerAuthor = (float) 100.8;
//		 int numAuthors = 20;
//		 int tweetsTotal = 2000;
//		 float avgCharsPerTweet = (float) 200.85;
//		 int cntchar=40000;
//		 
//		 List<StatContainer> list= new ArrayList<StatContainer>();
//		 StatContainer a = new StatContainer("A",200,200);
//		 StatContainer b = new StatContainer("B",200,200);
//		 StatContainer c = new StatContainer("C",200,200);
//		 list.add(a);list.add(b);list.add(a);list.add(b);list.add(c);
//		 
//		 final String LF = System.getProperty("line.separator");
//		 
//		 
//		 
//		 StringBuilder sb  = new StringBuilder();
//		 Formatter fm = new Formatter(sb, Locale.US);
//		 
//		 
//		 
//		 
//		 sb.append("Statistics for Domain " + domain);
//		 sb.append(LF);
//		 sb.append(LF);
//		 sb.append("General:");
//		 sb.append(LF);
//		 
//
//		 
//		 String formatArg = "%1$-30s %2$10d";
//		 String formatArg1 = "%1$-30s %2$13.2f";
//		 String formatArg2 = "%1$-20s %2$-20s %3$-20s";
//		 String formatArg3 = "%1$-20s %2$-20d %3$-20d";
//		 fm.format(formatArg, "Number of Authors: ", numAuthors);
//		 sb.append(LF);
//		 fm.format(formatArg, "Total Tweets: ", tweetsTotal);
//		 sb.append(LF);
//		 fm.format(formatArg1, "Average Tweets per Author: ", avgTweetsPerAuthor);
//		 sb.append(LF);
//		 fm.format(formatArg1, "Average Chars per Tweet: ", avgCharsPerTweet);
//		 sb.append(LF);
//		 sb.append(LF);
//		 fm.format("Authors:");
//		 sb.append(LF);
//		 fm.format(formatArg2, "Name", "#Tweets", "#Chars");
//		 for(StatContainer s : list) {
//			 sb.append(LF);
//			 fm.format(formatArg3, s.getName(), s.getTweets(), s.getchars());
//		 }
//		
//		 fm.close();
//	    System.out.println("Statistics saved to " + src + "/Statistics.txt");
//		System.out.println(sb.toString()); 
//	 }
//	 
	 
	 

}
