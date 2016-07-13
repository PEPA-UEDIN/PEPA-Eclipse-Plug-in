/**
 * 
 */
package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.Aggregated;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.AggregationAlgorithm;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.LabelledTransitionSystem;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.Partition;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.StateIsMarkedException;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.StateNotFoundException;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.CommonDefaulters;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.DefaultHashMap;

// TODO: we have to handle TAU specially.
// we should do so in a protected method so that Exact Equivalence can reimplement it.
/**
 * @author Giacomo Alzetta
 *
 */
public class ContextualLumpability<S extends Comparable<S>> implements AggregationAlgorithm<S> {

	/**
	 * @param initial
	 * @return
	 */
	@Override
	public Partition<S, PartitionBlock<S>> findPartition(LabelledTransitionSystem<S> initial) {
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
		return partition;
	}
	

	/**
	 * Given a partition and a LTS computes the aggregated LTS corresponding
	 * to the given partition.
	 * 
	 * @param initial
	 * @param partition
	 * @return
	 */
	@Override
	public LabelledTransitionSystem<Aggregated<S>> aggregateLts(
			LabelledTransitionSystem<S> initial,
			Partition<S, PartitionBlock<S>> partition) {
		
		// TODO: move this as default implementation in the interface.
		
		final int numActions = initial.numberOfActionTypes();
		List<Aggregated<S>> aggrLtsStates = new ArrayList<>(partition.size());
		HashMap<S, HashMap<S, double[]>> aggrTrans = new HashMap<>();
		HashMap<PartitionBlock<S>, Aggregated<S>> blocksToAggr = new HashMap<>(partition.size());
		
		for (PartitionBlock<S> block: partition.getBlocks()) {
			Aggregated<S> aggrState = new Aggregated<>(block);
			aggrLtsStates.add(aggrState);
			aggrTrans.put(aggrState.getRepresentative(), new HashMap<>());
			blocksToAggr.put(block, aggrState);
		}
		
		for (Aggregated<S> aggrState: aggrLtsStates) {
			S aggrSRepr = aggrState.getRepresentative();
			for (S state: aggrState) {
				// FIXME: The LTS has a getApparentRate(source, target, act)!
				List<Aggregated<S>> imAggr = new ArrayList<>();
				for (S target: initial.getImage(state)) {
					imAggr.add(blocksToAggr.get(partition.getBlockOf(target)));
				}
				
				for (Aggregated<S> targetAggr: imAggr) {
					S targetSRepr = targetAggr.getRepresentative();
					double[] rates = new double[numActions];
					//Arrays.fill(rates, 0.0d);
					aggrTrans.get(aggrSRepr).put(targetSRepr, rates);
					
					for (S t: targetAggr) {
						Iterator<Short> acts = initial.getActions(state, t);
					
						while (acts.hasNext()) {
							short act = acts.next();
							double[] m = aggrTrans.get(aggrSRepr).get(targetSRepr);
							
							m[act] += initial.getApparentRate(state, t, act);
						}
					}
				}
				
				
			}
		}

		
		 
		LabelledTransitionSystem<Aggregated<S>> aggrLts = new LtsModel<>(numActions);

		
		for (Aggregated<S> aggrS: aggrLtsStates) {
			aggrLts.addState(aggrS);
		}
		
		for (S source: aggrTrans.keySet()) {
			HashMap<S, double[]> sourceImage = aggrTrans.get(source);
			for (S target: sourceImage.keySet()) {
				double[] targetMap = sourceImage.get(target);
				short act = 0;
				for (double value: targetMap) {
					if (value != 0.0d) {
						aggrLts.addTransition(
								blocksToAggr.get(partition.getBlockOf(source)),
								blocksToAggr.get(partition.getBlockOf(target)),
								value, act);
					}
					++act;
				}
			}
		}
		return aggrLts;
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
		if (block.isEmpty()) {
			markedBlock = markedBlock.shareIdentity(block);
		}
		
		assert !markedBlock.isEmpty() && !block.isEmpty();
		
		List<Double> allWeights = new ArrayList<>(markedBlock.size());
		for (S s: markedBlock) {
			allWeights.add(weights.get(s));
		}
		Double pmc = PartitioningUtils.pmc(allWeights);
		
		for (S s: markedBlock) {
			try {
			markedBlock.setValue(s, weights.get(s));
			} catch (StateIsMarkedException e) {
				e.printStackTrace();
			} catch (StateNotFoundException e) {
				e.printStackTrace();
			}
		} 
		PartitionBlock<S> nonPmcBlock = markedBlock.splitBlockOnValue(pmc);
		Collection<PartitionBlock<S>> subBlocks = nonPmcBlock.isEmpty()
				? new ArrayList<>()
				: nonPmcBlock.splitBlock();
		
		ArrayList<PartitionBlock<S>> interestingBlocks = new ArrayList<>(2 + subBlocks.size());
		if (markedBlock != block) interestingBlocks.add(markedBlock);
		interestingBlocks.addAll(subBlocks);
		
		partition.updateWithSplit(interestingBlocks);
		
		if (block.wasUsedAsSplitter() && !interestingBlocks.isEmpty()) {
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
		} else {
			// AFAIK block should never end up in interestingBlocks.
			boolean res = interestingBlocks.remove(block);
			assert !res;
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
				if (!weights.containsKey(source)) {
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
				Iterator<Short> curActs = lts.getActions(source, state);
				while (curActs.hasNext()) {
					Short act = curActs.next();
					allActions.add(act);
					preIm.get(state).get(act).add(source);

				}
			}
		}
		return allActions;
	}

	/**
	 * The initial partition for contextual lumpability is just a single
	 * block containing every state of the LTS.
	 * 
	 * @param initial The LTS to aggregate.
	 * @return A singleton partition containing all states.
	 */
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
