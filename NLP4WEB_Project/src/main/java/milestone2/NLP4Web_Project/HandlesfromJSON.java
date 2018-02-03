package milestone2.NLP4Web_Project;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class HandlesfromJSON {

	String src ="";
	String file = "";
	
	public HandlesfromJSON(String path, String json) {
		//Set Path where JSOn Object is stored
		src = path;
		file = json;
	}
	
	public List<TwitterAccounts> getList() {
		Gson gson = new Gson();
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(src + "/" + file));
			TwitterAccounts[] array = gson.fromJson(reader, TwitterAccounts[].class); // contains the whole reviews list
			List<TwitterAccounts> data = new ArrayList<TwitterAccounts>();
			for(TwitterAccounts t : array) {
			
				data.add(t);
			}
			return data;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	
	public static void main(String[] args) {
		// Todo: Write this class and write main/orchestrating Class. Alos clean Up TwitterCrawlerClass
		// This class is supposed to get all Data from src/main/resources/Resources/TwitterHanlde_Json and return a GSON Object
		//ToDo: test this --> Debug or proceed
		HandlesfromJSON s = new HandlesfromJSON("src/main/resources/Resources","TwitterHandles.json");	
		//HandlesfromJSON s = new HandlesfromJSON(src+file);	
		
		
		final String LF = System.getProperty("line.separator");
		final String TAB = System.getProperty("\t");
		StringBuilder sb  = new StringBuilder();
		Formatter fm = new Formatter(sb, Locale.US);
	 
		String formatMy = "%1$3d %2$-15s %3$4s %4$-20s %5$-20s %6$5.1f %7$5.1f %8$b %9$b";
		String formatMy2 = "%1$3d %2$-15s %3$4s %4$-20s %5$-20s";		 
			 
		List<TwitterAccounts> l = s.getList();
		sb.append("Test Output:");
		
		for (TwitterAccounts t : l) {
			sb.append(LF);
			fm.format(formatMy, t.Number, t.Domain, t.Country, t.Name, t.TwitterHandle, t.numOfTweetsInK, t.numOfOriginalTweets, t.isSingleAuthor, t.Gender);
			//fm.format(formatMy2, t.Number, t.Domain, t.Country, t.Name, t.TwitterHandle);
			
			//System.out.println(t.Name );
		}
		
		fm.close();
		System.out.println(sb.toString());
	}
	


	 
	
	
}
