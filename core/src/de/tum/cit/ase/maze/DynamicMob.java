package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class DynamicMob extends Mob {

    private Animation<TextureRegion> animation;
    private Texture spriteSheet;

    private int lives;

    public DynamicMob(float x, float y) {
        super(x, y, 8, 6);
        lives = 3;
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
        super.stateTime += delta;
        super.getHitBox().setLocation((int) super.getX() + 4, (int) super.getY() + 6);
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(
                animation.getKeyFrame(super.stateTime, true),
                super.getX(),
                super.getY(),
                16,
                16
        );
    }

    /**
     * Reduces the life count of the dynamic mob by one.
     * <p>
     * This method is used to simulate taking damage by the mob. Each call to this method
     * decrements the mob's life count by one. This is typically invoked when the mob is
     * hit or harmed in some way in the game.
     * </p>
     *
     * @see DynamicMob#lives
     */
    public void takeDamage() {
        this.lives--;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
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

