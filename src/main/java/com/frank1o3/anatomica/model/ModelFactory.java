package com.frank1o3.anatomica.model;

/**
 * Produces a fresh {@link IDeformableModel} instance. Registered per model type
 * in
 * {@code AnatomicaRegistries.MODELS}. Unlike {@code PhysicsEngineFactory},
 * model
 * instances are typically stateless/immutable and could be cached rather than
 * recreated per-entity — callers are free to memoize the result of
 * {@link #create()}.
 */
@FunctionalInterface
public interface ModelFactory {
    IDeformableModel create();
}
