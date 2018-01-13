package milestone2.NLP4Web_Project;
 

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonParser;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory; 

public class TwitterCrawler {
	ConfigurationBuilder cb ;
	String user;
	String src;
	int pageno;
	Twitter twitter;
	HashMap<String, List<Status>> tweets; 
	
	private TwitterCrawler() {
		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("Qt0lPWcSiDDpAl7pLHWwNwfVY")
		.setOAuthConsumerSecret("TBc8MPxVezWus9lcDpP3OYf6r3uyKs5qckCYXskDNYKYm4SCCF")
		.setOAuthAccessToken("952170518513373189-o7jeHc3uQRGiYuYBc49tosaUAQRrh2D")
		.setOAuthAccessTokenSecret("w4Q7ETZrmwRIK0dQoI4v5DkD3zpae2cmRMbYC6Y2LBYJl")
		.setJSONStoreEnabled(true)
		;
		
		tweets = new HashMap<String, List<Status>>();
		twitter = new TwitterFactory(cb.build()).getInstance();
		pageno = 1;
		user = "realDonaldTrump";
		src = "src/main/resources/Crawler";
		
		try {
			List<Status> t = getTweets(twitter, user);
			tweets.put(user, t);
			writeToFile(src, tweets);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public static void main(String[] args) {
		TwitterCrawler tc = new TwitterCrawler();		
	}
	

	
	public List getTweets (Twitter twitter, String user) throws JSONException {
	    int pageno = 1;
	    //String user = "realDonaldTrump";
	    List<Status> statuses = new ArrayList<Status>();

	    while (true) {

	      try {
	    	
	        int size = statuses.size(); 
	        Paging page = new Paging(pageno++, 100);
	        statuses.addAll(twitter.getUserTimeline(user, page));
	        //if (statuses.size() == size)
	          break;
	      }
	      catch(TwitterException e) {

	        e.printStackTrace();
	      }
	    } 
	  
	    System.out.println("Crawled "+statuses.size() + " Tweets from @" + user);
	    Status first = statuses.get(0);
	    Status last = statuses.get(statuses.size()-1);
	    
	    System.out.println("Last tweet: " + last.getText() + " \nFirst Tweet: " + first.getText() );
		return statuses;
	    
	}
	
	
	public void writeToFile(String source, HashMap<String, List<Status>> tweets) {
    	//root folder is source. Will add new directory with blog name and insert single articles as files
		
		//Todo: Need to rethink the structure the jsonobjects are supposed to be stored. each tweet in one file in one folder?
		
		for(Map.Entry<String, List<Status>> entry : tweets.entrySet()) {
			String key =entry.getKey();
			List<Status> userTweets = entry.getValue();		
			///////////////////////////////////////////////////////////////////////////
		
	    	if(isDirectory(source, key)) {            
	            userTweets.forEach(a -> {
				    try {
				    	int cnt = userTweets.indexOf(a);			    	
				    	String filename = source + "/" + key + "/" + Integer.toString(cnt) + ".txt";
		
				    	FileWriter writer = new FileWriter(filename);
				        String temp = "Wrote \"" + Integer.toString(cnt) + "\" to file " +  source + "/" + key + "/" + "!";
				        //display to console
				        System.out.println(temp);
				        
				        //save to file
				        //final String LF = System.getProperty("line.separator");
				        
				        StringBuilder sb = new StringBuilder();
				        sb.append(a.toString());
				       
				        
//				        sb.append(a.get(2));
//				        sb.append(LF);
//				        sb.append(a.get(3));
				        
				        writer.write(sb.toString());
				        writer.close();
				        
				    } catch (IOException e) {
				        System.err.println(e.getMessage());
				    }
				});
	    	}
	    	else {
	    		System.out.println("###########################");
	        	System.out.println("Error: Did not save to files!");
	        	System.out.println("isDirectory(String, String) returned false.");
	        	System.out.println("Probably due to an Error in itself, resulting in the faliure to crate at least one necessary folder!");
	        	System.out.println("###########################");
	    	}
		}
	}

	 public boolean isDirectory(String source, String direc) {
	    	try {
	    		
	    		File fl = new File (source+"/"+direc);
	    		if(!fl.isDirectory()) {
	    			if(!fl.isFile()) {
	    				if(fl.mkdirs()) {
	    					return true;
	    				}
	    			}
	    		}
	    	}
	    	catch(Exception e){
	    		System.out.println("###########################");
	        	System.out.println("Error: Did not create folder!");
	        	System.out.println("Probably due to Mistake in either source: \"" + source +  "\" or direc \"" +direc+"\".");
	        	System.out.println(e);
	        	System.out.println("###########################");
	        	
	    	}
	    	return false;
	    }

}
