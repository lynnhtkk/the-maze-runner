package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class Heart {
    private float x;
    private float y;

    private float sinusInput;

    private Rectangle hitBox;

    Texture heartTextureSheet;
    Animation<TextureRegion> heartAnimation;

    Array<TextureRegion> animationFrames;

    public Heart(float x, float y) {
        this.x = x;
        this.y = y;
        this.sinusInput = 0f;
        this.hitBox = new Rectangle((int) x, (int) y, 16, 16);
        this.heartTextureSheet = new Texture(Gdx.files.internal("heart.png"));
        this.heartAnimation = loadAnimation();
    }

    private Animation<TextureRegion> loadAnimation() {
        int FRAMES = 4;
        int FRAME_SIZE = 32;
        animationFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < FRAMES; col++) {
            animationFrames.add(new TextureRegion(heartTextureSheet, col * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE));
        }
        return new Animation<>(.2f, animationFrames);
    }

    public void update(float delta) {
        sinusInput += delta;
        hitBox.setLocation((int) x + 8, (int) y + 8);
    }

    public void draw(Batch batch) {
        batch.draw(
                heartAnimation.getKeyFrame(sinusInput, true),
                x,
                y,
                32,
                32
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

    public void setHeartAnimation(Animation<TextureRegion> heartAnimation) {
        this.heartAnimation = heartAnimation;
    }

    public void dispose(){heartTextureSheet.dispose();};
}
