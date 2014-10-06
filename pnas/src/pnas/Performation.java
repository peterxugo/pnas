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
	HashMap<String, HashMap<String, Integer>> oldusersitems;
	HashMap<String, HashMap<String, Integer>> olditemsusers;
	HashMap<String, HashMap<String, Integer>> newusersitems;
	HashMap<String, HashMap<String, Integer>> newitemsusers;
	HashMap<String, HashMap<String, Integer>> removeusersitems;
	ConcurrentHashMap<String, HashMap<String, Float>> reommdermap;
	ConcurrentHashMap<String, List<Map.Entry<String, Float>>> topn;
	ConcurrentHashMap<String, HashSet<String>> topL;
	ConcurrentHashMap<String, HashMap<Float, Float>> indexmap;
	ConcurrentHashMap<String, Float> ps;
	ConcurrentHashMap<String, Float> rs;
	ConcurrentHashMap<String, Float> is;
	ArrayList<Float> hs;
	HashSet<String> commusers;
	ConcurrentHashMap<String, HashSet<String>> hit;
	String file;


	public Performation(float lambda, String file) throws IOException {
		this.lambda = lambda;
		this.file = file;
		this.getRcommder();
		this.hit = new ConcurrentHashMap<String, HashSet<String>>();
		// System.out.println(this.removeusersitems);
		// System.out.println(this.usersitems);
		// System.out.println(this.itemsusers);
		// System.out.println("usersitems\t"+this.usersitems);
		this.topn = new ConcurrentHashMap<String, List<Entry<String, Float>>>();
		this.topL = new ConcurrentHashMap<String, HashSet<String>>();
		this.indexmap = new ConcurrentHashMap<String, HashMap<Float, Float>>();
		this.ps = new ConcurrentHashMap<String, Float>();
		this.rs = new ConcurrentHashMap<String, Float>();
		this.is = new ConcurrentHashMap<String, Float>();
		this.hs = new ArrayList<Float>();

		// TODO Auto-generated constructor stub
	}

	public void getRcommder() throws IOException {

		long a = System.currentTimeMillis();
		System.out.println(this.file);

		// System.out.println("start is " + String.valueOf(a));
		Recommder recommder = new Recommder(this.lambda, this.file);
		CreateNetwork rawnetwork = new CreateNetwork();
		HashMap<String, HashMap<String, HashMap<String, Integer>>> network = rawnetwork
				.mapLink(rawnetwork.getLinkList(file));
		
		this.olditemsusers = network.get("itemsusers");
		this.oldusersitems=network.get("usersitems");
		
		
		this.newusersitems = recommder.usersitems;
		this.newitemsusers = recommder.itemsusers;
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
		
		HashMap<Float, Float> userindexmap = new HashMap<Float, Float>();
		if (usertopn.size() == 0) {
			System.out.println(user + "\t topn is empty!");
		} else {
			float before = usertopn.get(0).getValue();
			int start = 0;
			int end = 0;
			ArrayList<Float> samescore = new ArrayList<Float>();

			for (int i = 0; i < usertopn.size(); i++) {
//				System.out.println(i+"\t"+before);
				float now = usertopn.get(i).getValue();
				if (now == before) {
					samescore.add(now);
					end = i;
				} else {
					float index = (float) (start + end) / 2;
					for (float item : samescore) {
						userindexmap.put(item, index);
					}
					start =  i;
					end = i;
//					System.out.println("end"+end+"\t"+start);
					samescore.clear();
					samescore.add(now);
					before = now;
				}
			}
			float index = (float) (start + end) / 2;
			for (float item : samescore) {
				userindexmap.put(item, index);
			}
			this.indexmap.put(user, userindexmap);
		}

	}

	public void getRankScore(String user) {
		Set<String> removeitems = this.removeusersitems.get(user).keySet();
		HashMap<Float, Float> userindexrecommder = this.indexmap.get(user);
		
		for (String item : removeitems) {
			float index;
			Float recommderscore = this.reommdermap.get(user).get(item);
			if (userindexrecommder.containsKey(recommderscore)) {
				index = userindexrecommder.get(recommderscore);
			} else {
				index = (float) (this.olditemsusers.size() + userindexrecommder.size()- this.newusersitems.get(user)
						.size()) / 2;
			}
//			System.out.println("index\t"+index);
			float r = index
					/ (this.olditemsusers.size() - this.newusersitems.get(user)
							.size());
			this.rs.put(item, r);
		}

	}

	public void getTopL(int L, String user) {
		HashSet<String> hitset = new HashSet<String>();
		// System.out.println("this.topn" + this.topn + "user " + user);
		int i;
		for (i = 0; i < L & i < this.topn.get(user).size(); i++) {
			Entry<String, Float> item = this.topn.get(user).get(i);
			hitset.add(item.getKey());
			// System.out.println(hitset);
		}
		this.topL.put(user, hitset);
	}

	public void getUserPr(String user) {
		if (this.topn.get(user).size() == 0) {
			// System.out.println(user);
		} else {
			HashSet<String> hitset = new HashSet<String>();
			// System.out.println("this.topn" + this.topn + "user " + user);
			HashSet<String> topLset = this.topL.get(user);
			hitset.addAll(topLset);
			hitset.retainAll(this.removeusersitems.get(user).keySet());
			this.hit.put(user, hitset);
			float p = (float) hitset.size() / topLset.size();
			// System.out.println(user + "\t" + p);
			// System.out.println(user + "\t" + String.valueOf(p));
			this.ps.put(user, p);
		}

	}
	public void getSurprisal(String user) {
		HashSet<String> topLitems = this.topL.get(user);
		int usernum = this.oldusersitems.size();
		float counti = 0f;
		for (String item : topLitems) {
			float a = (float) usernum / this.olditemsusers.get(item).size();
			float i = (float) (Math.log(a) / Math.log(2));
			counti += i;
		}
		this.is.put(user, counti / topLitems.size());

	}
	
	
	public void getPersonalization() {
		HashSet<String> userset = this.commusers;
		String[] userlist = new String[userset.size()];
		userset.toArray(userlist);
		
		for (int i = 0; i < userlist.length; i++) {
			HashSet<String> sameset = new HashSet<String>();
			HashSet<String> userrecommder = this.topL.get(userlist[i]);
			for (int j = i+1; j < userlist.length; j++) {
				HashSet<String> comparerecommder = this.topL.get(userlist[j]);
				sameset.addAll(userrecommder);
				sameset.retainAll(comparerecommder);
				int samesize = sameset.size();
				float h = 1 - ((float) samesize /userrecommder.size());
//				System.out.println(h);
				this.hs.add(h);
				sameset.clear();
			}
		}

	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		long a = System.currentTimeMillis();
		float lambda = 0.23f;
		String file = "/source/delicious.data";
		Performation performation = new Performation(lambda, file);
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
		for (Float value : performation.ps.values()) {
			countp += value;
		}
		// for (String key : performation.ps.keySet()) {
		// countp += performation.ps.get(key);
		// }
		float countr = 0f;
		for (Float rvalue : performation.rs.values()) {
			countr += rvalue;
		}
		float counti = 0f;
		for (float ivalues : performation.is.values()) {
			counti += ivalues;
		}

		// for(String key:performation.rs.keySet()){
		// countr+=performation.rs.get(key);
		// }
//		System.out.println("removeusersitems\t"+performation.removeusersitems+"\treommdermap"+performation.reommdermap);
//		System.out.println("indexmap"+performation.indexmap);
//		System.out.println(performation.topn+"\t"+performation.topL);
		
		System.out.print("average_P is \t");
		System.out.println(countp / performation.ps.size());
		int removecount = 0;
		for (String key : performation.removeusersitems.keySet()) {
			removecount += performation.removeusersitems.get(key).size();
		}
		float ep = performation.olditemsusers.size()*performation.oldusersitems.size()/removecount;
		System.out.println("ep is  \t "+ ep);
		
		
		/***
		System.out.print("average_r is \t");
		System.out.println(countr / performation.rs.size());
		System.out.print("average_i is \t");
		System.out.println(counti / performation.rs.size());
		System.out.println("total cost\t"
				+ String.valueOf(System.currentTimeMillis() - a));

		int count = 0;
		for (HashSet<String> hitkey : performation.hit.values()) {
			count += hitkey.size();
		}
		int removecount = 0;
		for (String key : performation.removeusersitems.keySet()) {
			removecount += performation.removeusersitems.get(key).size();
		}

		System.out.println(count + "\t" + removecount);
		performation.getPersonalization();
		float hscount = 0f;
		for (Float item : performation.hs) {
			hscount += item;
		}
		System.out.println("hscoutn" + hscount / performation.hs.size());
		***/
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
		performation.getTopL(n, user);
		performation.indexreommder(user);

		performation.getUserPr(user);
//		performation.getSurprisal(user);
//		performation.getRankScore(user);
		// TODO Auto-generated method stub

	}

}