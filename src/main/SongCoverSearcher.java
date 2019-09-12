package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SongCoverSearcher {
	
	private static final String GOOGLE_SEARCH_URL = "https://www.googleapis.com/customsearch/v1";
	private static final String APP_API_KEY = "AIzaSyD8XvU9m8zAcWdiOXASYGHSERaWDw6f-Hk";
	private static final String BEATPORT_CX = "000131262480533438000:o_6o0ak1aww";
	
//	https://www.googleapis.com/customsearch/v1?key=AIzaSyBm94gwEdPcR482B2yH2ECyX7bGDM3-ZsE&q=RingTheAlarm&cx=000131262480533438000:o_6o0ak1aww
	public static void getImage(String query) throws IOException {
		URL obj;
		try {
			obj = new URL(GOOGLE_SEARCH_URL);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");
			
			con.addRequestProperty("key", APP_API_KEY);
			con.addRequestProperty("cx", BEATPORT_CX);
			con.addRequestProperty("q", query);
			
			if (con.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ con.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(con.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			con.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
