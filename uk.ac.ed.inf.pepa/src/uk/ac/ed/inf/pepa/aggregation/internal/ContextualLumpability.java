/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.ed.inf.pepa.aggregation.Aggregated;
import uk.ac.ed.inf.pepa.aggregation.AggregationAlgorithm;
import uk.ac.ed.inf.pepa.aggregation.LabelledTransitionSystem;
import uk.ac.ed.inf.pepa.aggregation.Partition;
import uk.ac.ed.inf.pepa.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.aggregation.StateNotFoundException;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.model.Rate;

/**
 * @author Giacomo Alzetta
 *
 */
public class ContextualLumpability<S extends Comparable<S>> implements AggregationAlgorithm<S> {

	@Override
	public LabelledTransitionSystem<Aggregated<S>> aggregate(LabelledTransitionSystem<S> initial) {
		Partition<S, PartitionBlock<S>> partition = initialPartition(initial);
		LinkedList<PartitionBlock<S>> splitters = new LinkedList<>(partition.getBlocks());
		LinkedList<PartitionBlock<S>> touchedBlocks = new LinkedList<>();
		HashMap<S, Double> weights = new HashMap<>();
		
		while (!splitters.isEmpty()) {
			PartitionBlock<S> splitter = splitters.pollFirst();
			HashMap<S, HashMap<Short, HashSet<S>>> preIm = new HashMap<>();
			HashSet<Short> allActions = new HashSet<>();
			
			for (S state: splitter) {
				List<S> splitPreIm = initial.getPreImage(state);
				for (S s: splitPreIm) {
					Set<Short> curActs = initial.getActions(s, state);
					allActions.addAll(curActs);
					for (Short act : curActs) {
						HashMap<Short, HashSet<S>> preImState = preIm.get(state);
						if (preImState == null) {
							preImState = new HashMap<>();
							preIm.put(state,  preImState);
						}
						HashSet<S> preImTargets = preImState.get(act);
						if (preImTargets == null) {
							preImTargets = new HashSet<>();
							preImState.put(act, preImTargets);
						}
						preImTargets.add(s);
					}
				}
			}
			
			for (short act: allActions) {
				weights.clear();
				
				ArrayList<S> seenStates = new ArrayList<>();
				for (S state : splitter) {
					for (S source : preIm.get(state).get(act)) {
						Double w = weights.get(source);
						if (w == null) {
							w = 0.0d;
							seenStates.add(source);
						}
						w += initial.getApparentRate(source, state, act);
						weights.put(source, w);
					}
				}
				
				for (S state: seenStates) {
					// only if it has a weight != 0.
					PartitionBlock<S> block = partition.getBlockOf(state);
					if (!block.getMarkedStates().hasNext()) {
						touchedBlocks.add(block);
					}
					try {
						block.markState(state);
					} catch (StateNotFoundException e) {
						e.printStackTrace();
					}
				}
				
				while (!touchedBlocks.isEmpty()) {
					PartitionBlock<S> block = touchedBlocks.pollFirst();
					PartitionBlock<S> markedBlock = block.splitMarkedStates();
					
					List<Double> allWeights = new ArrayList<>(weights.values());
					Double pmc = PartitioningUtils.pmc(allWeights);
					HashMap<S, Double> toPmc = PartitioningUtils.splitMapOnValue(weights, pmc);
					
				}
			}
			
			// compute pre-images
			// compute weights.
		}
		// TODO Auto-generated method stub
		return null;
	}

	public Partition<S, PartitionBlock<S>> initialPartition(LabelledTransitionSystem<S> initial) {
		PartitionBlock<S> p = new ArrayPartitionBlock<>();
		Partition<S, PartitionBlock<S>> partition = new Partition<>();
		
		for (S state: initial) {
			p.addState(state);
		}
		partition.addBlock(p);
		
		return partition;
	}

}
