/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import uk.ac.ed.inf.pepa.ctmc.derivation.common.CommonDefaulters;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.DefaultHashMap;

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
		DefaultHashMap<S, Double> weights = new DefaultHashMap<>(new CommonDefaulters.Basic<Double>(0.0d));
		
		while (!splitters.isEmpty()) {
			PartitionBlock<S> splitter = splitters.pollFirst();
			splitter.usingAsSplitter();
			
			DefaultHashMap<S, DefaultHashMap<Short, HashSet<S>>> preIm =
					new DefaultHashMap<>(
							new CommonDefaulters.DefaultHashMapDefaulter<>(
									new CommonDefaulters.HashSetDefaulter<>()
					)
			);
			
			//HashMap<S, HashMap<Short, HashSet<S>>> preIm = new HashMap<>();
			HashSet<Short> allActions = new HashSet<>();
			
			for (S state: splitter) {
				List<S> splitPreIm = initial.getPreImage(state);
				for (S s: splitPreIm) {
					Set<Short> curActs = initial.getActions(s, state);
					allActions.addAll(curActs);
					for (Short act : curActs) {
						DefaultHashMap<Short, HashSet<S>> preImState = preIm.get(state);
						//if (preImState == null) {
						//	preImState = new HashMap<>();
						//	preIm.put(state,  preImState);
						//}
						HashSet<S> preImTargets = preImState.get(act);
						//if (preImTargets == null) {
						//	preImTargets = new HashSet<>();
						//	preImState.put(act, preImTargets);
						//}
						preImTargets.add(s);
					}
				}
			}
			
			for (short act: allActions) {
				weights.clear();
				
				ArrayList<S> seenStates = new ArrayList<>();
				for (S state : splitter) {
					for (S source : preIm.get(state).get(act)) {
						if (weights.containsKey(source)) {
							seenStates.add(source);
						}
						Double w = weights.get(source);
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
					
					PartitionBlock<S> nonPmcBlock = markedBlock.splitBlockOnValue(pmc);
					Collection<PartitionBlock<S>> subBlocks = nonPmcBlock.splitBlock();
					
					// TODO: check if here we are adding the right sub-blocks as splitters.
					// the original algorithm has a bunch of "X takes the identity of Y"...
					ArrayList<PartitionBlock<S>> interestingBlocks = new ArrayList<>();
					if (!block.isEmpty()) {
						interestingBlocks.add(block);
					}
					
					interestingBlocks.add(markedBlock);
					interestingBlocks.addAll(subBlocks);
					
					if (block.wasUsedAsSplitter()) {
						PartitionBlock<S> fatBlock = Collections.max(
								interestingBlocks,
								new Comparator<PartitionBlock<S>>() {

									@Override
									public int compare(PartitionBlock<S> o1, PartitionBlock<S> o2) {
										return o1.size() - o2.size();
									}
									
								}
						);
						
						interestingBlocks.remove(fatBlock);
					}
					splitters.addAll(interestingBlocks);
				}
			}
		}
		
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
