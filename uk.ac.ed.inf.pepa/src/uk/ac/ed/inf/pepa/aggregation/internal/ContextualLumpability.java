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
		DefaultHashMap<S, Double> weights = new DefaultHashMap<>(
				new CommonDefaulters.Basic<Double>(0.0d)
		);
		
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
			HashSet<Short> allActions = computeAllPreimages(initial, splitter, preIm);
			
			for (short act: allActions) {
				ArrayList<S> seenStates = computeWeights(initial, weights, splitter, preIm, act);
				
				markVisitedStates(partition, touchedBlocks, seenStates);
				
				while (!touchedBlocks.isEmpty()) {
					PartitionBlock<S> block = touchedBlocks.pollFirst();
					performSplitting(partition, splitters, weights, block);
				}
				
				weights.clear();
			}
		}
		
		// Build the aggregated LTS.
		return null;
	}

	/**
	 * Split a block into its sub-blocks based on the marked states
	 * and the weights provided.
	 * 
	 * @param partition
	 * @param splitters
	 * @param weights
	 * @param block
	 */
	private void performSplitting(
			Partition<S, PartitionBlock<S>> partition,
			LinkedList<PartitionBlock<S>> splitters,
			DefaultHashMap<S, Double> weights,
			PartitionBlock<S> block) {
		PartitionBlock<S> markedBlock = block.splitMarkedStates();
		
		List<Double> allWeights = new ArrayList<>(weights.values());
		Double pmc = PartitioningUtils.pmc(allWeights);
		//HashMap<S, Double> toPmc = PartitioningUtils.splitMapOnValue(weights, pmc);
		
		PartitionBlock<S> nonPmcBlock = markedBlock.splitBlockOnValue(pmc);
		Collection<PartitionBlock<S>> subBlocks;
		
		if (!nonPmcBlock.isEmpty()) {
			subBlocks = nonPmcBlock.splitBlock();
		} else {
			subBlocks = new ArrayList<>();
		}
		
		ArrayList<PartitionBlock<S>> interestingBlocks = new ArrayList<>(2 + subBlocks.size());
		if (!block.isEmpty()) {
			interestingBlocks.add(block);
		}
		
		interestingBlocks.add(markedBlock);
		interestingBlocks.addAll(subBlocks);
		
		partition.updateWithSplit(interestingBlocks);
		
		if (block.wasUsedAsSplitter()) {
			// In this case it is safe to avoid using one
			// of the subblocks as a splitter.
			// we then remove the biggest one.
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

	/**
	 * Marks all visited states.
	 * 
	 * @param partition
	 * @param touchedBlocks
	 * @param seenStates
	 */
	private void markVisitedStates(
			Partition<S, PartitionBlock<S>> partition,
			LinkedList<PartitionBlock<S>> touchedBlocks,
			final ArrayList<S> seenStates) {
		for (S state: seenStates) {
			// FIXME: only if it has a weight != 0.
			PartitionBlock<S> block = partition.getBlockOf(state);
			if (!block.hasMarkedStates()) {
				touchedBlocks.add(block);
			}
			try {
				block.markState(state);
			} catch (StateNotFoundException e) {
				// This should never happen.
				e.printStackTrace();
			}
		}
	}

	/**
	 * Compute the weights of the transitions with action act that reach
	 * the splitter.
	 * 
	 * @param initial
	 * @param weights The map of weights that will be updated by the method.
	 * @param splitter The splitter block.
	 * @param preIm  The map that stores the preimage of splitter.
	 * @param act
	 * @return The states that have transitions with action act to the splitter.
	 */
	private ArrayList<S> computeWeights(
			final LabelledTransitionSystem<S> initial,
			DefaultHashMap<S, Double> weights,
			final PartitionBlock<S> splitter,
			final DefaultHashMap<S, DefaultHashMap<Short, HashSet<S>>> preIm,
			final short act) {
		ArrayList<S> seenStates = new ArrayList<>();
		for (S state : splitter) {
			for (S source : preIm.get(state).get(act)) {
				if (weights.containsKey(source)) {
					seenStates.add(source);
				}
				double w = weights.get(source);
				w += initial.getApparentRate(source, state, act);
				weights.put(source, w);
			}
		}
		return seenStates;
	}

	/**
	 * Computes the preimages of a splitter block and returns the
	 * set of actions seen in the preimages.
	 * 
	 * @param lts
	 * @param splitter The block of the LTS currently considered as a splitter.
	 * @param preIm The map that will be updated with the preimage of splitter.
	 * @return The set of actions such that have transitions to the splitter.
	 */
	private HashSet<Short> computeAllPreimages(
			final LabelledTransitionSystem<S> lts,
			final PartitionBlock<S> splitter,
			DefaultHashMap<S, DefaultHashMap<Short, HashSet<S>>> preIm) {
		HashSet<Short> allActions = new HashSet<>();
		
		for (S state: splitter) {
			for (S source: lts.getPreImage(state)) {
				Set<Short> curActs = lts.getActions(source, state);
				allActions.addAll(curActs);
				for (Short act : curActs) {
					preIm.get(state).get(act).add(source);
				}
			}
		}
		return allActions;
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
