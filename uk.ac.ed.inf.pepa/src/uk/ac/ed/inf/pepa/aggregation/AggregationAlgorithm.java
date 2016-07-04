package uk.ac.ed.inf.pepa.aggregation;


/**
 * Interface for aggregation algorithms.
 * 
 * @author Giacomo Alzetta
 *
 */
public interface AggregationAlgorithm<S extends Comparable<S>> {

	public LabelledTransitionSystem<Aggregated<S>> aggregate(LabelledTransitionSystem<S> initial);
}
