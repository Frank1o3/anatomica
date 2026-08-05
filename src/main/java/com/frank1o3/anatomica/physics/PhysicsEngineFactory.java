package com.frank1o3.anatomica.physics;

/**
 * Produces a fresh {@link IPhysicsEngine} instance. Registered per
 * physics-engine type
 * in {@code AnatomicaRegistries.PHYSICS_ENGINES}; a new instance is created
 * whenever an
 * entity's selected engine id changes (engines are stateful/per-entity, not
 * shared).
 */
@FunctionalInterface
public interface PhysicsEngineFactory {
    IPhysicsEngine create();
}
