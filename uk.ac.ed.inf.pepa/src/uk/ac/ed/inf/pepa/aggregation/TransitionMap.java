package uk.ac.ed.inf.pepa.aggregation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import uk.ac.ed.inf.pepa.ctmc.derivation.common.State;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.Transition;

public class TransitionMap {

	private HashMap<Integer, HashMap<Integer, Transition>> transitions;
	
	public TransitionMap() {
		transitions = new HashMap<>();
	}
	
	public TransitionMap(int initialCapacity) {
		transitions = new HashMap<>(initialCapacity);
	}
	
	public Transition put(Integer source, Integer target,
			Transition val) {
		HashMap<Integer, Transition> temp = transitions.get(source);
		if (temp == null) {
			temp = new HashMap<>();
			transitions.put(source, temp);
		}
		return temp.put(target, val);
	}
	
	public Transition get(short[] source, short[] target) {
		HashMap<short[], Transition> temp = transitions.get(source);
		if (temp != null) {
			return temp.get(target);
		}
		return null;
	}
	
	public List<Transition> getAll(short[] source) {
		HashMap<short[], Transition> ts = transitions.get(source);
		if (ts == null) {
			return new ArrayList<>();
		}
		
		List<Transition> all = new ArrayList<>(ts.values());
		return all;	
	}
	
	public boolean containsKey(short[] source) {
		return transitions.containsKey(source);
	}
	
	public boolean containsKey(short[] source, short[] target) {
		HashMap<short[], Transition> temp = transitions.get(source);
		if (temp != null) {
			return temp.containsKey(target);
		}
		return false;
	}
}
