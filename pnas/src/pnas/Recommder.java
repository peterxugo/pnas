package pnas;

import java.util.ArrayList;
import java.util.Collections;
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
	HashMap<Integer, Integer> sameuseritemsdegreesmeadian;
	HashMap<Integer, ArrayList<Integer>> sameuseritemsdegrees;

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
//		this.sameuseritemsdegrees = this.getsameuseritemsdegrees();
//		this.sameuseritemsdegreesmeadian = this.getsmaedegreeuseritemsmedian(this.sameuseritemsdegrees);

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
				break;
			}

		}

	}

	public HashMap<Integer, ArrayList<Integer>> getsameuseritemsdegrees() {
		HashMap<Integer, ArrayList<Integer>> sameuseritemsdegrees = new HashMap<Integer, ArrayList<Integer>>();
		for (String user : this.usersitems.keySet()) {
			ArrayList<Integer> itemsdegree = new ArrayList<Integer>();
			for (String item : this.usersitems.get(user).keySet()) {
				itemsdegree.add(this.itemsusers.get(item).size());
			}
			int userdegree = this.usersitems.get(user).size();
			if (sameuseritemsdegrees.containsKey(userdegree)) {
				ArrayList<Integer> son = sameuseritemsdegrees.get(userdegree);
				son.addAll(itemsdegree);

			} else {
				sameuseritemsdegrees.put(userdegree, itemsdegree);
			}
		}
		return sameuseritemsdegrees;
	}

	public HashMap<Integer, Integer> getsmaedegreeuseritemsmedian(HashMap<Integer, ArrayList<Integer>> sameuseritemsdegrees) {
		HashMap<Integer, Integer> sameuseritemsdegreesmeadian = new HashMap<Integer, Integer>();
		
		for (int userdegree : sameuseritemsdegrees.keySet()) {
			ArrayList<Integer> itemsdegree = sameuseritemsdegrees
					.get(userdegree);
			Collections.sort(itemsdegree);
			int median = itemsdegree.get(itemsdegree.size() / 2);
			sameuseritemsdegreesmeadian.put(userdegree, median);
		}
		return sameuseritemsdegreesmeadian;
	}
	
	public HashMap<String, Float> getOneUserrecommder(String user, float kind) {
		HashMap<String, Float> userrecommder = new HashMap<String, Float>();
		HashSet<String> testitems = new HashSet<String>();

		testitems.addAll(this.itemsusers.keySet());
		testitems.removeAll(this.usersitems.get(user).keySet());


		ArrayList<Integer> itemsdegrees = new ArrayList<Integer> ();
		for (String item : this.usersitems.get(user).keySet()) {
			itemsdegrees.add(this.itemsusers.get(item).size());
		}
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
				double distance = 0;
				for (int itemdegree : itemsdegrees) {
					distance += Math.abs(nodedegree - itemdegree);
				}
				distance = distance / itemsdegrees.size();
				distance = 1d / (1 + distance);
				score += this.wmartix.get(item).get(node) * distance;
			}
			userrecommder.put(item, score);
		}
		return userrecommder;
	}
	// public HashMap<String, Float> getOneUserrecommder(String user) {
	// HashMap<String, Float> userrecommder = new HashMap<String, Float>();
	// HashSet<String> testitems = new HashSet<String>();
	//
	// testitems.addAll(this.itemsusers.keySet());
	// testitems.removeAll(this.usersitems.get(user).keySet());
	//
	// for (String item : testitems) {
	// float score = 0;
	// HashSet<String> commonitems = new HashSet<String>();
	// commonitems.addAll(this.usersitems.get(user).keySet());
	// commonitems.retainAll(this.wmartix.get(item).keySet());
	// if (commonitems.size() == 0) {
	// continue;
	// }
	// for (String node : commonitems) {
	// score += this.wmartix.get(item).get(node);
	// }
	// userrecommder.put(item, score);
	// }
	// return userrecommder;
	//
	// }
}

class Mythread implements Runnable {
	String user;
	Recommder recommder;
	ConcurrentHashMap<String, HashMap<String, Float>> reommdermap;
	HashMap<Integer, Integer> sameuseritemsdegreesmeadian;
	float kind;

	Mythread(String user, Recommder recommder,
			ConcurrentHashMap<String, HashMap<String, Float>> reommdermap) {
		this.user = user;
		this.recommder = recommder;
		this.reommdermap = reommdermap;
	}

	@Override
	public void run() {
		HashMap<String, Float> userrecommder = this.recommder
				.getOneUserrecommder(this.user, 1);
		this.reommdermap.put(this.user, userrecommder);
		// TODO Auto-generated method stub

	}

}
