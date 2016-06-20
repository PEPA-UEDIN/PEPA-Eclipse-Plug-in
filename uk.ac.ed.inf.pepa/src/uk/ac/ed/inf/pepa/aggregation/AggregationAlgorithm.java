package uk.ac.ed.inf.pepa.aggregation;

import java.util.List;

import uk.ac.ed.inf.pepa.ctmc.derivation.IStateSpace;
import uk.ac.ed.inf.pepa.model.Rate;

/**
 * Interface for aggregation algorithms.
 * 
 * @author Giacomo Alzetta
 *
 */
public interface AggregationAlgorithm<T, V extends Rate> {

	public IStateSpace aggregate(IStateSpace initial);
}
