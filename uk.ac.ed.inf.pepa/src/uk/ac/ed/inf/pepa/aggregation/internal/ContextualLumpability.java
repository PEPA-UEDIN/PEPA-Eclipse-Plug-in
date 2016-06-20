/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation.internal;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.pepa.aggregation.AggregationAlgorithm;
import uk.ac.ed.inf.pepa.aggregation.PartitionBlock;
import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.model.Rate;

/**
 * @author Giacomo Alzetta
 *
 */
public class ContextualLumpability<T, V extends Rate> implements AggregationAlgorithm<T, V> {

	@Override
	public IStateSpace aggregate(IStateSpace initial) {
		List<PartitionBlock<T, V>> partition = initialPartition(initial);
		List<PartitionBlock<T, V>> splitters = new ArrayList<>(partition);
		List<PartitionBlock<T, V>> touchedBlocks = new ArrayList<>();
		ArrayList<V> weights = new ArrayList<>();
		
		while (!splitters.isEmpty()) {
			PartitionBlock<T, V> splitter = splitters.pop();
			// compute pre-images
			// compute weights.
		}
		// TODO Auto-generated method stub
		return null;
	}

	public List<PartitionBlock<T, V>> initialPartition(IStateSpace initial) {
		PartitionBlock<T, V> p = new ArrayPartitionBlock<>();
		ArrayList<PartitionBlock<T, V>> res = new ArrayList<>();
		res.add(p);
		return res;
	}

}
