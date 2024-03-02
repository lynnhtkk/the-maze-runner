package de.tum.cit.ase.maze.constants;

/**
 * Enumeration representing the possible directions an entity can face.
 * <p>
 * This enumeration is used to define the four cardinal directions that an entity, such as a player
 * or a mob, can face. Each enum value corresponds to a specific direction:
 * <ul>
 *     <li>{@code LEFT}: Indicates the entity is facing left.</li>
 *     <li>{@code RIGHT}: Indicates the entity is facing right.</li>
 *     <li>{@code UP}: Indicates the entity is facing upwards.</li>
 *     <li>{@code DOWN}: Indicates the entity is facing downwards.</li>
 * </ul>
 * This enumeration is essential for various game mechanics, including movement, animation,
 * and collision detection.
 * </p>
 */
public enum Direction {
    LEFT,
    RIGHT,
    UP,
    DOWN
}
