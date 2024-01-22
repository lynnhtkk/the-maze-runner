package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class DynamicMob extends Mob {

    private Animation<TextureRegion> animation;
    private Texture spriteSheet;

    private int lives;

    private boolean isInvincible;
    private float invincibility_timer;
    private final float INVINCIBILITY_DURATION;

    private Vector2 knockBackVector;
    private float knockBackTime;
    private final float KNOCKBACKDURATION;
    private boolean beingKnockedBack;

    public DynamicMob(float x, float y) {
        super(x, y, 8, 6);
        lives = 3;
        spriteSheet = new Texture(Gdx.files.internal("mobs.png"));
        KNOCKBACKDURATION = 1f;
        beingKnockedBack = false;
        isInvincible = false;
        INVINCIBILITY_DURATION = 1f;
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
        // check if the mob is invincible, if invincible, count down the timer
        if (isInvincible) {
            invincibility_timer -= delta;
            if (invincibility_timer <= 0) {
                isInvincible = false;
            }
        }

        // apply knock back if beingKnockedBack is true
        if (beingKnockedBack) {
            // count down the knock back timer
            knockBackTime -= delta;
            // if the timer is up (reaches 0), set beingKnockedBack to false again
            if (knockBackTime <= 0) {
                beingKnockedBack = false;
            } else {
                float knockBackFactor = knockBackTime / KNOCKBACKDURATION;
                Vector2 knockBackThisFrame = knockBackVector.cpy().scl(knockBackFactor);

                super.setX(super.getX() + knockBackThisFrame.x);
                super.setY(super.getY() + knockBackThisFrame.y);
            }
        }

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

    public void applyKnockBack(Player player, float knockBackDistance) {
        switch (player.getFacingDirection()) {
            case UP, DOWN:
                knockBackVector = new Vector2(0, super.getY() - player.getPlayerY());
                break;
            case LEFT, RIGHT:
                knockBackVector = new Vector2(super.getX() - player.getPlayerX(), 0);
                break;
        }

        knockBackVector.nor();
        knockBackVector.scl(knockBackDistance);

        knockBackTime = KNOCKBACKDURATION;
        beingKnockedBack = true;
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
        if (!isInvincible) {
            this.lives--;
            isInvincible = true;
            invincibility_timer = INVINCIBILITY_DURATION;
        }
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

    public boolean isInvincible() {
        return isInvincible;
    }

    public void setInvincible(boolean invincible) {
        isInvincible = invincible;
    }

    public float getInvincibility_timer() {
        return invincibility_timer;
    }

    public void setInvincibility_timer(float invincibility_timer) {
        this.invincibility_timer = invincibility_timer;
    }

    public float getINVINCIBILITY_DURATION() {
        return INVINCIBILITY_DURATION;
    }

    public Vector2 getKnockBackVector() {
        return knockBackVector;
    }

    public void setKnockBackVector(Vector2 knockBackVector) {
        this.knockBackVector = knockBackVector;
    }

    public float getKnockBackTime() {
        return knockBackTime;
    }

    public void setKnockBackTime(float knockBackTime) {
        this.knockBackTime = knockBackTime;
    }

    public float getKNOCKBACKDURATION() {
        return KNOCKBACKDURATION;
    }

    public boolean isBeingKnockedBack() {
        return beingKnockedBack;
    }

    public void setBeingKnockedBack(boolean beingKnockedBack) {
        this.beingKnockedBack = beingKnockedBack;
    }

    @Override
    public void dispose() {
        spriteSheet.dispose();
    }

}

