package pnas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Performation {
	float lambda;
	HashMap<String, HashMap<String, Integer>> usersitems;
	HashMap<String, HashMap<String, Integer>> itemsusers;

	HashMap<String, HashMap<String, Integer>> removeusersitems;
	ConcurrentHashMap<String, HashMap<String, Float>> reommdermap;
	ConcurrentHashMap<String, List<Map.Entry<String, Float>>> topn;
	ConcurrentHashMap<String, HashMap<Float, Float>> indexmap;
	ConcurrentHashMap<String, Float> ps;
	ConcurrentHashMap<String, Float> rs;
	HashSet<String> commusers;

	public Performation(float lambda) throws IOException {
		this.lambda = lambda;
		this.getRcommder();
		// System.out.println(this.removeusersitems);
		// System.out.println(this.usersitems);
		// System.out.println(this.itemsusers);
		// System.out.println("usersitems\t"+this.usersitems);
		this.topn = new ConcurrentHashMap<String, List<Entry<String, Float>>>();
		this.indexmap = new ConcurrentHashMap<String, HashMap<Float, Float>>();
		this.ps = new ConcurrentHashMap<String, Float>();
		this.rs = new ConcurrentHashMap<String, Float>();
		// TODO Auto-generated constructor stub
	}

	public void getRcommder() throws IOException {

		long a = System.currentTimeMillis();
		// System.out.println("start is " + String.valueOf(a));
		float lambad = 0.23f;
		Recommder recommder = new Recommder(lambad);
		this.itemsusers = recommder.itemsusers;
		this.usersitems = recommder.usersitems;
		this.removeusersitems = recommder.removeusersitems;

		ConcurrentHashMap<String, HashMap<String, Float>> reommdermap = new ConcurrentHashMap<String, HashMap<String, Float>>();
		HashSet<String> commusers = new HashSet<String>();
		// System.out.println("est.removeusersitems" +
		// recommder.removeusersitems);
		// System.out.println("test.usersitems" + recommder.usersitems);
		commusers.addAll(recommder.removeusersitems.keySet());
		commusers.retainAll(recommder.usersitems.keySet());
		this.commusers = commusers;

		ExecutorService pool2 = Executors.newFixedThreadPool(100);
		for (String user : commusers) {
			pool2.execute(new Mythread(user, recommder, reommdermap));
		}
		pool2.shutdown();
		while (true) {
			if (pool2.isTerminated()) {
				// System.out.println("ok!");
				System.out.println("getRcommder cost seconds is\t"
						+ String.valueOf(System.currentTimeMillis() - a));
				this.reommdermap = reommdermap;
				System.out
						.println("reommdermap size is\t" + reommdermap.size());
				break;
			}
		}

	}

	public void Sortrecommder(String user) {
		HashMap<String, Float> sortitem = this.reommdermap.get(user);
		// System.out.println(sortitem);
		if (sortitem.size() == 0) {
			this.topn.put(user, new ArrayList<Map.Entry<String, Float>>(
					sortitem.entrySet()));
		} else {
			List<Map.Entry<String, Float>> sortresult = new ArrayList<Map.Entry<String, Float>>(
					sortitem.entrySet());
			Collections.sort(sortresult,
					new Comparator<Map.Entry<String, Float>>() {

						public int compare(Entry<String, Float> o1,
								Entry<String, Float> o2) {
							// TODO Auto-generated method stub
							if (o2.getValue() - o1.getValue() > 0) {
								return 1;
							} else {
								return -1;
							}
						}
					});
			// System.out.println(sortresult);
			this.topn.put(user, sortresult);
		}

	}

	public void indexreommder(String user) {
		List<Entry<String, Float>> usertopn = this.topn.get(user);
		HashMap<Float, Float> indexmap = new HashMap<Float, Float>();
		if (usertopn.size() == 0) {
			this.indexmap.put(user, indexmap);
		} else {
			Float before = usertopn.get(0).getValue();
			int start = 0;
			int end = 0;
			ArrayList<Float> samescore = new ArrayList<Float>();

			for (int i = 0; i < usertopn.size(); i++) {
				Float now = usertopn.get(i).getValue();
				if (now == before) {
					samescore.add(now);
					end = i;
				} else {
					float index = (float) (start + end) / 2;
					for (Float item : samescore) {
						indexmap.put(item, index);
					}
					start = i;
					samescore.clear();
					samescore.add(now);
				}
			}
			this.indexmap.put(user, indexmap);
		}

	}
	public void getRankScore(String user) {
		Set<String> removeitems = this.removeusersitems.get(user).keySet();
		HashMap<Float, Float> userindexrecommder = this.indexmap.get(user);
		for (String item : removeitems) {
			Float index;
			if (userindexrecommder.containsKey(item)) {
				index = userindexrecommder.get(item);
			} else {
				index = (float) (this.itemsusers.size() + this.topn.size()) / 2;
			}
			float r = index
					/ (this.itemsusers.size() - this.usersitems.get(user)
							.size());
			this.rs.put(item, r);
		}

	}
	public void getUserPr(String user, int n) {
		if (this.topn.get(user).size() == 0) {
			System.out.println(user);
		} else {
			HashSet<String> hitset = new HashSet<String>();
			// System.out.println("this.topn" + this.topn + "user " + user);
			int i;
			for (i = 0; i < n & i < this.topn.get(user).size(); i++) {
				Entry<String, Float> item = this.topn.get(user).get(i);
				hitset.add(item.getKey());
				// System.out.println(hitset);
			}
			hitset.retainAll(this.removeusersitems.get(user).keySet());
			// System.out.println("histset "+hitset);
			float p = (float) hitset.size() / i;
			System.out.println(user + "\t" + p);
			// System.out.println(user + "\t" + String.valueOf(p));
			this.ps.put(user, p);
		}

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long a = System.currentTimeMillis();
		float lambda = 0.23f;
		Performation performation = new Performation(lambda);
		// System.exit(0);
		// System.out.println(performation.removeusersitems);
		// System.out.println(performation.reommdermap);
		ExecutorService pool3 = Executors.newFixedThreadPool(100);
		for (String user : performation.commusers) {
			// System.out.println(user);
			pool3.execute(new Mythread2(performation, user, 20));
		}
		pool3.shutdown();
		while (true) {
			if (pool3.isTerminated()) {
				// System.out.println(performation.ps);
				break;
			}
		}
		float countp = 0f;
		for (Float value:performation.ps.values()){
			countp+=value;
		}
//		for (String key : performation.ps.keySet()) {
//			countp += performation.ps.get(key);
//		}
		float countr = 0f;
		for(Float rvalue:performation.rs.values()){
			countr+=rvalue;
		}
//		for(String key:performation.rs.keySet()){
//			countr+=performation.rs.get(key);
//		}
		System.out.print("average_P is \t");
		System.out.println(countp / performation.ps.size());
		System.out.print("average_r is \t");
		System.out.println(countr/performation.rs.size());
		System.out.println("total cost"
				+ String.valueOf(System.currentTimeMillis() - a));
	}
}

class Mythread2 implements Runnable {
	Performation performation;
	String user;
	int n;

	Mythread2(Performation performation, String user, int n) {
		this.performation = performation;
		this.user = user;
		this.n = n;
	}

	@Override
	public void run() {
		performation.Sortrecommder(user);
		performation.indexreommder(user);
		performation.getUserPr(this.user, this.n);
		performation.getRankScore(user);
		// TODO Auto-generated method stub

	}

}