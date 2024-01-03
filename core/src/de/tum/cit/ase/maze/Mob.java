package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Mob {

    // mob's locations in X and Y-axis
    private float x;
    private float y;

    // sinusInput to update animation Frame
    private float sinusInput;

    private Texture spriteSheet;
    private Animation<TextureRegion> animation;

    public Mob(float x, float y) {
        this.x = x;
        this.y = y;
        sinusInput = 0f;
        spriteSheet = new Texture(Gdx.files.internal("mobs.png"));
        loadAnimations();
    }

    private void loadAnimations() {
        int FRAMES = 3;
        int FRAME_SIZE = 16;

        // LibGDX Array to store TextureRegion frames for animations
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        // flying bat frames
        for (int col = 3; col < 3 + FRAMES; col++) {
            walkFrames.add(new TextureRegion(this.spriteSheet, col * FRAME_SIZE, 4 * FRAME_SIZE, FRAME_SIZE, FRAME_SIZE));
        }

        this.animation = new Animation<>(.1f, walkFrames);
    }

    public void update(float delta) {
        sinusInput += delta;
    }

    public void draw(Batch batch) {
        batch.draw(
                this.animation.getKeyFrame(sinusInput, true),
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

    public float getSinusInput() {
        return sinusInput;
    }

    public void setSinusInput(float sinusInput) {
        this.sinusInput = sinusInput;
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

    public void dispose() {
        spriteSheet.dispose();
    }
}
