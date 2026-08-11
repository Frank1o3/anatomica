package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.client.config.BodyConfig;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.physics.LivingEntityLike;
import com.frank1o3.anatomica.physics.PhysicsEngineFactory;
import com.frank1o3.anatomica.client.registry.AnatomicaRegistries;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the physics engine instance(s) for one tracked entity: one if
 * {@link BodyConfig#independentSides()} is {@code false} (the right side
 * mirrors the
 * left at render time via an X-flip), two if it's {@code true} (each side
 * simulates
 * independently). Re-creates the engine(s) whenever the entity's selected
 * {@link BodyConfig#physicsEngineId()} changes.
 */
public final class ClientBodyPhysics {

    private static final Map<UUID, ClientBodyPhysics> HOLDERS = new ConcurrentHashMap<>();

    private IPhysicsEngine leftEngine;
    private IPhysicsEngine rightEngine;
    private net.minecraft.resources.Identifier currentEngineId;
    private boolean currentIndependentSides;

    private ClientBodyPhysics() {
    }

    /** Gets or creates the holder for {@code entityUuid}. */
    public static ClientBodyPhysics get(UUID entityUuid) {
        return HOLDERS.computeIfAbsent(entityUuid, id -> new ClientBodyPhysics());
    }

    public static void remove(UUID entityUuid) {
        HOLDERS.remove(entityUuid);
    }

    public static void clearAll() {
        HOLDERS.clear();
    }

    /**
     * Advances the owned engine(s) by one tick. Call once per client entity tick.
     */
    public void tick(float deltaTime, LivingEntityLike entity, BodyConfig config) {
        ensureEngines(config);
        if (leftEngine != null) {
            leftEngine.tick(deltaTime, entity, config);
        }
        if (currentIndependentSides && rightEngine != null) {
            rightEngine.tick(deltaTime, entity, config);
        }
    }

    /**
     * The left-side engine. Call {@link #ensureEngines(BodyConfig)} first if
     * needed.
     */
    public IPhysicsEngine leftEngine() {
        return leftEngine;
    }

    /**
     * The right-side engine: the independent right engine if
     * {@link BodyConfig#independentSides()}, otherwise the same left engine (the
     * renderer mirrors it via an X-flip instead of simulating a second copy).
     */
    public IPhysicsEngine rightEngine() {
        return currentIndependentSides ? rightEngine : leftEngine;
    }

    /**
     * Ensures engine instances exist and match the given {@code config}. Safe to
     * call
     * before rendering or ticking.
     */
    public void ensureEngines(BodyConfig config) {
        boolean engineChanged = leftEngine == null || !config.physicsEngineId().equals(currentEngineId);
        boolean sidesChanged = config.independentSides() != currentIndependentSides;

        if (!engineChanged && !sidesChanged) {
            return;
        }

        PhysicsEngineFactory factory = AnatomicaRegistries.PHYSICS_ENGINES.getValue(config.physicsEngineId());
        leftEngine = factory != null ? factory.create() : null;
        rightEngine = config.independentSides() && factory != null ? factory.create() : null;
        currentEngineId = config.physicsEngineId();
        currentIndependentSides = config.independentSides();
    }
}
