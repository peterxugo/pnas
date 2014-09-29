package pnas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CreateNetwork {
	public CreateNetwork() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String[]> getLinkList(String filename) throws IOException {
		// 读取文件内容，返回一个存在用户商品边数组的arraylist
		ArrayList<String[]> links = new ArrayList<String[]>();
		InputStream url = this.getClass().getResourceAsStream(filename);
//		File file = new File(url);
//		FileReader reader = new FileReader(file);
//		BufferedReader br = new BufferedReader(reader);
		BufferedReader br=new BufferedReader(new InputStreamReader(url));  
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
//		System.out.println(links.size());
		for (int i = 0; i < links.size(); i++) {
			try {
				HashMap<String, Integer> sonmap = usersitems
						.get(links.get(i)[0]);
				sonmap.put(links.get(i)[1], 1);
//				if(sonmap.size()<3)
//				System.out.println(sonmap);
				// 更新用户的邻居和度数量
			} catch (Exception e) {
				HashMap<String, Integer> sonmap = new HashMap<String, Integer>();
				sonmap.put(links.get(i)[1], 1);
				usersitems.put(links.get(i)[0], sonmap);
				// 建立用户，并更新用户
			}

		}
		for (int i = 0; i < links.size(); i++) {
			try {
				HashMap<String, Integer> sonmap = itemsusers
						.get(links.get(i)[1]);
//				sonmap.put("degree", sonmap.get("degree") + 1);
				sonmap.put(links.get(i)[0], 1);
				// 更新商品邻居和度数量
			} catch (Exception e) {
				HashMap<String, Integer> sonmap = new HashMap<String, Integer>();
				sonmap.put(links.get(i)[0], 1);
//				sonmap.put("degree", 1);
				itemsusers.put(links.get(i)[1], sonmap);
				// 建立商品，并更新商品
			}

		}
		HashMap<String, HashMap<String, HashMap<String, Integer>>> result = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
		result.put("usersitems", usersitems);
		result.put("itemsusers", itemsusers);
		// 将结果放入字典中作为返回值
		return result;
	}

	public void randomSet(int min, int max, int n, HashSet<Integer> set) {
		if (n > (max - min + 1) || max < min) {
			return;
		}
		for (int i = 0; i < n; i++) {
			// 调用Math.random()方法 HashSet<Integer>
			int num = (int) (Math.random() * (max - min)) + min;
			set.add(num);// 将不同的数存入HashSet中
		}
		int setSize = set.size();
		// 如果存入的数小于指定生成的个数，则调用递归再生成剩余个数的随机数，如此循环，直到达到指定大小
		if (setSize < n) {
			randomSet(min, max, n - setSize, set);// 递归
		}
	}

	public HashMap<String,ArrayList<String[]>> randomDel(ArrayList<String[]> links, float p) {
		//返回删除的边和生产的新的边，这些边保存在Arraylist<String[]>中，最后讲删除的边和新的边的Arraylist保存在字典中返回。
		ArrayList<String[]> newlinks = new ArrayList<String[]>();
		newlinks = (ArrayList<String[]>) links.clone();
		int linksnum = links.size();
		int delnum = (int) (linksnum * p);
		HashMap<String,ArrayList<String[]>> result = new HashMap<String, ArrayList<String[]>>();
		HashSet<Integer> set = new HashSet<Integer>();
		randomSet(0, linksnum, delnum, set);
		// System.out.println(set);
		ArrayList<String[]> dellinks = new ArrayList<String[]>();
		for (Integer index : set) {
			String[] item = links.get(index);
			dellinks.add(item);
//			System.out.println(item[0]+"\t"+item[1]);
			
		}
		newlinks.removeAll(dellinks);
		result.put("newlinks", newlinks);
		result.put("dellinks", dellinks);
		return result;

	}

	public static void main(String[] args) throws IOException {

	}
}
