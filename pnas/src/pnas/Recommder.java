package pnas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Recommder {
	ConcurrentHashMap<String, HashMap<String, Float>> wmartix;
	HashMap<String, HashMap<String, Integer>> usersitems;
	HashMap<String, HashMap<String, Integer>> itemsusers;

	HashMap<String, HashMap<String, Integer>> removeusersitems;

	public Recommder(float lambda) throws IOException {
		this.getBasicData(lambda);
		// this.mapremovelinks = this.usersitems;
		// TODO Auto-generated constructor stub
	}

	public void getBasicData(float lambda) throws IOException {

		ConcurrentHashMap<String, HashMap<String, Float>> wmartix = new ConcurrentHashMap<String, HashMap<String, Float>>();
		CreateNetwork createnetwork = new CreateNetwork();
		ArrayList<String[]> links = createnetwork
				.getLinkList("/source/new.data");
		HashMap<String, ArrayList<String[]>> result = createnetwork.randomDel(
				links, 0.1f);
		ArrayList<String[]> newlinks = result.get("newlinks");
		ArrayList<String[]> removelinks = result.get("dellinks");
		HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap = createnetwork
				.mapLink(newlinks);
		HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmaps = createnetwork
				.mapLink(removelinks);
		HashMap<String, HashMap<String, Integer>> removeusersitems = removelinksmaps
				.get("usersitems");
		this.removeusersitems = removeusersitems;
		this.usersitems = newlinksmap.get("usersitems");
		this.itemsusers = newlinksmap.get("itemsusers");

		TranslateMartix test = new TranslateMartix(newlinksmap);
		// 鍒涘缓涓�涓彲閲嶇敤鍥哄畾绾跨▼鏁扮殑绾跨▼姹�
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
				System.out.println("wmartix\t" + wmartix.size());
				this.wmartix = wmartix;
				break;
			}

		}

	}

	public HashMap<String, Float> getOneUserrecommder(String user) {
		HashMap<String, Float> userrecommder = new HashMap<String, Float>();
		HashSet<String> testitems = new HashSet<String>();
		testitems.addAll(this.itemsusers.keySet());
		testitems.removeAll(this.usersitems.get(user).keySet());
		for (String item : testitems) {
			float score = 0;
			HashSet<String> commonitems = new HashSet<String>();
			commonitems.addAll(this.usersitems.get(user).keySet());
			commonitems.retainAll(this.wmartix.get(item).keySet());
			if (commonitems.size() == 0) {
				continue;
			}
			for (String node : commonitems) {
				// System.out.println(this.usersitems.get(user));
				// System.out.println(node);
				score += this.wmartix.get(item).get(node)
						* this.usersitems.get(user).get(node);
			}
			userrecommder.put(item, score);
		}
		return userrecommder;// 瀛樺湪杩斿洖鍊兼槸绌虹殑鎯呭喌锛屽綋鎵�鏈夊垹闄ょ殑杈规病鏈夊緱鍒板垎鏁般��

	}

	public static void main(String[] args) throws IOException {
		long a = System.currentTimeMillis();
		System.out.println("start is " + String.valueOf(a));
		// TODO Auto-generated method stub
		float lambad = 0f;
		Recommder test = new Recommder(lambad);



		ConcurrentHashMap<String, HashMap<String, Float>> reommdermap = new ConcurrentHashMap<String, HashMap<String, Float>>();
		HashSet<String> commusers = new HashSet<String>();
		commusers.addAll(test.removeusersitems.keySet());
		commusers.retainAll(test.usersitems.keySet());
		ExecutorService pool2 = Executors.newFixedThreadPool(100);
		for (String user : commusers) {
			pool2.execute(new Mythread(user, test, reommdermap));
		}
		pool2.shutdown();
		while (true) {
			if (pool2.isTerminated()) {
				System.out.println("ok!");
				System.out.println("cost seconds is "
						+ String.valueOf(System.currentTimeMillis() - a));
				System.out.println(reommdermap.size());
				break;
			}
		}

	}

}

class Mythread implements Runnable {
	String user;
	Recommder recommder;
	ConcurrentHashMap<String, HashMap<String, Float>> reommdermap;

	Mythread(String user, Recommder recommder,
			ConcurrentHashMap<String, HashMap<String, Float>> reommdermap) {
		this.user = user;
		this.recommder = recommder;
		this.reommdermap = reommdermap;
	}

	@Override
	public void run() {
		HashMap<String, Float> userrecommder = this.recommder
				.getOneUserrecommder(this.user);
		this.reommdermap.put(this.user, userrecommder);
		// TODO Auto-generated method stub

	}

}
