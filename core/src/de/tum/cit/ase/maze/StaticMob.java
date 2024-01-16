package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class StaticMob extends Mob {

    private Texture spriteSheet;

    private Animation<TextureRegion> animation;

    public StaticMob(float x, float y) {
        super(x, y, 16, 8);
        spriteSheet = new Texture(Gdx.files.internal("fire_trap.png"));
        animation = loadAnimation();
    }

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

    @Override
    public void update(float delta) {
        super.stateTime += delta;
        super.getHitBox().setLocation((int) super.getX(), (int) super.getY());
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(
                animation.getKeyFrame(super.stateTime, true),
                super.getX(),
                super.getY(),
                16,
                16);
    }

    @Override
    public void dispose() {
        spriteSheet.dispose();
    }
}

