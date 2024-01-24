package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class Apple {
    private float x;
    private float y;

    private float sinusInput;

    private Rectangle hitBox;

    Texture appleTextureSheet;
    Animation<TextureRegion> appleAnimation;

    public Apple(float x, float y) {
        this.x = x;
        this.y = y;
        sinusInput = 0f;
        hitBox = new Rectangle((int) x + 3, (int) y + 3, 10, 10);
        appleTextureSheet = new Texture(Gdx.files.internal("Apple.png"));
        appleAnimation = loadAnimation();
    }

    private Animation<TextureRegion> loadAnimation() {
        int FRAMES = 4;
        int FRAME_SIZE = 16;
        Array<TextureRegion> animationFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < FRAMES; col++) {
            animationFrames.add(new TextureRegion(appleTextureSheet, col * FRAME_SIZE, 0, FRAME_SIZE, FRAME_SIZE));
        }
        return new Animation<>(.2f, animationFrames);
    }

    public void update(float delta) {
        sinusInput += delta;
        hitBox.setLocation((int) x + 3, (int) y + 3);
    }

    public void draw(Batch batch) {
        batch.draw(
                appleAnimation.getKeyFrame(sinusInput, true),
                x,
                y,
                16,
                16
        );
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
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
}
