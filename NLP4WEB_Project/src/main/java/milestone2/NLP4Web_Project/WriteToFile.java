package milestone2.NLP4Web_Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Status;

public class WriteToFile {
	String src;
	String filename;
	File fl;
	List<String> domains;
	HashMap< String, HashMap< List<Status>,List<String> > > tweets;
	List<TwitterAccounts> lookup;
	StringBuilder s;
	int writeToConsFrequ = 0;
	
	public WriteToFile(List<String> domains, HashMap< String, HashMap< List<Status>,List<String> > > tweetsToWrite, List<TwitterAccounts> data, String source, int writeToCons) {
		//TODO: Gets List of crawled Tweets as JSON with Twitterhandle of corresponding user, and list of domains
		//or Stringbuilder?
				// Sort TweetstoWrite in sub Groups depending on Domains
				// Check if Domain Folder exists --> no skip
					// Yes: Generate new Folder with DOMAIN_UNIXTIME
				//Generate new Folder in generated Folder with name: _TWITTERHANDLE
				//Write in new Subfolder with name: _NUMOFTWEET_.txt
		
		
		src = source;
		
		try {
			fl = new File(src);
		}catch(Exception e) {
			System.err.println(e.getMessage());
			System.out.println("Error trying to generate Filepath");
			System.out.println(src + " seems not to be valid");
			
		}
		this.domains = domains;
		tweets = tweetsToWrite;
		lookup = data;
		writeToConsFrequ = writeToCons;
	}
	
	public WriteToFile(StringBuilder s, String source) {
		//Not sure set with Paramneter is right
		//This is for writing single file containing all the Stats
		//Supposed to check if this file Exists.
			//Yes: Generate New File with name: stats__UNIXTIME_.txt
			//No: Generate new File with name: stats.txt
		this.s = s;
		src=source;
	}
	
	public void write() {
		//write whatever input is set.
		if( (domains != null && !domains.isEmpty()) && (tweets != null && !tweets.isEmpty()) ) {
			CheckWriteUsers();
		}
		else if(s != null && !s.equals("") && !s.equals(null)) {
			CheckWriteStats();
		}
		else {
			System.out.println("Error: Did not get any content to write");
		}
	}
	
	private void CheckWriteUsers(){
		//1. Sort tweets in subgroups
		for(String dom : domains) {
			if(isDirectory(dom)) {
				System.out.println("Generated folder " + dom);
				List<TwitterAccounts> tmp = getDomainAccounts(dom, lookup);
				for(TwitterAccounts tAcc:tmp) {
					if(isSubDirectory(dom, tAcc.TwitterHandle)) {
						System.out.println("Generated Subfolder " + dom + "/" + tAcc.TwitterHandle);
						writeUsers(tAcc, dom);
					}
				
				}
			}
			else {
				dom += Long.toString(System.currentTimeMillis() / 1000L) ;
				List<TwitterAccounts> tmp = getDomainAccounts(dom, lookup);
				for(TwitterAccounts tAcc:tmp) {
					if(isSubDirectory(dom, tAcc.TwitterHandle)) {
						System.out.println("Generated Subfolder " + dom + "/" + tAcc.TwitterHandle + " in existing folder");
						writeUsers(tAcc, dom);
					}
					else {
						System.out.println("Error: Writing " + tAcc.TwitterHandle + " failed. Cant build Subdirectory with that name");
					}
				}
			}
		}
	}
	
	private void writeUsers(TwitterAccounts acc, String dom) {
		//Write for each Twitteraccounts all JSON Data
		//for(TwitterAccounts acc : tmp) {
			
			
			List<String> tweetsJSON = getJSONFromHashMap(tweets, acc.TwitterHandle);
				for(String singleTweetJSON : tweetsJSON) {
					int cnt = tweetsJSON.indexOf(singleTweetJSON);			    	
			    	String filename = src + "/" + dom + "/" + acc.TwitterHandle + "/" + Integer.toString(cnt) + ".txt";
					FileWriter writer;
					try {
						writer = new FileWriter(filename);
				    	if(cnt%writeToConsFrequ==0) {
				    		String temp = "Wrote \"" + Integer.toString(cnt) + "\" to folder " +  src + "/" + dom + "/" + acc.TwitterHandle + "/" +  "  !";
				    		System.out.println(temp);
				    	}
				        StringBuilder sb = new StringBuilder();
				        sb.append(singleTweetJSON);
				        writer.write(sb.toString());
				        writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
		//}
	}
	
	private List<TwitterAccounts> getDomainAccounts(String domain, List<TwitterAccounts> masterList){
		//returns a List<TwitterAccounts> containing a Subset of masterList, that have all 'Domain' equal to String domain
		List<TwitterAccounts> tmpAcc = new ArrayList<TwitterAccounts>();
		
		for(TwitterAccounts acc : masterList) {
			if(acc.Domain.equals(domain)) {
				tmpAcc.add(acc);
			}
		}
		
		return tmpAcc;
		
	}
	
	private boolean isSubDirectory(String dir, String sub) {
		try {
    		File fl = new File (src+"/"+dir + "/" + sub);
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
        	System.out.println("Probably due to Mistake in either source: \"" + src +  "\" or direc \"" +dir+ "or dir" + sub + "\".");
        	System.out.println(e);
        	System.out.println("###########################");
    	}
		return false;
	}
	
	private boolean isDirectory(String dom) {
		try {
    		File fl = new File (src+"/"+dom);
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
        	System.out.println("Probably due to Mistake in either source: \"" + src +  "\" or direc \"" +dom+"\".");
        	System.out.println(e);
        	System.out.println("###########################");
    	}
		return false;
	}
	
	private List<String> getJSONFromHashMap (HashMap<String, HashMap<List<Status>,List<String> > > tweets, String handle) {
		//returns List<String> of all JSON Objects in hashMap for TwitterHandle handle
		List<String> tmp = new ArrayList<String>();
		HashMap<List<Status>,List<String>> userTweets = tweets.get(handle);
		for(Map.Entry<List<Status>, List<String> > Status : userTweets.entrySet()) {
			List<String> val = Status.getValue();
			tmp.addAll(val);
		}
		return tmp;
	}
	
	private void writeStats(String source, String name){
		
		String filename = src + "/" + name + ".txt";
		FileWriter writer;
		try {
			writer = new FileWriter(filename);
	 
			writer.write(s.toString());
			writer.close();
			System.out.println("Wrote Statitsics.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void CheckWriteStats(){
		File fl = new File(src);
		if(fl.isDirectory()) {
			if(!fl.isFile()) {
				writeStats(src, "Statistics");
			}
			else if(fl.isFile()) {
				String s = "Statistics" + Long.toString(System.currentTimeMillis() / 1000L);
				writeStats(src, s);
			}
		}
	}

	
	

}
