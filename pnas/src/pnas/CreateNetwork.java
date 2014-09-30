package pnas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CreateNetwork {
	public CreateNetwork() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String[]> getLinkList(String filename) throws IOException {
		// 璇诲彇鏂囦欢鍐呭锛岃繑鍥炰竴涓瓨鍦ㄧ敤鎴峰晢鍝佽竟鏁扮粍鐨刟rraylist
		ArrayList<String[]> links = new ArrayList<String[]>();
		InputStream url = this.getClass().getResourceAsStream(filename);
		// File file = new File(url);
		// FileReader reader = new FileReader(file);
		// BufferedReader br = new BufferedReader(reader);
		BufferedReader br = new BufferedReader(new InputStreamReader(url));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] link = line.split(" ");
			links.add(link);
		}
		br.close();
		return links;
	}

	public HashMap<String, HashMap<String, HashMap<String, Integer>>> mapLink(
			ArrayList<String[]> links) {
		HashMap<String, HashMap<String, Integer>> usersitems = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, HashMap<String, Integer>> itemsusers = new HashMap<String, HashMap<String, Integer>>();
		// System.out.println(links.size());

		for (int i = 0; i < links.size(); i++) {
			String user = links.get(i)[0];
			String item = links.get(i)[1];
			if (usersitems.containsKey(user)) {
				HashMap<String, Integer> sonmap = usersitems.get(user);
				sonmap.put(item, 1);
			} else {
				HashMap<String, Integer> sonmap = new HashMap<String, Integer>();
				sonmap.put(item, 1);
				usersitems.put(user, sonmap);
			}
		}

		for (int i = 0; i < links.size(); i++) {
			String user = links.get(i)[0];
			String item = links.get(i)[1];
			if (itemsusers.containsKey(item)) {
				HashMap<String, Integer> sonmap = itemsusers.get(item);
				sonmap.put(user, 1);
			} else {
				HashMap<String, Integer> sonmap = new HashMap<String, Integer>();
				sonmap.put(user, 1);
				itemsusers.put(item, sonmap);
			}
		}
		HashMap<String, HashMap<String, HashMap<String, Integer>>> result = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
		result.put("usersitems", usersitems);
		result.put("itemsusers", itemsusers);
		// 灏嗙粨鏋滄斁鍏ュ瓧鍏镐腑浣滀负杩斿洖鍊�
		return result;
	}

	public void randomSet(int min, int max, int n, HashSet<Integer> set) {
		if (n > (max - min + 1) || max < min) {
			return;
		}
		for (int i = 0; i < n; i++) {
			// 璋冪敤Math.random()鏂规硶 HashSet<Integer>
			int num = (int) (Math.random() * (max - min)) + min;
			set.add(num);// 灏嗕笉鍚岀殑鏁板瓨鍏ashSet涓�
		}
		int setSize = set.size();
		// 濡傛灉瀛樺叆鐨勬暟灏忎簬鎸囧畾鐢熸垚鐨勪釜鏁帮紝鍒欒皟鐢ㄩ�掑綊鍐嶇敓鎴愬墿浣欎釜鏁扮殑闅忔満鏁帮紝濡傛寰幆锛岀洿鍒拌揪鍒版寚瀹氬ぇ灏�
		if (setSize < n) {
			randomSet(min, max, n - setSize, set);// 閫掑綊
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, ArrayList<String[]>> randomDel(
			ArrayList<String[]> links, float p) {
		// 杩斿洖鍒犻櫎鐨勮竟鍜岀敓浜х殑鏂扮殑杈癸紝杩欎簺杈逛繚瀛樺湪Arraylist<String[]>涓紝鏈�鍚庤鍒犻櫎鐨勮竟鍜屾柊鐨勮竟鐨凙rraylist淇濆瓨鍦ㄥ瓧鍏镐腑杩斿洖銆�
		ArrayList<String[]> newlinks = new ArrayList<String[]>();
		newlinks = (ArrayList<String[]>) links.clone();
		int linksnum = links.size();
		int delnum = (int) (linksnum * p);
		HashMap<String, ArrayList<String[]>> result = new HashMap<String, ArrayList<String[]>>();
		HashSet<Integer> set = new HashSet<Integer>();
		randomSet(0, linksnum, delnum, set);
		// System.out.println(set);
		ArrayList<String[]> dellinks = new ArrayList<String[]>();
		for (Integer index : set) {
			String[] item = links.get(index);
			dellinks.add(item);
			// System.out.println(item[0]+"\t"+item[1]);

		}
		newlinks.removeAll(dellinks);
		result.put("newlinks", newlinks);
		result.put("dellinks", dellinks);
		return result;

	}

	public static void main(String[] args) throws IOException {
		CreateNetwork test = new CreateNetwork();
		ArrayList<String[]> links = test.getLinkList("/source/test.data");
		HashMap<String, HashMap<String, HashMap<String, Integer>>> a = test.mapLink(links);
		System.out.println(a);

	}
}
