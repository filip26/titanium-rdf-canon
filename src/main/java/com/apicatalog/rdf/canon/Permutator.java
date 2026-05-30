package com.apicatalog.rdf.canon;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterate over all possible permutations of an array.
 *
 */
final class Permutator implements Iterator<String[]> {

	/** The array we are permuting. */
	private final String[] array;

	/** Counts for Heap's algorithm. */
	private final int[] count;
	private final int length;

	/** Does another permutation exist?. */
	private boolean nextExists = true;

	/** State for Heap's algorithm. */
	private int state = 0;

	Permutator(String[] input) {
		this.array = input.clone();
		this.length = array.length;
		this.count = new int[length];
	}

	@Override
	public boolean hasNext() {
		return nextExists;
	}

	@Override
	public String[] next() {
		if (!nextExists) {
			throw new NoSuchElementException();
		}

		// Optimization: Use System.arraycopy instead of clone()
		// System.arraycopy is a native intrinsic method that is
		// optimized by the JVM into highly efficient block
		// memory moves, significantly outperforming clone() for
		// small to medium-sized arrays.
		var output = new String[length];
		System.arraycopy(array, 0, output, 0, length);

		while (state < length) {
			if (count[state] < state) {
				int swapIndex = (state % 2 == 0) ? 0 : count[state];

				var temp = array[swapIndex];
				array[swapIndex] = array[state];
				array[state] = temp;

				count[state]++;
				state = 0;
				return output;
			}

			count[state] = 0;
			state++;
		}

		nextExists = false;
		return output;
	}
}
