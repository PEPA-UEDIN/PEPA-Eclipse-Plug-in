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
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.LTS;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.LTSBuilder;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.Partition;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.StateIsMarkedException;
import uk.ac.ed.inf.pepa.ctmc.derivation.aggregation.StateNotFoundException;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.CommonDefaulters;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.DefaultHashMap;
import uk.ac.ed.inf.pepa.ctmc.derivation.common.ISymbolGenerator;


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
	public Partition<S, PartitionBlock<S>> findPartition(LTS<S> initial) {
		Partition<S, PartitionBlock<S>> partition = initialPartition(initial);
		LinkedList<PartitionBlock<S>> splitters = new LinkedList<>(partition.getBlocks());
		LinkedList<PartitionBlock<S>> touchedBlocks = new LinkedList<>();
		DefaultHashMap<S, Double> weights = new DefaultHashMap<>(
				new CommonDefaulters.Basic<Double>(0.0d)
		);
		
		LTS<S> ltsView = initial.variantView();
		
		while (!splitters.isEmpty()) {
			PartitionBlock<S> splitter = splitters.pollFirst();
			System.err.println("Using: " + splitter.toString() + " as splitter.");
			splitter.usingAsSplitter();
			
			
			HashMap<S, HashMap<Short, HashSet<S>>> preIm = new HashMap<>();
			
			//HashMap<S, HashMap<Short, HashSet<S>>> preIm = new HashMap<>();
			HashSet<Short> allActions = computeAllPreimages(ltsView, splitter, preIm);
			
			System.err.println("preimages computed:\n" + preIm.toString());
			for (short act: allActions) {
				System.err.println("Partitioning for action: " + act);
				ArrayList<S> seenStates = computeWeights(ltsView, weights, splitter, preIm, act);
				System.err.println("Weights are: " + weights.toString());
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
	public LTS<Aggregated<S>> aggregateLts(
			LTS<S> initial,
			Partition<S, PartitionBlock<S>> partition) {
		
		// TODO: move this as default implementation in the interface.
		
		final int numActions = initial.numberOfActionTypes();
		List<Aggregated<S>> aggrLtsStates = new ArrayList<>(partition.size());
		HashMap<S, HashMap<S, double[]>> aggrTrans = new HashMap<S, HashMap<S, double[]>>();
		HashMap<PartitionBlock<S>, Aggregated<S>> blocksToAggr = new HashMap<PartitionBlock<S>, Aggregated<S>>(partition.size());
		
		for (PartitionBlock<S> block: partition.getBlocks()) {
			Aggregated<S> aggrState = new Aggregated<>(block);
			aggrLtsStates.add(aggrState);
			aggrTrans.put(aggrState.getRepresentative(), new HashMap<S, double[]>());
			blocksToAggr.put(block, aggrState);
		}
		
		prepareAggregatedData(initial, partition, numActions, aggrLtsStates, aggrTrans, blocksToAggr);
		 
		return makeAggregatedLts(partition, numActions, aggrLtsStates, aggrTrans, blocksToAggr);
	}

	@Override
	public LTS<Aggregated<S>> aggregate(LTS<S> initial) {
		return aggregateLts(initial, findPartition(initial));
	}

	/**
	 * @param partition
	 * @param numActions
	 * @param aggrLtsStates
	 * @param aggrTrans
	 * @param blocksToAggr
	 * @return
	 */
	private LTS<Aggregated<S>> makeAggregatedLts(Partition<S, PartitionBlock<S>> partition,
			final int numActions, List<Aggregated<S>> aggrLtsStates, HashMap<S, HashMap<S, double[]>> aggrTrans,
			HashMap<PartitionBlock<S>, Aggregated<S>> blocksToAggr) {
		LTSBuilder<Aggregated<S>> aggrLts = new LtsModel<>(numActions);

		
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
		return aggrLts.getLts();
	}


	/**
	 * @param initial
	 * @param partition
	 * @param numActions
	 * @param aggrLtsStates
	 * @param aggrTrans
	 * @param blocksToAggr
	 */
	private void prepareAggregatedData(LTS<S> initial, Partition<S, PartitionBlock<S>> partition,
			final int numActions, List<Aggregated<S>> aggrLtsStates, HashMap<S, HashMap<S, double[]>> aggrTrans,
			HashMap<PartitionBlock<S>, Aggregated<S>> blocksToAggr) {
		for (Aggregated<S> aggrState: aggrLtsStates) {
			S aggrSRepr = aggrState.getRepresentative();

			for (S state: aggrState) {
				
				for (S target: initial.getImage(state)) {
					PartitionBlock<S> b = partition.getBlockOf(target);
					Aggregated<S> targetAggr = blocksToAggr.get(b);
					S targetRepr = targetAggr.getRepresentative();
					HashMap<S, double[]> trans = aggrTrans.get(aggrSRepr);
					double[] rates = trans.get(targetRepr);
					for (short act: initial.getActions(state, target)) {
						if (rates == null) {
							rates = new double[numActions];
							trans.put(targetRepr, rates);
						}
						
						rates[act] += initial.getApparentRate(state, target, act);
					}
				}
			}
		}
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
		
		System.err.println("splitting block: " + block.toString());
		PartitionBlock<S> markedBlock = block.splitMarkedStates();
		System.err.println("marked states: " + markedBlock.toString());
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
		System.err.println("nonPmcBlock: " + nonPmcBlock.toString());
		System.err.println("Pmc block:" + markedBlock.toString());
		Collection<PartitionBlock<S>> subBlocks = nonPmcBlock.isEmpty()
				? new ArrayList<PartitionBlock<S>>()
				: nonPmcBlock.splitBlock();
		System.err.println("subBlocks: " + subBlocks.toString());
		ArrayList<PartitionBlock<S>> interestingBlocks = new ArrayList<PartitionBlock<S>>(2 + subBlocks.size());
		
		interestingBlocks.addAll(subBlocks);
		if (block == markedBlock) {
			partition.updateWithSplit(interestingBlocks);
			interestingBlocks.add(markedBlock);
		} else {
			interestingBlocks.add(markedBlock);
			partition.updateWithSplit(interestingBlocks);
		}
		
		if (block.wasUsedAsSplitter()) {
			System.err.println("Block was used as a splitter");
			// In this case it is safe to avoid using one
			// of the subblocks as a splitter.
			// we then remove the biggest one.
			if (block != markedBlock) {
				interestingBlocks.add(block);
			}

			PartitionBlock<S> fatBlock = Collections.max(
					interestingBlocks,
					new Comparator<PartitionBlock<S>>() {

						@Override
						public int compare(PartitionBlock<S> o1, PartitionBlock<S> o2) {
							return o1.size() - o2.size();
						}
						
					}
			);
			System.err.println("fatBlock is: " + fatBlock.toString());
			interestingBlocks.remove(fatBlock);
		} else if (markedBlock == block) {
			System.err.println("Removing block");
			interestingBlocks.remove(block);
		}
		
		for (PartitionBlock<S> b: interestingBlocks) {
			b.toBeUsedAsSplitter();
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
			final LTS<S> initial,
			DefaultHashMap<S, Double> weights,
			final PartitionBlock<S> splitter,
			final HashMap<S, HashMap<Short, HashSet<S>>> preIm,
			final short act) {
		ArrayList<S> seenStates = new ArrayList<>();
		for (S state : splitter) {
			HashMap<Short, HashSet<S>> trans = preIm.get(state);
			if (trans == null) {
				continue;
			} 
			HashSet<S> tStates = trans.get(act);
			if (tStates == null) {
				continue;
			}
			
			for (S source : tStates) {
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
			final LTS<S> lts,
			final PartitionBlock<S> splitter,
			HashMap<S, HashMap<Short, HashSet<S>>> preIm) {
		HashSet<Short> allActions = new HashSet<>();
		
		for (S state: splitter) {
			for (S source: lts.getPreImage(state)) {
				for (short act: lts.getActions(source, state)) {
					allActions.add(act);

					HashMap<Short, HashSet<S>> trans = preIm.get(state);
					if (trans == null) {
						trans = new HashMap<>();
						preIm.put(state, trans);
					}
					HashSet<S> tStates = trans.get(act);
					if (tStates == null) {
						tStates = new HashSet<>();
						trans.put(act, tStates);
					}
					
					tStates.add(source);
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
	public Partition<S, PartitionBlock<S>> initialPartition(LTS<S> initial) {
		PartitionBlock<S> p = new ArrayPartitionBlock<>();
		Partition<S, PartitionBlock<S>> partition = new Partition<>();
		
		for (S state: initial) {
			p.addState(state);
		}
		partition.addBlock(p);
		
		return partition;
	}
}
