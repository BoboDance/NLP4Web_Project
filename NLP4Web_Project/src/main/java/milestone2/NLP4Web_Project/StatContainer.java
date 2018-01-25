package milestone2.NLP4Web_Project;

public class StatContainer {
	private String name;
	private int tweets;
	private int chars;
	
	public String getName() {
		return name;
	}
	public void setName(String s) {
		name =s;
	}
	
	
	public int getTweets() {
		return tweets;
	}
	public void setTweets(int s) {
		tweets =s;
	}
	
	public int getchars() {
		return chars;
	}
	public void setChars(int s) {
		chars =s;
	}

	public StatContainer(String s, int t, int c) {
		name =s;
		tweets = t;
		chars = c;
	}
}
