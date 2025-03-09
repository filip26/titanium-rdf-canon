package com.apicatalog.rdf.canon;

/**
 * Functional interface that enables controlled interruption of the RDF dataset
 * canonicalization process based on user-defined criteria.
 * <p>
 * An implementation of {@code RdfCanonTicker} can be provided to monitor
 * progress or impose constraints such as timeouts, processing limits, or other
 * custom stopping conditions during canonicalization.
 * <p>
 * The {@link #tick()} method is invoked periodically during the execution of
 * {@link RdfCanon#provide(com.apicatalog.rdf.api.RdfQuadConsumer)}. If
 * {@code tick()} throws an {@link IllegalStateException}, the canonicalization
 * process will terminate prematurely.
 * </p>
 * <p>
 * This mechanism is useful for applications that require fine-grained control
 * over resource usage or need to abort processing based on external factors.
 * </p>
 */
@FunctionalInterface
public interface RdfCanonTicker {

    static final RdfCanonTicker EMPTY = () -> {};
     
    /**
     * Called periodically during RDF dataset canonicalization to determine whether
     * the process should continue.
     * <p>
     * Implementations should throw {@link IllegalStateException} to interrupt the
     * process when necessary (e.g., on timeout, exceeding a limit, or external
     * cancellation).
     *
     * @throws IllegalStateException if the canonicalization process should be
     *                               aborted.
     */
    void tick() throws IllegalStateException;
}