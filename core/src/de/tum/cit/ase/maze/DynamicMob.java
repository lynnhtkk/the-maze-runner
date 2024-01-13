package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class DynamicMob extends Mob {

    private float sinusInput;
    private Animation<TextureRegion> animation;
    private Texture spriteSheet;

    public DynamicMob(float x, float y) {
        super(x, y, 8, 6);
        spriteSheet = new Texture(Gdx.files.internal("mobs.png"));
        loadAnimation();
    }

    private void loadAnimation() {
        int FRAMES = 3;
        int FRAME_WIDTH = 16;
        int FRAME_HEIGHT = 16;

        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        for (int col = 3; col < FRAMES + 3; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * FRAME_WIDTH, 4 * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
        }
        animation = new Animation<>(.1f, walkFrames);
    }

    @Override
    public void update(float delta) {
        sinusInput += delta;
        super.getHitBox().setLocation((int) super.getX() + 4, (int) super.getY() + 6);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(
                animation.getKeyFrame(sinusInput, true),
                super.getX(),
                super.getY(),
                16,
                16
        );
    }

    public float getSinusInput() {
        return sinusInput;
    }

    public void setSinusInput(float sinusInput) {
        this.sinusInput = sinusInput;
    }

    public Animation<TextureRegion> getAnimation() {
        return animation;
    }

    public void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
    }

    public Texture getSpriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(Texture spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    @Override
    public void dispose() {
        spriteSheet.dispose();
    }

}

