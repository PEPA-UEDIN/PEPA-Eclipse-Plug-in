package uk.ac.ed.inf.pepa.ctmc.derivation.aggregation;


/**
 * Interface for aggregation algorithms.
 * 
 * @author Giacomo Alzetta
 *
 */
public interface AggregationAlgorithm<S extends Comparable<S>> {
	
	/**
	 * Compute the partition using the algorithm.
	 * 
	 * @param initial
	 * @return
	 */
	public Partition<S, PartitionBlock<S>> findPartition(
			LTS<S> initial);
	
	/**
	 * Given an LTS and a partition obtain an aggregated LTS.
	 * 
	 * @param initial
	 * @param partition
	 * @return
	 */
	public LTS<Aggregated<S>> aggregateLts(
			LTS<S> initial,
			Partition<S, PartitionBlock<S>> partition);

	/**
	 * Aggregate the LTS using the algorithm.
	 * 
	 * @param initial
	 * @return
	 */
	public LTS<Aggregated<S>> aggregate(LTS<S> initial);
}
