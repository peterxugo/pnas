package pnas;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UserLinkDist {
	
	public void getlinkdegree(String filename) throws IOException{
		String fileName = "source/duserlinkdegree.txt";
		@SuppressWarnings("resource")
		FileWriter fw = new FileWriter(fileName);
		CreateNetwork network = new CreateNetwork();
		ArrayList<String[]> links = network.getLinkList(filename);
		HashMap<String, HashMap<String, HashMap<String, Integer>>> linkmap = network.mapLink(links);
		HashMap<String, HashMap<String, Integer>> usersitems = linkmap.get("usersitems");
		HashMap<String, HashMap<String, Integer>> itemsusers = linkmap.get("itemsusers");
		for(String user:usersitems.keySet()){
//			fw.write(usersitems.get(user).size()+"\t");
			Set<String> items = usersitems.get(user).keySet();
			for(String item:items){
				fw.write((itemsusers.get(item).size()+"\t"));
			}
			fw.write("\n");
		}
	}

	public static void main(String[] args) {
		UserLinkDist userlinkdist = new  UserLinkDist();
		try {
			userlinkdist.getlinkdegree("/source/delicious.data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
