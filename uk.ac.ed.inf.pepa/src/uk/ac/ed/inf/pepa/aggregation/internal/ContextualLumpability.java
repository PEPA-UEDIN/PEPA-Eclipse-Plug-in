/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import uk.ac.ed.inf.pepa.aggregation.Aggregated;
import uk.ac.ed.inf.pepa.aggregation.AggregationAlgorithm;
import uk.ac.ed.inf.pepa.aggregation.LabelledTransitionSystem;
import uk.ac.ed.inf.pepa.aggregation.Partition;
import uk.ac.ed.inf.pepa.aggregation.PartitionBlock;
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
		ArrayList<Double> weights = new ArrayList<>();
		
		while (!splitters.isEmpty()) {
			PartitionBlock<S> splitter = splitters.pollFirst();
			HashMap<S, HashMap<Short, HashSet<S>>> preIm = new HashMap<>();
			HashSet<Short> allActions = new HashSet<>();
			
			for (S state: splitter) {
				List<S> splitPreIm = initial.getPreImage(state);
				for (S s: splitPreIm) {
					HashSet<Short> curActs = initial.getActions(s, state);
					allActions.addAll(curActs);
					for (Short act : curActs) {
						// TODO: checks for null
						preIm.get(state).get(act).add(s);
					}
				}
			}
			
			for (short act: allActions) {
				ArrayList<S> seenStates = new ArrayList<>();
				for (S state : splitter) {
					for (S source : preIm.get(state).get(act)) {
						// sum weights
					}
				}
				
				for (S state: seenStates) {
					// only if it has a weight != 0.
					PartitionBlock<S> block = partition.getBlockOf(state);
					if (!block.getMarkedStates().hasNext()) {
						touchedBlocks.add(block);
					}
					block.markState(state);
				}
				
				while (!touchedBlocks.isEmpty()) {
					PartitionBlock<S> block = touchedBlocks.pollFirst();
					PartitionBlock<S> markedBlock = block.splitMarkedStates();
					
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
