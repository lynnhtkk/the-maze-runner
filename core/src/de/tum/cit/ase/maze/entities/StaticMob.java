package de.tum.cit.ase.maze.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.ase.maze.entities.Mob;

/**
 * Represents a static mob (Trap) in the game.
 * <p>
 * This class extends the {@link Mob} class to create static, animated entities in the game,
 * such as traps. It includes functionality to handle the mob's animation, hitbox,
 * and rendering. In this game a static mob would be a fire trap, which has a specific
 * animation and changes its hitbox based on the animation frame (e.g., when the fire is released).
 * </p>
 */
public class StaticMob extends Mob {

    private Texture spriteSheet;

    private Animation<TextureRegion> animation;

    /**
     * Constructs a new StaticMob with specified initial position.
     * <p>
     * Initializes the static mob at the given coordinates (x, y) and sets up its sprite sheet and animation.
     * The sprite sheet for the mob is loaded from assets folder. ("fire_trap.png").
     * </p>
     *
     * @param x The x-coordinate where the static mob will be positioned.
     * @param y The y-coordinate where the static mob will be positioned.
     */
    public StaticMob(float x, float y) {
        super(x, y, 16, 8);
        spriteSheet = new Texture(Gdx.files.internal("fire_trap.png"));
        animation = loadAnimation();
    }

    /**
     * Loads the animation for the static mob.
     * <p>
     * This method creates an animation from the sprite sheet by dividing it into frames.
     * The number of frames and their dimensions are specified.
     * This animation is used to visually represent the mob in the game.
     * </p>
     *
     * @return An {@link Animation} object representing the mob's animation.
     */
    private Animation<TextureRegion> loadAnimation() {
        int FRAMES = 14;
        int FRAME_WIDTH = 32;
        int FRAME_HEIGHT = 41;

        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        for (int col = 0; col < FRAMES; col++) {
            frames.add(new TextureRegion(spriteSheet, col * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
        }
        return new Animation<>(.2f, frames);
    }

    /**
     * Updates the state of the static mob each frame.
     * <p>
     * This method updates the animation state time and adjusts the hit box of the mob based on
     * the current animation frame. For our game, which we are using fire traps to represent static mobs, the hit box's height
     * is increased when the fire is released.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds, used for updating the mob's state.
     */
    @Override
    public void update(float delta) {
        super.stateTime += delta;
        // to check which frame is currently rendered. change the size of hitbox according to it. (fire released at frame 9)
        int frameIndex = (int) ((super.stateTime % (.2f * 14)) / .2f);
        if (frameIndex == 9) {
            super.getHitBox().height = 12;
        } else {
            super.getHitBox().height = 0;
        }
        super.getHitBox().setLocation((int) super.getX(), (int) super.getY());
    }

    /**
     * Draws the static mob on the screen.
     * <p>
     * This method renders the mob's current animation frame at its position using the provided batch.
     * It is called within the game's render loop to ensure the static mob is displayed.
     * </p>
     *
     * @param batch The batch used for drawing the texture, part of the rendering system.
     *
     * @see Batch
     */
    @Override
    public void draw(Batch batch) {
        batch.draw(
                animation.getKeyFrame(super.stateTime, true),
                super.getX(),
                super.getY(),
                16,
                16);
    }

    public Texture getSpriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(Texture spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
    }

    /**
     * Disposes of the resources used by the StaticMob.
     * <p>
     * This method disposes the sprite sheet texture when the static mob is no longer in use.
     * </p>
     */
    @Override
    public void dispose() {
        spriteSheet.dispose();
    }
}

