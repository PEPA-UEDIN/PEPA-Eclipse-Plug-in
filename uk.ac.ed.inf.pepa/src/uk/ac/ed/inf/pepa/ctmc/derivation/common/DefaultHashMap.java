/**
 * 
 */
package uk.ac.ed.inf.pepa.ctmc.derivation.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An HashMap class that automatically creates default values
 * when they are not present.
 * 
 * 
 * @author Giacomo Alzetta
 *
 */
public class DefaultHashMap<K, V, T extends Defaulter<V>> implements Map<K, V> {

	private HashMap<K, V> map;
	private T defaulter;
	
	public DefaultHashMap(T defaulter, int initialCapacity) {
		this.defaulter = defaulter;
		map = new HashMap<>(initialCapacity);
	}
	
	public DefaultHashMap(T defaulter) {
		this(defaulter, 0);
	}
	
	@Override
	public V get(Object key) {
		
		V val = map.get(key);
		if (val == null) {
			val = defaulter.getDefault();
			map.put((K) key, val);
		}
		return val;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return map.entrySet();
	}
}
