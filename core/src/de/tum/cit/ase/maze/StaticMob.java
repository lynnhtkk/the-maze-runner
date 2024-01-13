package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StaticMob extends Mob {

    private Texture spriteSheet;

    public StaticMob(float x, float y) {
        super(x, y, 16, 16);
        spriteSheet = new Texture(Gdx.files.internal("tilemap_packed.png"));
    }

    @Override
    public void update(float delta) {
        super.getHitBox().setLocation((int) super.getX(), (int) super.getY());
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(
                new TextureRegion(spriteSheet, 16, 10 * 16, 16, 16),
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

