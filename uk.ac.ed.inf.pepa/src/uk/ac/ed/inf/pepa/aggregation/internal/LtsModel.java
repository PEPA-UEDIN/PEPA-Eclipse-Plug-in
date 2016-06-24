package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import uk.ac.ed.inf.pepa.aggregation.LabelledTransitionSystem;
import uk.ac.ed.inf.pepa.model.Activity;
import uk.ac.ed.inf.pepa.model.Model;
import uk.ac.ed.inf.pepa.model.NamedRate;

public class LtsModel<S> implements LabelledTransitionSystem<S> {

	private ArrayList<S> states;
	private HashMap<S, HashMap<S, Activity>> transitionMap;
	private HashMap<S, ArrayList<S>> preImageMap;
	
	public LtsModel() {
		states = new ArrayList<>();
		preImageMap = new HashMap<>();
		transitionMap = new HashMap<>();
	}
	
	@Override
	public boolean isValid() {
		return false;
	}
	
	@Override
	public int size() {
		return states.size();
	}

	@Override
	public NamedRate getApparentRate(S source, S target, String action) {
		return (NamedRate) transitionMap.get(source).get(target).getRate();
	}

	@Override
	public List<S> getImage(S source) {
		HashMap<S, Activity> targetsMap = transitionMap.get(source);
		ArrayList<S> targets = new ArrayList<>(targetsMap.size());
		for (S key: targetsMap.keySet()) {
			targets.add(key);
		}
		return targets;
		
	}

	@Override
	public List<S> getPreImage(S target) {
		return preImageMap.get(target);
	}

	@Override
	public boolean addState(S state) {
		states.add(state);
		transitionMap.put(state, new HashMap<>());
		preImageMap.put(state, new HashMap<>());
	}

	@Override
	public boolean addTransition(S source, S target, NamedRate rate) {
		HashMap<S, HashMap<S, Activity>> map = transitionMap.get(source);
		if (map == null) {
			map = new HashMap<>();
			transitionMap.put(source, map);
		}
		
	}

}
