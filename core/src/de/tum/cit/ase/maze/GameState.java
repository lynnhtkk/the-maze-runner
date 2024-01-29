package de.tum.cit.ase.maze;

/**
 * Enumeration representing the various states of the game.
 * <p>
 * This enumeration is used to define the different stages that the game can be in
 * at any given time. It is essential for controlling game flow, determining when to load new maps,
 * and managing transitions between different states of gameplay. The possible states are:
 * <ul>
 *     <li>{@code RUNNING}: The game is in progress and ongoing.</li>
 *     <li>{@code NEW_GAME}: A new game is to be started, typically requiring initialization a new {@link GameScreen}.</li>
 *     <li>{@code GAME_OVER}: The game has ended due to the player losing or other conditions.</li>
 *     <li>{@code VICTORY}: The game has ended with the player achieving victory.</li>
 * </ul>
 * </p>
 */
public enum GameState {
    RUNNING,
    NEW_GAME,
    GAME_OVER,
    VICTORY
}
