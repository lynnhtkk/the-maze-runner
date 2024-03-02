package de.tum.cit.ase.maze.buffs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class Key {
    private float x;
    private float y;

    private float stateTime;

    private Rectangle hitBox;

    private Texture keyTextureSheet;
    private Animation<TextureRegion> keyAnimation;
    private Array<TextureRegion> animationFrames;

    /**
     * Constructs a new Key object at the specified location.
     * <p>
     * This constructor initializes a Key with its position set to (x, y). It also sets up the {@code hitBox}
     * for the key and loads the texture and animation required for rendering the key. The texture for
     * the key is obtained from the assets folder ("key.png").
     * </p>
     *
     * @param x The x-coordinate where the key will be positioned.
     * @param y The y-coordinate where the key will be positioned.
     */
    public Key(float x, float y) {
        this.x = x;
        this.y = y;
        stateTime = 0f;
        hitBox = new Rectangle((int) x + 3, (int) y + 3, 10, 10);
        keyTextureSheet = new Texture(Gdx.files.internal("key.png"));
        keyAnimation = loadAnimation();
    }

    /**
     * Loads the movement animation for key.
     * <p>
     * This private method initializes the animation for the key's floating effect by creating
     * a sequence of texture frames. The animation is constructed from a sprite sheet
     * where each frame represents a different stage of the key.
     * The method sets up a specific number of frames (defined by {@code FRAMES}), each
     * with a specified size (defined by {@code FRAME_SIZE}).
     * These frames are then added to the animation sequence. The animation is stored in
     * the {@code keyAnimation} field of the Key class.
     * </p>
     *
     * @see Texture
     * @see TextureRegion
     * @see Animation
     */
    private Animation<TextureRegion> loadAnimation() {
        int FRAMES = 4;
        int FRAME_SIZE = 16;
        animationFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < FRAMES; col++) {
            animationFrames.add(new TextureRegion(keyTextureSheet, col * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE));
        }
        return new Animation<>(.2f, animationFrames);
    }

    /**
     * Updates the state of the key each frame.
     * <p>
     * This method is responsible for updating the state of the key based on the elapsed time
     * (delta). It updates the {@code stateTime} value used in the key's animation and also updates
     * the location of the {@code hitBox}.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds, used for
     *              updating the key's state.
     */
    public void update(float delta) {
        stateTime += delta;
        hitBox.setLocation((int) x + 3, (int) y + 3);
    }

    /**
     * Draws the key on the screen.
     * <p>
     * This method renders the key's current animation frame at its position. It uses the batch
     * provided to draw the texture. The method should be called within the game's render loop
     * to ensure the key is properly displayed.
     * </p>
     *
     * @param batch The batch used for drawing the texture, part of the rendering system.
     *
     * @see Batch
     * @see Animation
     */
    public void draw(Batch batch) {
        batch.draw(
                keyAnimation.getKeyFrame(stateTime, true),
                x,
                y,
                16,
                16
        );
    }

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

    public void setPosition(float x, float y) {
        this.x = x;
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

    public Texture getKeyTextureSheet() {
        return keyTextureSheet;
    }

    public void setKeyTextureSheet(Texture keyTextureSheet) {
        this.keyTextureSheet = keyTextureSheet;
    }

    public Animation<TextureRegion> getKeyAnimation() {
        return keyAnimation;
    }

    public void setKeyAnimation(Animation<TextureRegion> keyAnimation) {
        this.keyAnimation = keyAnimation;
    }

    public Array<TextureRegion> getAnimationFrames() {
        return animationFrames;
    }

    public void setAnimationFrames(Array<TextureRegion> animationFrames) {
        this.animationFrames = animationFrames;
    }

    public void dispose() {
        keyTextureSheet.dispose();
    }

}
