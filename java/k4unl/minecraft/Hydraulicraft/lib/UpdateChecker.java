package k4unl.minecraft.Hydraulicraft.lib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import k4unl.minecraft.Hydraulicraft.lib.config.ModInfo;

import com.google.gson.Gson;
import com.jcraft.jogg.Page;

public class UpdateChecker {
	static class UpdateInfo {
	    List<Integer> latestVersion;
	    String dateOfRelease;
	    List<String> changelog;
	}
	
	
	public static boolean updateAvailable(){
		String json = "";
		try {
			json = readUrl("http://hydraulicraft.k-4u.nl/update.json");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Gson gson = new Gson();        
		UpdateInfo info = gson.fromJson(json, UpdateInfo.class);
		
		if(info.latestVersion.get(0) != ModInfo.VERSION_MAIN || info.latestVersion.get(1) != ModInfo.VERSION_MAJOR || info.latestVersion.get(2) != ModInfo.VERSION_MINOR){
			Log.info("New version available!");
			Log.info("Latest version released at: " + info.dateOfRelease);
			return true;
		}else{
			return false;
		}
			
		
	}
	
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }

	}
}
