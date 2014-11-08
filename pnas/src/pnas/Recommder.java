package pnas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Recommder {
	ConcurrentHashMap<String, HashMap<String, Float>> wmartix;
	ConcurrentHashMap<String, HashMap<String, Float>> secondwmartix;
	HashMap<String, HashMap<String, Integer>> usersitems;
	HashMap<String, HashMap<String, Integer>> itemsusers;
	HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap;
	HashMap<String, HashMap<String, Integer>> removeusersitems;
	HashMap<Integer, Integer> sameuseritemsdegreesmeadian;
	HashMap<Integer, ArrayList<Integer>> sameuseritemsdegrees;
	HashMap<Integer, Integer> userssame;
	HashMap<Integer, Integer> itemssame;
	HashMap<Integer, HashMap<Integer,Integer>>useritemsame;
	public Recommder(
			float lambda,
			HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap,
			HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmaps) {
		this.removeusersitems = removelinksmaps.get("usersitems");
		this.newlinksmap = newlinksmap;
		this.usersitems = newlinksmap.get("usersitems");
		this.itemsusers = newlinksmap.get("itemsusers");
		this.userssame =this.getsame(this.usersitems);
		this.itemssame = this.getsame(this.itemsusers);
		this.useritemsame = this.getlistsame(this.usersitems);
		this.wmartix = new ConcurrentHashMap<String, HashMap<String, Float>>();
		this.secondwmartix = new ConcurrentHashMap<String, HashMap<String,Float>>();
		this.getWmartix(lambda, this.wmartix,this.secondwmartix);
		
//		System.out.println(" \tafter wmartix"+wmartix);
//		System.out.println(secondwmartix);
		
		
		// this.sameuseritemsdegrees = this.getsameuseritemsdegrees();
		// this.sameuseritemsdegreesmeadian =
		// this.getsmaedegreeuseritemsmedian(this.sameuseritemsdegrees);

	}

//	public void getWmartix(float lambda,
//			ConcurrentHashMap<String, HashMap<String, Float>> wmartix) {
//
//		TranslateMartix translatemartix = new TranslateMartix(this.newlinksmap);
//		ExecutorService pool = Executors.newFixedThreadPool(100);
//		for (String item : translatemartix.itemsusers.keySet()) {
//			pool.execute(new Myrunnable(translatemartix, item, wmartix, lambda));
//		}
//		pool.shutdown();
//		while (true) {
//			if (pool.isTerminated()) {
//				break;
//			}
//
//		}
//	}
	public void getWmartix(float lambda,
			ConcurrentHashMap<String, HashMap<String, Float>> wmartix,
			ConcurrentHashMap<String, HashMap<String, Float>> secondwmartix ) {

		TranslateMartix translatemartix = new TranslateMartix(this.newlinksmap);
		ExecutorService pool = Executors.newFixedThreadPool(100);
		for (String item : translatemartix.itemsusers.keySet()) {
			pool.execute(new RunWmartix(translatemartix, item, wmartix, lambda));
		}
		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				break;
			}

		}
//		System.out.println("before wmartix"+wmartix);
		pool = Executors.newFixedThreadPool(100);
		for(Enumeration<String> keys = wmartix.keys();keys.hasMoreElements();){
			String key = keys.nextElement();
			pool.execute(new RunSecondWmartix(key, wmartix, secondwmartix, translatemartix));
		}
		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				break;
			}

		}

	}
	public HashMap<Integer, Integer> getsame(
			HashMap<String, HashMap<String, Integer>> mymap) {
		HashMap<Integer, Integer> same = new HashMap<Integer, Integer>();
		for (String son : mymap.keySet()) {
			int degree = mymap.get(son).size();
			if (same.containsKey(degree)) {
				same.put(degree, same.get(degree) + 1);
			} else {
				same.put(degree, 1);
			}
		}
		return same;
	}
	public HashMap<Integer, HashMap<Integer,Integer>> getlistsame(
			HashMap<String, HashMap<String, Integer>> mymap) {
		HashMap<Integer, HashMap<Integer,Integer>> same = new HashMap<Integer, HashMap<Integer,Integer>>();
		for (String user : mymap.keySet()) {
			
			int userdegree = mymap.get(user).size();
			HashMap<Integer, Integer> son;
			if(same.containsKey(userdegree)){
				son = same.get(userdegree);
			}else{
				son = new HashMap<Integer, Integer>();
				same.put(userdegree, son);
			}
			
			
			
			for(String item:mymap.get(user).keySet()){
				int itemdegree = this.itemsusers.get(item).size();
				if(son.containsKey(itemdegree)){
					son.put(itemdegree, son.get(itemdegree)+1);
				}else{
					son.put(itemdegree, 1);
				}
			}
		}
		return same;
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

	public HashMap<Integer, Integer> getsmaedegreeuseritemsmedian(
			HashMap<Integer, ArrayList<Integer>> sameuseritemsdegrees) {
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

//	public HashMap<String, Float> getOneUserrecommder(String user, float kind) {
//		HashMap<String, Float> userrecommder = new HashMap<String, Float>();
//		HashSet<String> testitems = new HashSet<String>();
//
//		testitems.addAll(this.itemsusers.keySet());
//		testitems.removeAll(this.usersitems.get(user).keySet());
//
//		// ArrayList<Integer> itemsdegrees = new ArrayList<Integer> ();
//		// for (String item : this.usersitems.get(user).keySet()) {
//		// itemsdegrees.add(this.itemsusers.get(item).size());
//		// }
//		for (String item : testitems) {
//			float score = 0;
//
//			HashSet<String> commonitems = new HashSet<String>();
//			commonitems.addAll(this.usersitems.get(user).keySet());
//			commonitems.retainAll(this.wmartix.get(item).keySet());
//			if (commonitems.size() == 0) {
//				continue;
//			}
//			for (String node : commonitems) {
//
//				int nodedegree = this.itemsusers.get(node).size();
//				// double distance = 0;
//				// for (int itemdegree : itemsdegrees) {
//				// distance += Math.abs(nodedegree - itemdegree);
//				// }
//				// distance = distance / itemsdegrees.size();
//				// distance = 1d / (1 + distance);
//				score += this.wmartix.get(item).get(node)
//						* Math.pow(nodedegree, kind);
//			}
//			userrecommder.put(item, score);
//		}
//		return userrecommder;
//	}
	
	
	
//	public HashMap<String, Float> getOneUserrecommder(String user, float kind) {
//		HashMap<String, Float> userrecommder = new HashMap<String, Float>();
//		HashSet<String> testitems = new HashSet<String>();
//
//		testitems.addAll(this.itemsusers.keySet());
//		testitems.removeAll(this.usersitems.get(user).keySet());
//		int userdegree = this.usersitems.get(user).size();
//		int min0 = userdegree*this.userssame.get(userdegree);
//		for (String item : testitems) {
//			float score = 0;
//			HashSet<String> commonitems = new HashSet<String>();
//			commonitems.addAll(this.usersitems.get(user).keySet());
//			commonitems.retainAll(this.wmartix.get(item).keySet());
//			if (commonitems.size() == 0) {
//				continue;
//			}
//			for (String node : commonitems) {
//				
//				int nodedegree = this.itemsusers.get(node).size();
//				int min1 = nodedegree*this.itemssame.get(nodedegree);
//				int min2 = this.userssame.get(userdegree)*this.itemssame.get(nodedegree);
//				int min = Math.min(min0, min1);
//				min = Math.min(min, min2);
//				double c = this.useritemsame.get(userdegree).get(nodedegree);
//				
//				
//				score += this.wmartix.get(item).get(node)
//						* Math.pow(c/min, kind);
//			}
//			userrecommder.put(item, score);
//		}
//		return userrecommder;
//	}
	
	public HashMap<String, Float> getOneUserrecommder(String user) {
		HashMap<String, Float> userrecommder = new HashMap<String, Float>();
		HashSet<String> testitems = new HashSet<String>();

		testitems.addAll(this.itemsusers.keySet());
		testitems.removeAll(this.usersitems.get(user).keySet());

		for (String item : testitems) {
			float score = 0;
			HashSet<String> commonitems = new HashSet<String>();
			commonitems.addAll(this.usersitems.get(user).keySet());
			commonitems.retainAll(this.secondwmartix.get(item).keySet());
			if (commonitems.size() == 0) {
				continue;
			}
			for (String node : commonitems) {
				score += this.secondwmartix.get(item).get(node);
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
	HashMap<Integer, Integer> sameuseritemsdegreesmeadian;
	float kind;

	Mythread(String user, Recommder recommder,
			ConcurrentHashMap<String, HashMap<String, Float>> reommdermap,
			float kind) {
		this.user = user;
		this.recommder = recommder;
		this.reommdermap = reommdermap;
		this.kind = kind;
	}
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

	}

}
