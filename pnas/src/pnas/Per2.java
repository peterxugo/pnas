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

public class Per2 {

	float lambda;
	String filename;
	HashMap<String, HashMap<String, Integer>> oldusersitems;
	HashMap<String, HashMap<String, Integer>> olditemsusers;
	HashMap<String, HashMap<String, Integer>> newusersitems;
	HashMap<String, HashMap<String, Integer>> newitemsusers;
	HashMap<String, HashMap<String, Integer>> removeusersitems;
	// ConcurrentHashMap<String,String[]> topn;
	ConcurrentHashMap<String, Float> rs;
	ConcurrentHashMap<String, Float> ps;
	ConcurrentHashMap<String, Float> is;
	ConcurrentHashMap<String, Float> hs;
	ConcurrentHashMap<String, HashMap<String, Float>> recommdermap;
	private HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap;
	private HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmap;

	Per2(float lambda,
			HashMap<String, HashMap<String, HashMap<String, Integer>>> oldlinksmap,
			HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap,
			HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmap) {
		this.lambda = lambda;
		this.newlinksmap = newlinksmap;
		this.removelinksmap = removelinksmap;
		this.olditemsusers = oldlinksmap.get("itemsusers");
		this.oldusersitems = oldlinksmap.get("usersitems");
		this.newitemsusers = newlinksmap.get("itemsusers");
		this.newusersitems = newlinksmap.get("usersitems");
		this.removeusersitems = removelinksmap.get("usersitems");
		this.rs = new ConcurrentHashMap<String, Float>();
		this.ps = new ConcurrentHashMap<String, Float>();
		this.is = new ConcurrentHashMap<String, Float>();
		this.hs = new ConcurrentHashMap<String, Float>();
		this.recommdermap = new ConcurrentHashMap<String, HashMap<String, Float>>();
//		this.getReommder(lambda2);

	}

	public void getReommder() {

		Recommder recommder = new Recommder(this.lambda, this.newlinksmap,
				this.removelinksmap);

		ExecutorService pool = Executors.newFixedThreadPool(100);
		for (String user : this.removeusersitems.keySet()) {
			if (this.newusersitems.containsKey(user)) {
				pool.execute(new Mythread(user, recommder, this.recommdermap));
			}
		}
		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				// System.out.println("recommdermap size is \t"
				// + this.recommdermap.size());
				break;
			}
		}

	}
	public void getReommder(float lambda2) {

		Recommder recommder = new Recommder(this.lambda, this.newlinksmap,
				this.removelinksmap);

		ExecutorService pool = Executors.newFixedThreadPool(100);
		for (String user : this.removeusersitems.keySet()) {
			if (this.newusersitems.containsKey(user)) {
				pool.execute(new Mythread(user, recommder, this.recommdermap,lambda2));
			}
		}
		pool.shutdown();
		while (true) {
			if (pool.isTerminated()) {
				// System.out.println("recommdermap size is \t"
				// + this.recommdermap.size());
				break;
			}
		}

	}
	public List<Entry<String, Float>> sortRecomdermap(String user) {
		HashMap<String, Float> userrecommderlist = this.recommdermap.get(user);
		List<Map.Entry<String, Float>> sortresult = new ArrayList<Map.Entry<String, Float>>(
				userrecommderlist.entrySet());
		Collections.sort(sortresult,
				new Comparator<Map.Entry<String, Float>>() {

					public int compare(Entry<String, Float> o1,
							Entry<String, Float> o2) {
						// TODO Auto-generated method stub
						if (o2.getValue() - o1.getValue() > 0) {
							return 1;
						} else if (o2.getValue() - o1.getValue() < 0) {
							return -1;
						}else{
							return 0;
						}
						
					}
				});
		return sortresult;

	}

	public HashMap<String, Float> getUserItemsIndex(
			List<Entry<String, Float>> sortresult) {
		HashMap<String, Float> itemindex = new HashMap<String, Float>();
		// System.out.println(sortresult);
		for (int i = 0; i < sortresult.size();) {
			float score = sortresult.get(i).getValue();
			int j = i + 1;

			while (true) {
				if (j >= sortresult.size()) {
					j--;
					break;
				}
				float nextsocre = sortresult.get(j).getValue();
				if (nextsocre == score) {
					j++;
				} else {
					j--;
					break;
				}
			}

			float index = (float) (i + j) / 2;
			while (i <= j) {
				String item = sortresult.get(i).getKey();
				itemindex.put(item, index);
				i++;
			}

		}
		return itemindex;

	}

	public float getUserRankScore(String user, HashMap<String, Float> itemindex) {
		Set<String> userremoveitems = this.removeusersitems.get(user).keySet();
		float indexcount = 0f;
		float index;
		int useritemcandidatesize = this.olditemsusers.size()
				- this.newusersitems.get(user).size();
		for (String item : userremoveitems) {
			if (itemindex.containsKey(item)) {
				index = itemindex.get(item);
			} else {
				index = (float) (this.olditemsusers.size()
						- this.newusersitems.get(user).size() + this.recommdermap
						.get(user).size()) / 2;
			}
			indexcount += index;
		}
		float rcount = indexcount / useritemcandidatesize;
		float ravrage = rcount / userremoveitems.size();
		this.rs.put(user, ravrage);
		return ravrage;
	}

	public Set<String> getTopn(int n, List<Entry<String, Float>> sortresult) {
		Set<String> usertopn = new HashSet<String>();
		for (int i = 0; i < n & i < sortresult.size(); i++) {
			usertopn.add(sortresult.get(i).getKey());
		}
		return usertopn;
	}
	public float getPresion(String user, Set<String> usertopn) {
		float p;
		Set<String> hit = new HashSet<String>();
		hit.addAll(usertopn);
		hit.retainAll(this.removeusersitems.get(user).keySet());
		p = (float) hit.size() / usertopn.size();
		this.ps.put(user, p);
		return p;
	}
	public double getSurprisal(String user, Set<String> topn) {
		double i = 0;
		double icount = 0;
		int usernum = this.oldusersitems.size();
		for (String item : topn) {
			float randomly_selected = (float) this.olditemsusers.get(item)
					.size() / usernum;
			i = Math.log(1 / randomly_selected) / Math.log(2);
			icount += i;
		}
		double iavrage = icount / topn.size();
		this.is.put(user, (float) iavrage);
		return iavrage;
	}

	public static void main(String[] agrs) throws IOException {

		String filename = "/source/newnetflix";
		CreateNetwork createnetwork = new CreateNetwork();
		ArrayList<String[]> links = createnetwork.getLinkList(filename);
		HashMap<String, HashMap<String, HashMap<String, Integer>>> oldlinksmap = createnetwork
				.mapLink(links);
		HashMap<String, ArrayList<String[]>> a = createnetwork.randomDel(links,
				0.1f);
		HashMap<String, HashMap<String, HashMap<String, Integer>>> newlinksmap = createnetwork
				.mapLink(a.get("newlinks"));
		HashMap<String, HashMap<String, HashMap<String, Integer>>> removelinksmap = createnetwork
				.mapLink(a.get("dellinks"));

		int n = 20;

		for (int k = 0; k < 20; k++) {
			float lambda = 0.05f*k;
			System.out.print(lambda+"\t");
			Per2 per = new Per2(lambda, oldlinksmap, newlinksmap,
					removelinksmap);
			per.getReommder();
			ExecutorService pool2 = Executors.newFixedThreadPool(100);
			for (String user : per.removeusersitems.keySet()) {
				if (per.recommdermap.get(user) == null) {
					continue;
				}
				pool2.execute(new PerRunnable(per, user, n));
			}
			pool2.shutdown();
			while (true) {
				if (pool2.isTerminated()) {
					// System.out.println("complete!");
					break;
				}
			}

			float rcount = 0;
			for (Float r : per.rs.values()) {
				rcount += r;
			}
			System.out.print(rcount / per.rs.size() + "\t");
			// averages rankscore

			float pcount = 0;
			for (float p : per.ps.values()) {
				pcount += p;
			}
			System.out.print(pcount / per.ps.size() + "\t");
			int removecount = 0;
			// averages precision

			for (String user : per.removeusersitems.keySet()) {
				removecount += per.removeusersitems.get(user).size();
			}
			int itemsnum = per.olditemsusers.size();
			int usersmun = per.oldusersitems.size();
			float p = pcount / per.ps.size();
			float ep = p * itemsnum * usersmun / removecount;
			System.out.print(ep + "\t");
			// averages ep

			float icount = 0;
			for (float i : per.is.values()) {
				icount += i;
			}
			System.out.println(icount / per.is.size());
			// averages i
		}
	}

}

class PerRunnable implements Runnable {
	Per2 per;
	String user;
	int n;
	PerRunnable(Per2 per, String user, int n) {
		this.per = per;
		this.user = user;
		this.n = n;
	}
	@Override
	public void run() {
		List<Entry<String, Float>> sortresult = per.sortRecomdermap(this.user);
		Set<String> topn = per.getTopn(this.n, sortresult);
		HashMap<String, Float> itemindex = per.getUserItemsIndex(sortresult);
		per.getUserRankScore(this.user, itemindex);
		per.getPresion(this.user, topn);
		per.getSurprisal(this.user, topn);

	}

}
