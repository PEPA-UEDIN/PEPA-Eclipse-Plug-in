package uk.ac.ed.inf.pepa.ctmc.derivation.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CommonDefaulters {

	class ArrayListDefaulter<V> implements Defaulter<ArrayList<V>> {
		
		public ArrayList<V> getDefault() {
			return new ArrayList<>();
		}
	}
	
	class HashMapDefaulter<K, V> implements Defaulter<HashMap<K, V>> {
		
		public HashMap<K, V> getDefault() {
			return new HashMap<>();
		}
	}
	
	class HashSetDefaulter<V> implements Defaulter<HashSet<V>> {
		
		public HashSet<V> getDefault() {
			return new HashSet<>();
		}
	}
}
