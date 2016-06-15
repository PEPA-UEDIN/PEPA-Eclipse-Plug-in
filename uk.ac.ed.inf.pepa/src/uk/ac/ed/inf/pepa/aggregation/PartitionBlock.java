/**
 * 
 */
package uk.ac.ed.inf.pepa.aggregation;

import java.util.Iterator;

import uk.ac.ed.inf.pepa.model.Rate;


/**
 * An interface that represents blocks of a partition refinement data structure
 * of a state space.
 * 
 * Each block of the partition contains states that can be marked or not.
 * This class provides operations to mark states and also the ability to set
 * a value for them. It also provides operations that partition the states of this block
 * in multiple blocks depending on whether the states where marked or not or on the value
 * assigned to each state.
 * 
 * Note that the type T is represents a generic state while the type
 * V represents a generic value assigned to a given state.
 * 
 * @author Giacomo Alzetta
 */
public interface PartitionBlock<T, V extends Rate> {
	
	/**
	 * Add a non-marked state to the block.
	 * 
	 * @param state
	 */
	public void addState(T state);
	
	/**
	 * True if this block of the partition doesn't contain any state.
	 * 
	 * @return True if the block is empty, False otherwise.
	 */
	public boolean isEmpty();
	
	/**
	 * Iterate over all the states contained in this block of the partition.
	 * 
	 * @return
	 */
	public Iterator<T> getStates();
	
	/**
	 * Iterate over all the states that were marked in this block.
	 * @return
	 */
	public Iterator<T> getMarkedStates();
	
	/**
	 * Splits the block into two blocks: one containing the marked states and one
	 * containing the non-marked states.
	 * 
	 * Note that the block containing the marked states is returned while the current
	 * instance is modified.
	 * 
	 * @return
	 */
	public PartitionBlock<T, V> splitMarkedStates();
	
	/**
	 * Splits the block into a certain number of blocks grouping together states depending
	 * on the value V associated with them.
	 * 
	 * @return
	 */
	public Iterator<PartitionBlock<T, V>> splitBlock();
	
	/**
	 * Marks the given state in the block.
	 * If the state is already marked does nothing.
	 * Raises a <code>StateNotFoundException</code> if the state
	 * could not be found.
	 * 
	 * @param state	A state of the block.
	 */
	public void markState(T state) throws StateNotFoundException;
	
	/**
	 * Return true if the given state is a marked state in the block.
	 * 
	 * @param state a state of the block.
	 * @return
	 */
	public boolean isMarked(T state) throws StateNotFoundException;
	
	/**
	 * Sets the value associated with a state.
	 * The state should <b>not</b> be a marked state.
	 * 
	 * @param state A state of the block
	 * @param value The value to be associated with this state.
	 */
	public void setValue(T state, V value) throws StateNotFoundException, StateIsMarkedException;
	
	/**
	 * Return the value associated with a state.
	 * The state should <b>not</b> be a marked state.
	 * 
	 * @param state A state of the block
	 * @return The value associated with the state or <code>null</code> if no value was set.
	 */
	public V getValue(T state) throws StateNotFoundException, StateIsMarkedException;
	
}
