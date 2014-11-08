package pnas;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;

import sun.font.EAttribute;

public class TranslateMartix {
	HashMap<String, HashMap<String, Integer>> usersitems;
	HashMap<String, HashMap<String, Integer>> itemsusers;

	public TranslateMartix(
			HashMap<String, HashMap<String, HashMap<String, Integer>>> linksmap) {
		this.usersitems = linksmap.get("usersitems");
		this.itemsusers = linksmap.get("itemsusers");
	}

	public HashMap<String, Float> getOneItemW(String testitem, float lambda) {
		HashMap<String, Float> itemw = new HashMap<String, Float>();
		for (String compareitem : this.itemsusers.keySet()) {
			float sigma = 0f;
			Set<String> testitemlinkusers = this.itemsusers.get(testitem)
					.keySet();
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
	public void getSecondOrderwmartix(String key,
			ConcurrentHashMap<String, HashMap<String, Float>> wmartix,
			ConcurrentHashMap<String, HashMap<String, Float>> secondwmartix) {
		secondwmartix.put(key, (HashMap<String, Float>) wmartix.get(key)
				.clone());
		for (String sonkey : wmartix.get(key).keySet()) {
			HashMap<String, Float> sonhashmap = wmartix.get(sonkey);
			for (String item : sonhashmap.keySet()) {
				float w = sonhashmap.get(item) * wmartix.get(key).get(sonkey);
				if (wmartix.get(key).containsKey(item)) {
					secondwmartix.get(key).put(item,
							wmartix.get(key).get(item) * (1 + w));
				} else {
					secondwmartix.get(key).put(item, w);
				}
			}
		}

	}

}

class RunWmartix implements Runnable {
	TranslateMartix translatemartix;
	String item;
	float lambda;
	ConcurrentHashMap<String, HashMap<String, Float>> wmartix;

	public RunWmartix(TranslateMartix translatemartix, String item,
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

class RunSecondWmartix implements Runnable {
	ConcurrentHashMap<String, HashMap<String, Float>> wmartix;
	ConcurrentHashMap<String, HashMap<String, Float>> secondwmartix;
	TranslateMartix translatemartix;
	String key;

	RunSecondWmartix(String key,
			ConcurrentHashMap<String, HashMap<String, Float>> wmartix,
			ConcurrentHashMap<String, HashMap<String, Float>> secondwmartix,
			TranslateMartix translatemartix) {
		this.wmartix = wmartix;
		this.secondwmartix = secondwmartix;
		this.translatemartix = translatemartix;
		this.key = key;

	}
	public void run() {
		this.translatemartix.getSecondOrderwmartix(this.key,this.wmartix,
				this.secondwmartix);
	}
}
