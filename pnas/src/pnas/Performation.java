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

	HashMap<String, HashMap<String, Integer>> removelinksmap;
	ConcurrentHashMap<String, HashMap<String, Float>> reommdermap;
	ConcurrentHashMap<String, List<Map.Entry<String, Float>>> topn;
	ConcurrentHashMap<String, HashMap<Float, Float>> indexmap;
	ConcurrentHashMap<String,Float> ps;
	public Performation(float lambda)
			throws IOException {
		this.lambda = lambda;
		this.getRcommder();
		this.topn = new ConcurrentHashMap<String, List<Entry<String, Float>>>();
		this.indexmap = new ConcurrentHashMap<String, HashMap<Float, Float>>();
		this.ps = new ConcurrentHashMap<String, Float>();
		// TODO Auto-generated constructor stub
	}

	public void getRcommder() throws IOException {
		// long a = System.currentTimeMillis();
		// System.out.println("start is " + String.valueOf(a));
		// TODO Auto-generated method stub
		float lambad = this.lambda;
		ConcurrentHashMap<String, HashMap<String, Float>> reommdermap = new ConcurrentHashMap<String, HashMap<String, Float>>();
		Recommder recommder = new Recommder(lambad);
		this.itemsusers = recommder.itemsusers;
		this.removelinksmap = recommder.removelinksmap;
		this.usersitems = recommder.usersitems;
		ExecutorService pool2 = Executors.newFixedThreadPool(100);
		Set<String> users = recommder.removelinksmap.keySet();
		for (String user : users) {
			pool2.execute(new Mythread(user, recommder, reommdermap));
		}
		pool2.shutdown();
		while (true) {
			if (pool2.isTerminated()) {
				// System.out.println("ok!");
				// System.out.println("cost seconds is "
				// + String.valueOf(System.currentTimeMillis() - a));
				// System.out.println(reommdermap);
				this.reommdermap = reommdermap;
				break;
			}
		}

	}

	public void Sortrecommder(String user) {
		HashMap<String, Float> sortitem = this.reommdermap.get(user);
		// System.out.println(sortitem);
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

	public void indexreommder(String user) {
		List<Entry<String, Float>> usertopn = this.topn.get(user);
		HashMap<Float, Float> indexmap = new HashMap<Float, Float>();
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
		this.indexmap.put( user,indexmap);
	}

	public void getUserPr(String user, int n) {
		HashSet<String> hitset = new HashSet<String>();
		for (int i = 0; i < n; i++) {
			Entry<String, Float> item = this.topn.get(user).get(i);
			hitset.add(item.getKey());
		}
		hitset.retainAll(this.removelinksmap.keySet());
		float p = (float) hitset.size() / n;
		this.ps.put(user, p);
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Performation performation = new Performation(0f);
		ExecutorService pool3 = Executors.newFixedThreadPool(100);
		for(String user:performation.removelinksmap.keySet()){
			System.out.println(user);
			pool3.execute(new Mythread2(performation, user, 20));
		}
		pool3.shutdown();
		while(true){
			if (pool3.isTerminated()){
				System.out.println(performation.ps);
				break;
			}
		}
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
		// TODO Auto-generated method stub

	}

}