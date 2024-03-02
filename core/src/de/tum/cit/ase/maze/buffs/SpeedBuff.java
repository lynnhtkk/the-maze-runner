/**
 * The SpeedBuff class represents a speed boost item(Apple) in a maze game. It includes functionality
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
 * Represents a speed boost item(Apple) with animation and collision properties.
 */
public class SpeedBuff {
    private float x;
    private float y;

    private float sinusInput;

    private Rectangle hitBox;

    Texture appleTextureSheet;
    Animation<TextureRegion> appleAnimation;
    /**
     * Constructs a SpeedBuff object with the specified initial position.
     *
     * @param x The initial x-coordinate of the Apple.
     * @param y The initial y-coordinate of the Apple.
     */
    public SpeedBuff(float x, float y) {
        this.x = x;
        this.y = y;
        sinusInput = 0f;
        hitBox = new Rectangle((int) x + 3, (int) y + 3, 10, 10);
        appleTextureSheet = new Texture(Gdx.files.internal("apple.png"));
        appleAnimation = loadAnimation();
    }
    /**
     * Loads the animation frames from the texture sheet.
     *
     * @return The Animation object created from the loaded frames.
     */
    private Animation<TextureRegion> loadAnimation() {
        int FRAMES = 4;
        int FRAME_SIZE = 16;
        Array<TextureRegion> animationFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < FRAMES; col++) {
            animationFrames.add(new TextureRegion(appleTextureSheet, col * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE));
        }
        return new Animation<>(.2f, animationFrames);
    }
    /**
     * Updates the Apple's state based on the elapsed time.
     *
     * @param delta The time elapsed since the last update.
     */
    public void update(float delta) {
        sinusInput += delta;
        hitBox.setLocation((int) x + 3, (int) y + 3);
    }
    /**
     * Draws the Apple on the specified batch.
     *
     * @param batch The Batch used for rendering.
     */
    public void draw(Batch batch) {
        batch.draw(
                appleAnimation.getKeyFrame(sinusInput, true),
                x,
                y,
                16,
                16
        );
    }
    /**
     * Sets the position of the Apple.
     *
     * @param x The new x-coordinate.
     * @param y The new y-coordinate.
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
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

    public Texture getAppleTextureSheet() {
        return appleTextureSheet;
    }

    public void setAppleTextureSheet(Texture appleTextureSheet) {
        this.appleTextureSheet = appleTextureSheet;
    }

    public Animation<TextureRegion> getAppleAnimation() {
        return appleAnimation;
    }

    public void setAppleAnimation(Animation<TextureRegion> appleAnimation) {
        this.appleAnimation = appleAnimation;
    }
    /**
     * Disposes of the resources used by the speed boost item.
     */
    public void dispose() {
        appleTextureSheet.dispose();
    }
}
