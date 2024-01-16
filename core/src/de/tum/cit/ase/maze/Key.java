package de.tum.cit.ase.maze;

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

    private float sinusInput;

    private Rectangle hitBox;

    Texture keyTextureSheet;
    Animation<TextureRegion> keyAnimation;

    public Key(float x, float y) {
        this.x = x;
        this.y = y;
        sinusInput = 0f;
        hitBox = new Rectangle((int) x + 3, (int) y + 3, 10, 10);
        keyTextureSheet = new Texture(Gdx.files.internal("key.png"));
        keyAnimation = loadAnimation();
    }

    private Animation<TextureRegion> loadAnimation() {
        int FRAMES = 4;
        int FRAME_SIZE = 16;
        Array<TextureRegion> animationFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < FRAMES; col++) {
            animationFrames.add(new TextureRegion(keyTextureSheet, col * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE));
        }
        return new Animation<>(.2f, animationFrames);
    }

    public void update(float delta) {
        sinusInput += delta;
        hitBox.setLocation((int) x + 3, (int) y + 3);
    }

    public void draw(Batch batch) {
        batch.draw(
                keyAnimation.getKeyFrame(sinusInput, true),
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

    public void dispose() {
        keyTextureSheet.dispose();
    }

}
