package com.frank1o3.anatomica.client.render;

import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.anatomica.physics.LivingEntityLike;
import com.frank1o3.anatomica.physics.PhysicsEngineFactory;
import com.frank1o3.anatomica.client.registry.AnatomicaRegistries;
import com.frank1o3.franklylib.Vec3;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the physics engine instance(s) for one tracked entity: left breast, right breast
 * (mirrored or independent), and outer cloth softbody.
 *
 * <p>
 * Extended in place to manage the coupled multi-layer system (left, right, cloth)
 * directly within this holder, avoiding duplication of entity tracking, lifecycle,
 * and config-change recreation logic.
 */
public final class ClientBodyPhysics {

    private static final Map<UUID, ClientBodyPhysics> HOLDERS = new ConcurrentHashMap<>();

    private IPhysicsEngine leftEngine;
    private IPhysicsEngine rightEngine;
    private IPhysicsEngine clothEngine;
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

    /** Drops simulation state for entities no longer present in the client world. */
    public static void retainFor(Set<UUID> activeEntityIds) {
        HOLDERS.keySet().retainAll(activeEntityIds);
    }

    /**
     * Advances the owned engine(s) by one tick and resolves inter-layer contact constraints.
     * Call once per client entity tick.
     */
    public void tick(float deltaTime, LivingEntityLike entity, IBodyConfig config) {
        ensureEngines(config);
        if (leftEngine != null) {
            leftEngine.tick(deltaTime, entity, config);
        }
        if (currentIndependentSides && rightEngine != null) {
            rightEngine.tick(deltaTime, entity, config);
        }
        if (clothEngine != null) {
            clothEngine.tick(deltaTime, entity, config);
            resolveContacts();
        }
    }

    /**
     * Pairwise contact resolution between cloth nodes and each breast's nodes.
     * All three engines share the same SoftbodyGridLayout topology, allowing
     * grid-index-aligned penetration depth evaluation.
     */
    private void resolveContacts() {
        if (clothEngine == null || leftEngine == null) {
            return;
        }

        int count = clothEngine.nodeCount();
        for (int i = 0; i < count; i++) {
            if (clothEngine.isNodeFixed(i)) {
                continue;
            }

            Vec3 clothPos = clothEngine.nodePosition(i);
            Vec3 leftPos = leftEngine.nodePosition(i);
            Vec3 rightPos = (rightEngine != null && currentIndependentSides)
                    ? rightEngine.nodePosition(i)
                    : leftPos;

            // Most forward breast position (more negative Z = further forward)
            float breastFrontZ = Math.min(leftPos.z(), rightPos.z());
            float margin = 0.005f;

            // Penetration occurs when cloth sits behind breast (cloth.z > breast.z - margin)
            if (clothPos.z() > breastFrontZ - margin) {
                float penetration = clothPos.z() - (breastFrontZ - margin);
                Vec3 rest = clothEngine.nodeRestPosition(i);

                // Push cloth outward along -Z
                clothEngine.applyImpulse(rest, new Vec3(0f, 0f, -penetration * 0.5f));

                // Apply soft reaction impulse to penetrating breast engines
                if (leftPos.z() > breastFrontZ - margin * 0.5f) {
                    leftEngine.applyImpulse(rest, new Vec3(0f, 0f, penetration * 0.2f));
                }
                if (rightEngine != null && currentIndependentSides && rightPos.z() > breastFrontZ - margin * 0.5f) {
                    rightEngine.applyImpulse(rest, new Vec3(0f, 0f, penetration * 0.2f));
                }
            }
        }
    }

    /**
     * The left-side engine. Call {@link #ensureEngines(IBodyConfig)} first if needed.
     */
    public IPhysicsEngine leftEngine() {
        return leftEngine;
    }

    /**
     * The right-side engine: the independent right engine if
     * {@link IBodyConfig#independentSides()}, otherwise the same left engine (the
     * renderer mirrors it via an X-flip instead of simulating a second copy).
     */
    public IPhysicsEngine rightEngine() {
        return currentIndependentSides ? rightEngine : leftEngine;
    }

    /**
     * The cloth engine covering both breasts. Call {@link #ensureEngines(IBodyConfig)} first if needed.
     */
    public IPhysicsEngine clothEngine() {
        return clothEngine;
    }

    /**
     * Ensures engine instances exist and match the given {@code config}. Safe to
     * call before rendering or ticking.
     */
    public void ensureEngines(IBodyConfig config) {
        boolean engineChanged = leftEngine == null || clothEngine == null || !config.physicsEngineId().equals(currentEngineId);
        boolean sidesChanged = config.independentSides() != currentIndependentSides;

        if (!engineChanged && !sidesChanged) {
            return;
        }

        PhysicsEngineFactory factory = AnatomicaRegistries.PHYSICS_ENGINES.getValue(config.physicsEngineId());
        leftEngine = factory != null ? factory.create() : null;
        rightEngine = config.independentSides() && factory != null ? factory.create() : null;
        clothEngine = factory != null ? factory.create() : null;
        currentEngineId = config.physicsEngineId();
        currentIndependentSides = config.independentSides();
    }
}
