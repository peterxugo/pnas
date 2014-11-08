package pnas;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserLinkDist {

	HashMap<String, HashMap<String, Integer>> oldusersitems;
	HashMap<String, HashMap<String, Integer>> olditemsusers;
	HashMap<String, HashMap<String, Integer>> newusersitems;
	HashMap<String, HashMap<String, Integer>> newitemsusers;
	HashMap<String, HashMap<String, Integer>> removeusersitems;
	
	UserLinkDist(String filename) throws IOException{
		
		CreateNetwork createnetwork = new CreateNetwork();
		ArrayList<String[]> links = createnetwork.getLinkList(filename);
		HashMap<String, HashMap<String, HashMap<String, Integer>>> oldlinksmap = createnetwork
				.mapLink(links);
		HashMap<String, ArrayList<String[]>> a = createnetwork.randomDel(links,
				0.1f);
		HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap = createnetwork
				.mapLink(a.get("newlinks"));
//		HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmap = createnetwork
//				.mapLink(a.get("dellinks"));
		
		this.olditemsusers = oldlinksmap.get("itemsusers");
		this.oldusersitems = oldlinksmap.get("usersitems");
		this.newitemsusers = newlinksmap.get("itemsusers");
		this.newusersitems = newlinksmap.get("usersitems");
		
	}
	public void getlinkdegree(String filename) throws IOException{
		FileWriter fw = new FileWriter("source/test");
		for(String user:this.oldusersitems.keySet()){
//			fw.write(usersitems.get(user).size()+"\t");
			Set<String> items = this.oldusersitems.get(user).keySet();
			for(String item:items){
				fw.write((this.oldusersitems.get(item).size()+"\t"));
			}
			fw.write("\n");
		}
	}
	public HashMap<Integer, Integer> getsame(
			HashMap<String, HashMap<String, Integer>> mymap) {
		HashMap<Integer, Integer> same = new HashMap<Integer, Integer>();
		for (String son : mymap.keySet()) {
			int degree = mymap.get(son).size();
//			System.out.println(degree);
			if (same.containsKey(degree)) {
				same.put(degree, same.get(degree) + 1);
			} else {
				same.put(degree, 1);
			}
		}
		return same;
	}
	public void outputsame(String filename) throws IOException{
		String sameusersitemsfile = "source/sameusersitemsfile";
		String sameitemsusersfile = "source/sameitemsusersfile";
		FileWriter fw1 = new FileWriter(sameusersitemsfile);
		FileWriter fw2 = new FileWriter(sameitemsusersfile);

		
		
		HashMap<Integer, Integer> sameusersitems = this.getsame(this.oldusersitems);
		HashMap<Integer, Integer> sameitemsusers = this.getsame(this.olditemsusers);
		
		
		for(Integer key:sameusersitems.keySet()){
			fw1.write(key+"\t"+sameusersitems.get(key)+"\n");
		}
		for(Integer key:sameitemsusers.keySet()){
			fw2.write(key+"\t"+sameitemsusers.get(key)+"\n");
		}
		fw1.close();
		fw2.close();
		
		
		String sameusersitemsfile3 = "source/sameusersitemsfile0.9";
		String sameitemsusersfile4 = "source/sameitemsusersfile0.9";
		FileWriter fw3 = new FileWriter(sameusersitemsfile3);
		FileWriter fw4 = new FileWriter(sameitemsusersfile4);
		HashMap<Integer, Integer> sameusersitems9 = this.getsame(this.newusersitems);
		HashMap<Integer, Integer> sameitemsusers9 = this.getsame(this.newitemsusers);
		for(Integer key:sameusersitems9.keySet()){
			fw3.write(key+"\t"+sameusersitems9.get(key)+"\n");
		}
		for(Integer key:sameitemsusers9.keySet()){
			fw4.write(key+"\t"+sameitemsusers9.get(key)+"\n");
		}
		fw3.close();
		fw4.close();
	}

	public static void main(String[] args) throws IOException {
		UserLinkDist userlinkdist = new  UserLinkDist( "/source/new_RYM.data");
//		try {
//			userlinkdist.getlinkdegree("/source/taobao.txt");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		userlinkdist.outputsame("/source/newnetflix");
	}

}
