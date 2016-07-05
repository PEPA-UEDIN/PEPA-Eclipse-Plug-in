package uk.ac.ed.inf.pepa.ctmc.derivation.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * A class that provides common defaulters to be used with the
 * DefaultHashMap class.
 * 
 * @author Giacomo Alzetta
 *
 */
public class CommonDefaulters {

	/**
	 * A defaulter that returns a new empty ArrayList as default value.
	 * 
	 * @author Giacomo Alzetta
	 *
	 * @param <V> The type of the elements of the array.
	 */
	static public class ArrayListDefaulter<V> implements Defaulter<ArrayList<V>> {
		
		private int initialCapacity;
		
		public ArrayListDefaulter(int capacity) {
			initialCapacity = capacity;
		}
		
		public ArrayListDefaulter() {
			initialCapacity = 0;
		}
		
		public ArrayList<V> getDefault() {
			return new ArrayList<>(initialCapacity);
		}
	}
	
	/**
	 * A defaulter that returns a new empty HashMap as default value.
	 * 
	 * @author Giacomo Alzetta
	 *
	 * @param <K> The type of the keys of the HashMap
	 * @param <V> The type of the values of the HashMap
	 */
	static public class HashMapDefaulter<K, V> implements Defaulter<HashMap<K, V>> {
		
		private int initialCapacity;
		
		public HashMapDefaulter(int capacity) {
			initialCapacity = capacity;
		}
		
		public HashMapDefaulter() {
			initialCapacity = 0;
		}
		
		public HashMap<K, V> getDefault() {
			return new HashMap<>(initialCapacity);
		}
	}
	
	/**
	 * A defaulter that returns a new empty HashSet as default value.
	 * 
	 * @author Giacomo Alzetta
	 *
	 * @param <V> The type of the elements of the HashSet.
	 */
	static public class HashSetDefaulter<V> implements Defaulter<HashSet<V>> {
		
		private int initialCapacity;
		
		public HashSetDefaulter(int capacity) {
			initialCapacity = capacity;
		}
		
		public HashSetDefaulter() {
			initialCapacity = 0;
		}
		
		public HashSet<V> getDefault() {
			return new HashSet<>(initialCapacity);
		}
	}
}
