package com.frank1o3.anatomica.physics;

import com.frank1o3.anatomica.config.IBodyConfig;
import com.frank1o3.franklylib.Vec3;

/**
 * Contract for a per-entity soft-body (or otherwise) simulation driving a set
 * of
 * physics nodes over time.
 *
 * <p>
 * This interface intentionally exposes <b>node data only</b> — no legacy scalar
 * getters (position X/Y, rotation, etc). Consumers (models, debug tooling) read
 * node
 * positions and derive whatever they need from that; the engine has no opinion
 * on how
 * its nodes map onto a visible mesh. See {@code IDeformableModel} for the
 * node-to-vertex skinning side of that contract.
 *
 * <p>
 * Implementations are expected to be client-only (physics here is a purely
 * cosmetic,
 * per-client simulation — it does not need to be authoritative or run on a
 * dedicated
 * server), but the interface itself lives common-side so common-side code can
 * reference the type without pulling in client classes.
 */
public interface IPhysicsEngine {

    /** Advances the simulation by one tick. */
    void tick(float deltaTime, LivingEntityLike entity, IBodyConfig config);

    /** Resets all nodes back to their rest positions with zero velocity. */
    void reset();

    /**
     * Number of simulated nodes. Constant for the lifetime of an engine instance.
     */
    int nodeCount();

    /**
     * Rest (undeformed) position of node {@code index}, in the engine's local
     * space.
     */
    Vec3 nodeRestPosition(int index);

    /**
     * Current simulated position of node {@code index}, as of the last
     * {@link #tick}.
     */
    Vec3 nodePosition(int index);

    /** Current simulated velocity of node {@code index}. */
    Vec3 nodeVelocity(int index);

    /**
     * Whether node {@code index} is pinned (never integrated — e.g. an anchor row).
     */
    boolean isNodeFixed(int index);

    /**
     * Computes an interpolated snapshot between the previous and current tick's
     * node
     * positions, to be read via {@link #interpolatedNodePosition(int)} during
     * rendering. Must be called once per frame before rendering reads node data.
     */
    void interpolate(float partialTick);

    /**
     * Interpolated position of node {@code index}, valid after
     * {@link #interpolate}.
     */
    Vec3 interpolatedNodePosition(int index);

    /**
     * Applies an instantaneous impulse to the node(s) nearest {@code localPoint}.
     */
    void applyImpulse(Vec3 localPoint, Vec3 force);
}
