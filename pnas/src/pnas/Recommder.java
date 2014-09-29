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
		HashMap<String, ArrayList<String[]>> result = createnetwork.randomDel(links, 0.1f);
		ArrayList<String[]> newlinks = result.get("newlinks");
		ArrayList<String[]> removelinks = result.get("dellinks");
		HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap = createnetwork.mapLink(newlinks);
		HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmaps = createnetwork.mapLink(removelinks);
		HashMap<String, HashMap<String, Integer>> removeusersitems = removelinksmaps.get("usersitems");
		this.removeusersitems = removeusersitems;
		this.usersitems =  newlinksmap.get("usersitems");
		this.itemsusers = newlinksmap.get("itemsusers");
		TranslateMartix test = new TranslateMartix(newlinksmap);
		// 创建一个可重用固定线程数的线程池
		System.out.println("start to multiprocess!");
		long now = System.currentTimeMillis();
		ExecutorService pool = Executors.newFixedThreadPool(20);
		 for (String item : test.itemsusers.keySet()) {
		 pool.execute(new Myrunnable(test, item, wmartix, lambda));
		 }
		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				System.out.println("used secondes"
						+ String.valueOf(System.currentTimeMillis() - now));
				System.out.println("wmartix\t"+wmartix.size());
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
		return userrecommder;// 存在返回值是空的情况，当所有删除的边没有得到分数。

	}

	public static void main(String[] args) throws IOException {
		long a = System.currentTimeMillis();
		System.out.println("start is " + String.valueOf(a));
		// TODO Auto-generated method stub
		float lambad = 0f;
		Recommder test = new Recommder(lambad);
		HashSet<String> aaa = new HashSet<String>();
		for(String user:test.removeusersitems.keySet()){
			System.out.println(test.usersitems.get(user));
		}
		
		
		
		for (String user:test.removeusersitems.keySet()){
			test.getOneUserrecommder(user);
		}
//		ConcurrentHashMap<String, HashMap<String, Float>> reommdermap = new ConcurrentHashMap<String, HashMap<String, Float>>();
//		Recommder test = new Recommder(lambad);
//		ExecutorService pool2 = Executors.newFixedThreadPool(100);
//		Set<String> users = test.removeusersitems.keySet();
//		for (String user : users) {
//			pool2.execute(new Mythread(user, test, reommdermap));
//		}
//		pool2.shutdown();
//		while (true) {
//			if (pool2.isTerminated()) {
//				System.out.println("ok!");
//				System.out.println("cost seconds is "
//						+ String.valueOf(System.currentTimeMillis() - a));
//				System.out.println(reommdermap);
//				break;
//			}
//		}

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
