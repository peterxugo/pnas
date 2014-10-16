package pnas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Recommder {
	ConcurrentHashMap<String, HashMap<String, Float>> wmartix;
	HashMap<String, HashMap<String, Integer>> usersitems;
	HashMap<String, HashMap<String, Integer>> itemsusers;
	HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap;
	HashMap<String, HashMap<String, Integer>> removeusersitems;

	public Recommder(
			float lambda,
			HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap,
			HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmaps) {
		this.removeusersitems = removelinksmaps.get("usersitems");
		this.newlinksmap = newlinksmap;
		this.usersitems = newlinksmap.get("usersitems");
		this.itemsusers = newlinksmap.get("itemsusers");
		this.wmartix = new ConcurrentHashMap<String, HashMap<String, Float>>();
		this.getWmartix(lambda, this.wmartix);

	}

	public void getWmartix(float lambda,
			ConcurrentHashMap<String, HashMap<String, Float>> wmartix) {

		TranslateMartix test = new TranslateMartix(this.newlinksmap);
		ExecutorService pool = Executors.newFixedThreadPool(100);
		for (String item : test.itemsusers.keySet()) {
			pool.execute(new Myrunnable(test, item, wmartix, lambda));
		}
		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				// System.out.println("wmartix used secondes\t"
				// + String.valueOf(System.currentTimeMillis() - now));
				// System.out.println("wmartix size is \t" + wmartix.size());
				// this.wmartix = wmartix;
				break;
			}

		}

	}

	public HashMap<String, Float> getOneUserrecommder(String user, float lambda2) {
		HashMap<String, Float> userrecommder = new HashMap<String, Float>();
		HashSet<String> testitems = new HashSet<String>();

		testitems.addAll(this.itemsusers.keySet());
		testitems.removeAll(this.usersitems.get(user).keySet());

//		int userdegree = this.usersitems.get(user).size();
		for (String item : testitems) {
			float score = 0;
			HashSet<String> commonitems = new HashSet<String>();
			commonitems.addAll(this.usersitems.get(user).keySet());
			commonitems.retainAll(this.wmartix.get(item).keySet());
			if (commonitems.size() == 0) {
				continue;
			}
			for (String node : commonitems) {
				int nodedegree = this.itemsusers.get(node).size();
				double pro = Math.pow(nodedegree, lambda2);
				score += this.wmartix.get(item).get(node) *  pro;
			}
			userrecommder.put(item, score);
		}
		return userrecommder;

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
				score += this.wmartix.get(item).get(node);
			}
			userrecommder.put(item, score);
		}
		return userrecommder;

	}
}

class Mythread implements Runnable {
	String user;
	Recommder recommder;
	ConcurrentHashMap<String, HashMap<String, Float>> reommdermap;
	float lambda2;
	Mythread(String user, Recommder recommder,
			ConcurrentHashMap<String, HashMap<String, Float>> reommdermap) {
		this.user = user;
		this.recommder = recommder;
		this.reommdermap = reommdermap;
	}

	Mythread(String user, Recommder recommder,
			ConcurrentHashMap<String, HashMap<String, Float>> reommdermap,
			float lambda2) {
		this.user = user;
		this.recommder = recommder;
		this.reommdermap = reommdermap;
		this.lambda2 = lambda2;
	}
	@Override
	public void run() {
		HashMap<String, Float> userrecommder = this.recommder
				.getOneUserrecommder(this.user);
		this.reommdermap.put(this.user, userrecommder);
		// TODO Auto-generated method stub

	}

}
