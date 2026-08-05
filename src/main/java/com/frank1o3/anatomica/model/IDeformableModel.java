package com.frank1o3.anatomica.model;

import com.frank1o3.anatomica.physics.IPhysicsEngine;
import com.frank1o3.franklylib.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A mesh that can be skinned against an {@link IPhysicsEngine}'s current node
 * state.
 *
 * <p>
 * Every implementation — including the simplest built-in box shape — goes
 * through the
 * shared {@link NodeSkinning#skinAll} path in {@link #deform}. There is
 * intentionally
 * no per-model special-casing here: a model with few vertices just ends up
 * weighted
 * toward whichever handful of nodes are nearest.
 */
public interface IDeformableModel {

    /**
     * Rest-pose vertices, each pre-tagged with its physics node influences/weights.
     */
    ModelVertex[] baseVertices();

    /**
     * Flat triangle index list into {@link #baseVertices()}. Length must be a
     * multiple of 3.
     */
    int[] indices();

    /** Stable registry id for this model, e.g. {@code anatomica:box}. */
    Identifier id();

    /** Human-readable name shown in model-selection UI. */
    Component displayName();

    /**
     * Returns this model's vertices deformed by the current state of
     * {@code engine},
     * in the same order as {@link #baseVertices()}.
     */
    default Vec3[] deform(IPhysicsEngine engine) {
        return NodeSkinning.skinAll(baseVertices(), engine);
    }
}
