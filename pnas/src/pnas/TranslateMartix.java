package pnas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslateMartix {
	HashMap<String, HashMap<String, Integer>> usersitems;
	HashMap<String, HashMap<String, Integer>> itemsusers;

	public TranslateMartix(
			HashMap<String, HashMap<String, HashMap<String, Integer>>> linksmap) {
		// 传入包含usersitems和usersitems字典的字典
		// TODO Auto-generated constructor stub
		this.usersitems = linksmap.get("usersitems");
		this.itemsusers = linksmap.get("itemsusers");
		// System.out.println(this.usersitems);
	}

	public HashMap<String, Float> getOneItemW(String testitem, float lambda) {
		HashMap<String, Float> itemw = new HashMap<String, Float>();
		for (String compareitem : this.itemsusers.keySet()) {
			float sigma = 0f;
			// System.out.println(testitem);
			Set<String> testitemlinkusers = this.itemsusers.get(testitem)
					.keySet();
			// System.out.println(this.itemsusers.get(testitem));
			int testitemlinkusersdegree = this.itemsusers.get(testitem).size();
			int compareitemlinkusersdegree = this.itemsusers.get(compareitem)
					.size();
			Set<String> compareitemlinkusers = this.itemsusers.get(compareitem)
					.keySet();
			Set<String> commonusers = new HashSet<String>();
			commonusers.addAll(testitemlinkusers);
			commonusers.retainAll(compareitemlinkusers);
			// commonusers.remove("degree");
			if (commonusers.size() == 0)
				continue;
			for (String user : commonusers) {
				// System.out.println(user);
				int degree = this.usersitems.get(user).size();
				sigma += 1f / degree;
			}
			float a = (float) (1f / (Math.pow(testitemlinkusersdegree,
					1 - lambda) * Math.pow(compareitemlinkusersdegree, lambda)));
			float w = a * sigma;
			itemw.put(compareitem, w);
		}
		return itemw;

	}

	public static void main(String[] args) throws IOException {
		ConcurrentHashMap<String, HashMap<String, Float>> wmartix = new ConcurrentHashMap<String, HashMap<String, Float>>();
		float lambda = 0f;
		CreateNetwork createnetwork = new CreateNetwork();
		ArrayList<String[]> links = createnetwork
				.getLinkList("/source/test.data");
		HashMap<String, ArrayList<String[]>> a = createnetwork.randomDel(links,
				0.1f);
		ArrayList<String[]> newlinks = a.get("newlinks");
		ArrayList<String[]> removelinks = a.get("dellinks");
		System.out.println("remove"+createnetwork.mapLink(removelinks));
		HashMap<String, HashMap<String, HashMap<String, Integer>>> linksmap = createnetwork
				.mapLink(newlinks);

		TranslateMartix test = new TranslateMartix(linksmap);
		// 创建一个可重用固定线程数的线程池
		System.out.println("start to multiprocess!");
		long now = System.currentTimeMillis();

		ExecutorService pool = Executors.newFixedThreadPool(100);

		for (String item : test.itemsusers.keySet()) {

			pool.execute(new Myrunnable(test, item, wmartix, lambda));

		}

		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				System.out.println("used secondes"
						+ String.valueOf(System.currentTimeMillis() - now));
				System.out.println(wmartix.size());
				System.out.println(wmartix);
				// System.out.println(wmartix.get("1022"));
				break;
			}

		}

		// System.out.println(wmartix);
		// TODO Auto-generated method stub

	}

}

class Myrunnable implements Runnable {
	TranslateMartix translatemartix;
	String item;
	float lambda;
	ConcurrentHashMap<String, HashMap<String, Float>> wmartix;

	public Myrunnable(TranslateMartix translatemartix, String item,
			ConcurrentHashMap<String, HashMap<String, Float>> wmartix,
			float lambda) {
		this.translatemartix = translatemartix;
		this.item = item;
		this.wmartix = wmartix;
		this.lambda = lambda;
	}

	@Override
	public void run() {
		HashMap<String, Float> oneitemws = this.translatemartix.getOneItemW(
				this.item, this.lambda);
		// TODO Auto-generated method stub
		this.wmartix.put(this.item, oneitemws);
	}
}

class Myrunnable2 implements Runnable {
	TranslateMartix translatemartix;
	String[] item;
	float lambda;
	int start;
	int end;
	HashMap<String, HashMap<String, Float>> result;
	ConcurrentHashMap<String, HashMap<String, Float>> wmartix;

	public Myrunnable2(TranslateMartix translatemartix, String[] item,
			ConcurrentHashMap<String, HashMap<String, Float>> wmartix,
			int start, int end, float lambda) {
		this.translatemartix = translatemartix;
		this.item = item;
		this.wmartix = wmartix;
		this.lambda = lambda;
		this.result = new HashMap<String, HashMap<String, Float>>();
		this.start = start;
		this.end = end;
	}

	@Override
	public void run() {
		for (int i = this.start; i < this.end; i++) {
			HashMap<String, Float> oneitemws = this.translatemartix
					.getOneItemW(this.item[i], this.lambda);
			this.result.put(this.item[i], oneitemws);
		}
		// TODO Auto-generated method stub
		this.wmartix.putAll(result);
	}
}
