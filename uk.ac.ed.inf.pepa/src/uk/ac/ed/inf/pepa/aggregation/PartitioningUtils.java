/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;


/**
 * @author Giacomo Alzetta
 *
 */
public final class PartitioningUtils {
	
	/**
	 * Make the class non-instantiable
	 */
	private PartitioningUtils() {}

	/**
	 * Compute the possible majority candidate of a sequence of values.
	 * 
	 * A possible majority candidate for a list of values X is the most common
	 * value if there is a value that appears in at list 50% of the positions,
	 * otherwise any value at all contained in the list.
	 */
	public static <V> V pmc(List<V> values) {
		int count = 0;
		V current = null;
		for (V val: values) {
			if (current == null) current = val;
			
			if (val.equals(current)) ++count;
			else 					 --count;
			
			if (count == 0) {
				current = val;
				count = 1;
			}
		}
		
		return current;
	}
	
	/**
	 * Separates the elements of the map that
	 * @param mapping
	 * @param pivot
	 * @return
	 */
	public static <K, V> HashMap<K, V> splitMapOnValue(HashMap<K, V> mapping, V pivot) {
		HashMap<K, V> newMap = new HashMap<K, V>();
		
		Iterator<Map.Entry<K, V>> iterator = mapping.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<K, V> entry = iterator.next();
			if (entry.getValue().equals(pivot)) {
				iterator.remove();
				newMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		return newMap;
	}
}
