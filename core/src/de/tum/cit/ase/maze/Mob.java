package de.tum.cit.ase.maze;

import com.badlogic.gdx.graphics.g2d.Batch;

import java.awt.*;

/**
 * Abstract base class (parent class) for all mob entities in the game.
 * <p>
 * This class serves as a foundation for different types of mobs, such as {@link StaticMob}
 * and {@link DynamicMob}. It defines the basic properties and functionalities that all mob
 * types share, including position, state time, and hitbox. The class provides the framework
 * for essential methods like update, draw, and dispose, which must be implemented by its
 * subclasses according to their specific behaviors and characteristics.
 * </p>
 */
public abstract class Mob {

    protected float x;
    protected float y;
    protected float stateTime;

    private Rectangle hitBox;

    /**
     * Constructs a Mob object with specified initial position and Hit Box dimensions.
     * <p>
     * Initializes a Mob at the given coordinates (x, y) and sets up its Hit Box with the specified width and height.
     * The state time is initialized to zero.
     * </p>
     *
     * @param x The x-coordinate where the mob will be positioned.
     * @param y The y-coordinate where the mob will be positioned.
     * @param hitBoxWidth The width of the mob's Hit Box.
     * @param hitBoxHeight The height of the mob's Hit Box.
     */
    public Mob(float x, float y, int hitBoxWidth, int hitBoxHeight) {
        this.x = x;
        this.y = y;
        stateTime = 0f;
        hitBox = new Rectangle((int) x, (int) y, hitBoxWidth, hitBoxHeight);
    }

    /**
     * Updates the state of the mob.
     * <p>
     * This abstract method is to be implemented by subclasses to define how the mob's state is updated
     * each frame, based on the elapsed time (delta).
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds.
     */
    public abstract void update(float delta);

    /**
     * Draws the mob on the screen.
     * <p>
     * This abstract method is to be implemented by subclasses to define how the mob is rendered
     * each frame using the provided batch.
     * </p>
     *
     * @param batch The batch used for drawing the mob, part of the rendering system.
     */
    public abstract void draw(Batch batch);

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setHitBox(Rectangle hitBox) {
        this.hitBox = hitBox;
    }

    /**
     * Disposes of the resources used by the mob.
     * <p>
     * This abstract method is to be implemented by subclasses to handle the cleanup and
     * releasing of resources used by the mob, such as textures or animations.
     * </p>
     */
    public abstract void dispose();
}

