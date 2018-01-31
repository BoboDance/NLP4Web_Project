package milestone2.NLP4Web_Project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import twitter4j.Status;

public class GenStats {
	int totalNumUsers=0;
	int totalNumDomains=0;
	int totalNumFemales=0;
	int totalNumMales=0;
	int totalNumTweets=0;
	int totalNumChars=0;
	
	long start;
	long end1;
	long end2;
	long timeSpentCrawling;
	long timeSpentWriting;
	
	double avgTweetsPerUser;
	double avgCharsPerUser;
	double avgTweetsPerMaleUser;
	double avgCharsPerMaleUser;
	double avgTweetsPerFemaleUser;
	double avgCharsPerFemaleUser;
	double avgCharsPerTweet;
	double percMaleFromAll;
	
	StringBuilder sLight = new StringBuilder();
	//ToDo get Time Crawled etc. Maybe also input them in constructor?
	
	HashMap<String, HashMap<List<Status>, List<String>>> masterData;
	List<TwitterAccounts> lookupData;
	//List<String> domains;
	HashMap<String, List<TwitterAccounts>> domains = new HashMap<String, List<TwitterAccounts>>();
	//Kategorien müssen noch als einzelne Klasse geschrieben werden
	
	GenStats(HashMap<String, HashMap<List<Status>, List<String>>> crawledData, List<TwitterAccounts> accountData, List<String> domains,  long start, long end1, long end2){
		masterData = crawledData;
		lookupData = accountData;
		
		for(TwitterAccounts t: accountData) {
			//this.domains.put(s, null);
			if(this.domains!=null && !this.domains.isEmpty() && this.domains.containsKey(t.Domain)) {
				List<TwitterAccounts> tmpT = this.domains.get(t.Domain);
				tmpT.add(t);
				this.domains.put(t.Domain, tmpT);
			}
			else {
				List<TwitterAccounts> tmpT = new ArrayList<TwitterAccounts>();
				tmpT.add(t);
				this.domains.put(t.Domain, tmpT);
			}
		}
			
		this.start = start;
		this.end1 = end1;
		this.end2 = end2;	
	}
	
	private void setGlobalStats() {
		for(TwitterAccounts tAcc: lookupData) {
			totalNumUsers++;
			if(tAcc.Gender) {
				totalNumFemales++;
			}
			else {
				totalNumMales+=1;
			}
			totalNumTweets+=tAcc.numOfOriginalTweets;
			setTotalNumChars(tAcc.TwitterHandle, tAcc);		
			
		}
		
		for(String s: domains.keySet()) {
			totalNumDomains++;
		}
		avgTweetsPerUser = totalNumTweets/totalNumUsers;
		avgCharsPerUser = totalNumChars / totalNumUsers;
		avgTweetsPerMaleUser = totalNumTweets/totalNumMales;
		avgCharsPerMaleUser = totalNumChars/totalNumMales;
		avgTweetsPerFemaleUser = totalNumTweets/totalNumFemales;
		avgCharsPerFemaleUser = totalNumChars/totalNumFemales;
		avgCharsPerTweet = totalNumChars/totalNumTweets;
		percMaleFromAll = totalNumMales/totalNumUsers;
		
		timeSpentCrawling = end1-start;
		timeSpentWriting = end2-end1; 
	}
	
	private void setTotalNumChars(String twitterHandle, TwitterAccounts t) {
		HashMap<List<Status>, List<String>> tmp = masterData.get(twitterHandle);
		
		for(Map.Entry<List<Status>, List<String> > Status : tmp.entrySet()) {
			List<Status> key = Status.getKey();
			for(Status s: key) {
				totalNumChars+= s.getText().length();
				t.chars = s.getText().length();
			}
		}
	}
	
	private HashMap<String, List<Double>> getDomainStats() {
		//reorders input map to return HashMap with domains as key and a List of 5 or 6 Doubles representing
		//[numofAuhtors] ; total Number of tweets, total number of chars; AvgCgars/Tweet; Percent females; Percent US
		HashMap<String, List<TwitterAccounts>> map = domains;
		HashMap<String, List<Double>> tmpMap = new HashMap<String, List<Double>>();
		for(Map.Entry<String, List<TwitterAccounts> > domSet : map.entrySet()) {
			String key = domSet.getKey();
			List<TwitterAccounts> val = domSet.getValue();
			tmpMap.put(key, new ArrayList<Double>());
			double tmpNumOfAuthors = val.size();
			double tmpNumOfTweets = 0;
			double tmpNumOfChars = 0;
			double tmpAvgCharsPerTweet;
			double tmpPercFemales;
			double tmpPercUS;
			int cntFem = 0;
			int cntUS = 0;
			
			for(TwitterAccounts t: val) {
				double tmp1 = 0;
				double tmp2 = t.numOfOriginalTweets;
				double tmp3 = t.chars;
				double tmp4 = tmp3/tmp2;
				double tmp5 = 0; 
				double tmp6 = 0;
								
				tmpNumOfTweets+= tmp2;
				tmpNumOfChars+= tmp3;
				if(t.Gender) {
					cntFem++;
					tmp5=1;
				};
				if(t.Country =="US" ) {
					cntUS++;
					tmp6=1;
				}
				List<Double> tmpData = new ArrayList<Double>();
				tmpData.add(tmp1);tmpData.add(tmp2);tmpData.add(tmp3);tmpData.add(tmp4);tmpData.add(tmp5);tmpData.add(tmp6);
				tmpMap.put(t.TwitterHandle, tmpData);
			}
			
			tmpAvgCharsPerTweet = tmpNumOfChars/tmpNumOfTweets;
			tmpPercFemales = cntFem/tmpNumOfAuthors;
			tmpPercUS = cntUS/tmpNumOfAuthors;
			List<Double> tmpData = new ArrayList<Double>();
			tmpData.add(tmpNumOfAuthors);tmpData.add(tmpNumOfTweets);tmpData.add(tmpNumOfChars);tmpData.add(tmpAvgCharsPerTweet);tmpData.add(tmpPercFemales);tmpData.add(tmpPercUS);
			tmpMap.replace(key, new ArrayList<Double>(), tmpData);		
		}
		
		return tmpMap;
	}
	
	private StringBuilder genStringBuilder(HashMap<String,List<Double>> outPutTable) {
		
		DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
		Date date = new Date();
		String sNow = (dateFormat.format(date)); //2016/11/16 12:08:43
		 
		 
		final String LF = System.getProperty("line.separator");
		final String SP = "---------------------------------";
		StringBuilder sb  = new StringBuilder();
		Formatter fm = new Formatter(sb, Locale.US);
		Formatter fmLight = new Formatter(sLight, Locale.US);
		
		String formatArgGeneral1 = "%1$-40s %2$,10d"; //for ints
		String formatArgGeneral2 = "%1$-40s %2$,9.0f"; //for doubles
		String formatArgGeneral2a = "%1$-40s %2$,13f %3$,3f"; //for doubles
		String formatArgGeneral2b = "%1$-40s %2$,13d"; //for doubles
		String formatArgTab = "%1$-20s %2$,10.0f %3$,20.0f %4$,20.0f %5$,20.2f %6$10.3f %7$10.3f";
		String formatArgTab2 = "%1$-20s %2$-15s %3$-15s %4$-15s %5$-15s %6$-20s %7$-20s";
		

		
		sb.append("Statistics for Crawl on: ");
		sb.append(LF);
		sb.append(sNow);
		sb.append(LF);
		sb.append(SP);
		sb.append(LF);
		
		sLight.append("Statistics for Crawl on: ");
		sLight.append(LF);
		sLight.append(sNow);
		sLight.append(LF);
		sLight.append(SP);
		sLight.append(LF);
		
		
		/*
		 * 	int totalNumUsers;
			int totalNumDomains;
			int totalNumFemales;
			int totalNumMales;
			int totalNumTweets;
			int totalNumChars;
			
			double avgTweetsPerUser;
			double avgCharsPerUser;
			double avgTweetsPerMaleUser;
			double avgCharsPerMaleUser;
			double avgTweetsPerFemaleUser;
			double avgCharsPerFemaleUser;
			double avgCharsPerTweet;
			double percMaleFromAll;
		 */
		
		
		sb.append("General:");
		sb.append(LF);
		fm.format(formatArgGeneral1, "Number of accounts crawled:", totalNumUsers);
		sb.append(LF);
		fm.format(formatArgGeneral1, "Number of domains:", totalNumDomains);
		sb.append(LF);
		fm.format(formatArgGeneral1, "Number Of female users:", totalNumFemales);
		sb.append(LF);
		fm.format(formatArgGeneral1, "Number Of male users:", totalNumMales);
		sb.append(LF);
		fm.format(formatArgGeneral1, "Number of tweets crawled:", totalNumTweets);
		sb.append(LF);
		fm.format(formatArgGeneral1, "Number of chars crawled:", totalNumChars);
		sb.append(LF);
		sb.append(LF);
		fm.format(formatArgGeneral2, "Average number of tweets per user:", avgTweetsPerUser);
		sb.append(LF);
		fm.format(formatArgGeneral2, "Average number of chars per user:", avgCharsPerUser);
		sb.append(LF);
		fm.format(formatArgGeneral2, "Average number of chars per tweet:", avgCharsPerTweet);
		sb.append(LF);
		fm.format(formatArgGeneral2, "Average number of tweets per male user:", avgTweetsPerMaleUser);
		sb.append(LF);
		fm.format(formatArgGeneral2, "Average number of chars per male user:", avgCharsPerMaleUser);
		sb.append(LF);
		fm.format(formatArgGeneral2, "Average number of tweets per female user:", avgTweetsPerFemaleUser);
		sb.append(LF);
		fm.format(formatArgGeneral2, "Average number of chars per female user:", avgCharsPerFemaleUser);
		sb.append(LF);
		fm.format(formatArgGeneral2a, "Percentage of male users (female users):", percMaleFromAll, 1-percMaleFromAll);
		sb.append(LF);
		sb.append(SP);
		sb.append(LF);
		
		
		sLight.append("General:");
		sLight.append(LF);
		fmLight.format(formatArgGeneral1, "Number of accounts crawled:", totalNumUsers);
		sLight.append(LF);
		fmLight.format(formatArgGeneral1, "Number of tweets crawled:", totalNumTweets);
		sLight.append(LF);
		fmLight.format(formatArgGeneral1, "Number of domains:", totalNumDomains);
		sLight.append(LF);
		fmLight.format(formatArgGeneral1, "Number Of female users:", totalNumFemales);
		sLight.append(LF);
		fmLight.format(formatArgGeneral1, "Number Of male users:", totalNumMales);
		sLight.append(LF);
		fmLight.format(formatArgGeneral2, "Average number of tweets per user:", avgTweetsPerUser);
		sLight.append(LF);
		fmLight.format(formatArgGeneral2, "Average number of chars per user:", avgCharsPerUser);
		sLight.append(LF);
		sLight.append(SP);
		sLight.append(LF);
		
		
		fm.format(formatArgGeneral2b, "Time spent Crawling (s):", timeSpentCrawling/1000);
		sb.append(LF);
		fm.format(formatArgGeneral2b, "Time spent Writing (s):", timeSpentWriting/1000);
		sb.append(LF);
		sb.append(SP);
		sb.append(LF);
		
		
		fmLight.format(formatArgGeneral2b, "Time spent Crawling (s):", timeSpentCrawling/1000);
		sLight.append(LF);
		fmLight.format(formatArgGeneral2b, "Time spent Writing (s):", timeSpentWriting/1000);
		sLight.append(LF);
		sLight.append(SP);
		sLight.append(LF);

		fmLight.format(formatArgTab2, "Name", "Accounts total", "Original Tweets", "Chars", "Chars/Tweet", "Percent female", "Percent US");

		fm.format(formatArgTab2, "Name", "Accounts total", "Original Tweets", "Chars", "Chars/Tweet", "Percent female", "Percent US");
		for(Map.Entry<String, List<Double>> t :outPutTable.entrySet()) {
			if(t.getValue().get(0)!= 0) {
				sLight.append(LF);
				fmLight.format(formatArgTab, t.getKey(), t.getValue().get(0), t.getValue().get(1), t.getValue().get(2), t.getValue().get(3), t.getValue().get(4), t.getValue().get(5));
			}
			sb.append(LF);
			fm.format(formatArgTab, t.getKey(), t.getValue().get(0), t.getValue().get(1), t.getValue().get(2), t.getValue().get(3), t.getValue().get(4), t.getValue().get(5));
		}
		
		fm.close();

		System.out.println(sLight);
		
		return sb;
		
	}
	
	public void generateStats() {
		//TODO: Gen Values and put them in Stringbuilder. Then return string, so that main class can give it to Filewriter
		//TODO: Format and Export Data collected in this class. Fully in txt file lightweight variant in Console
		setGlobalStats();
		HashMap<String,List<Double>> outPutTable = getDomainStats();
		StringBuilder s = genStringBuilder(outPutTable);
		
		WriteToFile f = new WriteToFile(s, "src/main/resources");
		f.write();
	}

}
