package milestone2.NLP4Web_Project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
	//Approach: Give Crawler URL to archive site
	//Crawler loads first #x of articles
	//only body text will get saved into one file per blogpost, stored n one directory per blog
	
	    
	public void crawlDomain() {
		for (String s: blogs) {
			getPageLinks(s, maxArt);
			getArticles(s, maxArt);
			writeToFile("src/main/resources/Crawler", "Slate_Star_Codex");
		}
	}
	
	String[] blogs = {
			 "https://slatestarcodex.com/archives/"
	};
	
	
	//Methods for page Crawling start here
	//From Tutorial: https://www.mkyong.com/java/jsoup-basic-web-crawler-example/
	
	 private HashSet<String> links ;
	 private List<List<String>> articles ;
	 private int maxArt;
	 
	 private Crawler() {
	 links = new HashSet<>();
	 articles = new ArrayList<>();
	 //Maximum Number of articles per blog saved //Technically maximum number of Links saved. Downstream number could decrease more!
	 maxArt =10;
	 }
	 

    public void getPageLinks(String URL, int maxAnz) {
    	String URL2 = URL;
    	URL2 = URL2.replace("/archives/", "/");
        if (!links.contains(URL)) {
            try {
                Document document = Jsoup.connect(URL).get();
                //System.out.println(document.toString());
//                Elements tmp = document.select("a[href^=\"" + URL2 + "\"][rel=\"bookmark\"]").select(":not(a[href*=\"Comment\"])");
//                Elements tmp2 = document.select("div.pjgm-postcontent a[href^=\"" + URL2 + "\"]").select(":not(a[href*=\"Comment\"])").select(":not(a[href*=\"comment\"])").select(":not(a[href*=\"login\"])");
//                Elements oL2 = tmp2.select("div#pjgm-postcontent");
//                Elements tmp = document.select("a[href^=\"" + URL + "\"]");
//                Elements otherLinks = tmp.select("not: a[href^=\"Comment\"]");
                Elements otherLinks = document
                		.select("div.pjgm-postcontent a[href^=\"" + URL2 + "\"]")
                		.select(":not(a[href*=\"Comment\"])")
                		.select(":not(a[href*=\"comment\"])")
                		.select(":not(a[href*=\"login\"])");
                
               
                for (int cnt = 0; cnt < maxAnz && cnt < otherLinks.size(); cnt++) {
	            	Element page = otherLinks.get(cnt);             
            		//String newPage = page.se
                    if (links.add(page.attr("abs:href"))) {
                        //Remove the comment from the line below if you want to see it running on your editor
                        System.out.println("Added " + page.attr("abs:href") + " to 'links'!");
                    }
                    getPageLinks(page.attr("abs:href"), maxAnz);
            	}

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
	
    public void getArticles(String blogName, int maxArt) {
    	//Designed: Original Site had on one WebPage multiple Articles with one Link each. SSC just has one Article per Page, which already are in 'links'
    	//tries to connect to each link from 'links', then identify article links; then save each text with corresponding link
    	//should do: save Link, Title and Text in String List, add list to articles
        links.forEach(x -> {
            Document document;
            try {
                document = Jsoup.connect(x).get();
                Elements content = document.select("div#pjgm-content");
                Element title = content.select("h1.pjgm-posttitle").first();
                Elements textEl = content.select("div.pjgm-postcontent");
                
            	try {
                    ArrayList<String> temporary = new ArrayList<>();
                    temporary.add(x.toString());
                    temporary.add(blogName);
                    temporary.add(title.text());
                    temporary.add(textEl.text());
                    articles.add(temporary);
                    
                    System.out.println("Added \"" + title.text() + "\" to articles!");
            	}
                catch(NullPointerException e) {
                	System.out.println("###########################");
                	System.out.println("Error: Did not add " + x.toString());
                	System.out.println("Probably due to non-standard article behind link.");
                	System.out.println("###########################");
                }
   
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
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

    public void writeToFile(String source, String blogName) {
    	//root folder is source. Will add new directory with blog name and insert single articles as files
    	if(isDirectory(source, blogName)) {            
            articles.forEach(a -> {
			    try {
			    	int cnt = articles.indexOf(a);
			    	String tmpArtName = a.get(2);
			    	tmpArtName = tmpArtName.replaceAll("[^a-zA-Z]", "");
			    	String filename = source + "/" + blogName + "/" + Integer.toString(cnt) + tmpArtName + ".txt";
	
			    	FileWriter writer = new FileWriter(filename);
			        String temp = "Wrote \"" + a.get(0) + "\" to file " + filename + "!";
			        //display to console
			        System.out.println(temp);
			        
			        //save to file
			        final String LF = System.getProperty("line.separator");
			        
			        StringBuilder sb = new StringBuilder();
			        
			        sb.append(a.get(2));
			        sb.append(LF);
			        sb.append(a.get(3));
			        
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
	
	public static void main(String[] args) {
		Crawler c = new Crawler();
		c.crawlDomain();
		// TODO Auto-generated method stub

	}

}
