package com.apicatalog.rdf.canon;

/**
 * An implementation of {@link RdfCanonTicker} that monitors elapsed time in
 * milliseconds and interrupts the RDF canonicalization process if a specified
 * maximum duration is exceeded.
 * <p>
 * This ticker uses {@link System#currentTimeMillis()} to measure elapsed time.
 * It throws an {@link IllegalStateException} if the canonicalization process
 * runs longer than the allowed time limit.
 *
 * <p>
 * <strong>Example usage:</strong>
 * </p>
 * 
 * <pre>{@code
 * RdfCanonTicker ticker = new ElapsedTimeTicker(5000); // 5 seconds timeout
 * }</pre>
 *
 * @see RdfCanonTicker
 */
public final class RdfCanonTimeTicker implements RdfCanonTicker {

    /** Maximum allowed execution time in milliseconds. */
    private final long maxDurationMillis;

    /** Start time in milliseconds, initialized on the first {@code tick()} call. */
    private long startTimeMillis = 0;

    /**
     * Creates a new {@code ElapsedTimeTicker} with a specified timeout duration.
     *
     * @param maxDurationMillis the maximum allowed execution time in milliseconds;
     *                          must be positive
     * @throws IllegalArgumentException if {@code maxDurationMillis} is zero or
     *                                  negative
     */
    public RdfCanonTimeTicker(long maxDurationMillis) {
        if (maxDurationMillis <= 0) {
            throw new IllegalArgumentException("Maximum duration must be positive.");
        }
        this.maxDurationMillis = maxDurationMillis;
    }

    /**
     * Invoked periodically during RDF canonicalization to check if the maximum
     * execution time has been exceeded.
     *
     * @throws IllegalStateException if the allowed time limit has been reached
     */
    @Override
    public void tick() throws IllegalStateException {
        long currentTimeMillis = System.currentTimeMillis();

        if (startTimeMillis == 0) {
            startTimeMillis = currentTimeMillis;
            return;
        }

        long elapsedMillis = currentTimeMillis - startTimeMillis;

        if (elapsedMillis > maxDurationMillis) {
            throw new IllegalStateException(
                    "Maximum execution time of " + maxDurationMillis + " ms exceeded. " +
                            "Elapsed: " + elapsedMillis + " ms.");
        }
    }
}
