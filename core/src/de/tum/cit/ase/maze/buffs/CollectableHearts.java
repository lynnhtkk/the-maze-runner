/**
 * The PowerBuff class represents a power-up item(Heart) in a maze game. It includes functionality
 * for updating its state, drawing on the screen, and managing its properties such as position
 * and animation.
 */
package de.tum.cit.ase.maze.buffs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.awt.*;
/**
 * Represents a power-up item(Heart) with animation and collision properties.
 */
public class CollectableHearts {
    private float x;
    private float y;

    private float sinusInput;

    private Rectangle hitBox;

    Texture heartTextureSheet;
    Animation<TextureRegion> heartAnimation;

    Array<TextureRegion> animationFrames;
    /**
     * Constructs a PowerBuff object with the specified initial position.
     *
     * @param x The initial x-coordinate of the heart.
     * @param y The initial y-coordinate of the heart.
     */
    public CollectableHearts(float x, float y) {
        this.x = x;
        this.y = y;
        this.sinusInput = 0f;
        this.hitBox = new Rectangle((int) x, (int) y, 16, 16);
        this.heartTextureSheet = new Texture(Gdx.files.internal("heart.png"));
        this.heartAnimation = loadAnimation();
    }
    /**
     * Loads the animation frames from the texture sheet.
     *
     * @return The Animation object created from the loaded frames.
     */
    private Animation<TextureRegion> loadAnimation() {
        int FRAMES = 4;
        int FRAME_SIZE = 32;
        animationFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < FRAMES; col++) {
            animationFrames.add(new TextureRegion(heartTextureSheet, col * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE));
        }
        return new Animation<>(.2f, animationFrames);
    }
    /**
     * Updates the heart's state based on the elapsed time.
     *
     * @param delta The time elapsed since the last update.
     */
    public void update(float delta) {
        sinusInput += delta;
        hitBox.setLocation((int) x + 8, (int) y + 8);
    }
    /**
     * Draws the heart on the specified batch.
     *
     * @param batch The Batch used for rendering.
     */
    public void draw(Batch batch) {
        batch.draw(
                heartAnimation.getKeyFrame(sinusInput, true),
                x,
                y,
                32,
                32
        );
    }
    // Getters and Setters...
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

    public float getSinusInput() {
        return sinusInput;
    }

    public void setSinusInput(float sinusInput) {
        this.sinusInput = sinusInput;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setHitBox(Rectangle hitBox) {
        this.hitBox = hitBox;
    }

    public Texture getHeartTextureSheet() {
        return heartTextureSheet;
    }

    public void setHeartTextureSheet(Texture heartTextureSheet) {
        this.heartTextureSheet = heartTextureSheet;
    }

    public Animation<TextureRegion> getHeartAnimation() {
        return heartAnimation;
    }

    public Array<TextureRegion> getAnimationFrames() {
        return animationFrames;
    }

    public void setAnimationFrames(Array<TextureRegion> animationFrames) {
        this.animationFrames = animationFrames;
    }

    public void setHeartAnimation(Animation<TextureRegion> heartAnimation) {
        this.heartAnimation = heartAnimation;
    }
    /**
     * Disposes of the resources used by the power-up.
     */
    public void dispose(){heartTextureSheet.dispose();};
}
