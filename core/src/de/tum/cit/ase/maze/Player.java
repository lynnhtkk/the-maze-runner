package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Player {
    private Texture spriteSheet;

    private float playerX;
    private float playerY;

    private float speed;

    private int playerWidth;
    private int playerHeight;

    private TiledMapTileLayer collisionLayer;

    private Rectangle collisionBox;
    private Rectangle hitBox;

    private boolean isInvincible;
    private float invincibility_timer;
    private final float INVINCIBILITY_DURATION;

    private Direction facingDirection;
    private boolean attacking;
    private float attackStateTime;
    private Rectangle attackBox;

    private Vector2 knockBackVector;
    private float knockBackTime;
    private final float KNOCKBACKDURATION;
    private boolean beingKnockedBack;

    private int playerLives;

    private boolean hasKey;

    private Map<String, Animation<TextureRegion>> playerAnimations;
    private Animation<TextureRegion> currentAnimation;

    private float stateTime;

    private Sound attackSound;

    public Player(float playerX, float playerY, TiledMapTileLayer collisionLayer) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.collisionLayer = collisionLayer;
        this.speed = 80f;
        this.playerWidth = 16;
        this.playerHeight = 32;
        this.playerLives = 3;
        this.hasKey = false;
        isInvincible = false;
        invincibility_timer = 0f;
        INVINCIBILITY_DURATION = 2f;
        facingDirection = Direction.DOWN;
        attacking = false;
        attackStateTime = 0f;
        attackBox = new Rectangle((int) playerX, (int) playerY, 0, 0);
        attackSound = Gdx.audio.newSound(Gdx.files.internal("attack.wav"));
        knockBackTime = 0f;
        KNOCKBACKDURATION = 1f;
        beingKnockedBack = false;
        this.collisionBox = new Rectangle((int) playerX + 4, (int) playerY + 6, (int) (playerWidth * 0.5), (int) (playerHeight * 0.2));
        this.hitBox = new Rectangle((int) playerX + 4, (int) playerY + 8, 8, 15);
        this.stateTime = 0f;
        this.spriteSheet = new Texture(Gdx.files.internal("character.png"));
        this.playerAnimations = this.loadAnimations();
        this.currentAnimation = this.playerAnimations.get("down");
    }

    public void update(float delta, int mapWidth, int mapHeight, int borderTiles) {
        attacking = false;
        attackBox.setLocation((int) playerX, (int) playerY);
        attackBox.setSize(0, 0);

        // check to see if the player is knocked back (took damage) and apply knock back if it does
        if (beingKnockedBack) {
            // count down the knock back timer
            knockBackTime -= delta;
            if (knockBackTime <= 0) {
                beingKnockedBack = false;
            } else {
                // to get the slowly knocked back effect
                float knockBackFactor = knockBackTime / KNOCKBACKDURATION;
                Vector2 knockBackThisFrame = knockBackVector.cpy().scl(knockBackFactor);

                // to check collision and to make sure that the player doesn't get knocked back to the walls
                float potentialX = this.hitBox.x + knockBackThisFrame.x + 1;
                float potentialY = this.hitBox.y + knockBackThisFrame.y + 1;
                if (!isCellBlocked(potentialX, potentialY) && !isCellBlocked(potentialX + collisionBox.width, potentialY)
                        && !isCellBlocked(potentialX, potentialY + collisionBox.height) && !isCellBlocked(potentialX + collisionBox.width, potentialY + collisionBox.height)) {
                    this.playerX += knockBackThisFrame.x;
                    this.playerY += knockBackThisFrame.y;
                } else {
                    // if the player collides with the wall, reposition the player slightly outside the wall on the Y-axis and stop the knock back effect
                    TiledMapTileLayer.Cell cell = collisionLayer.getCell((int) (potentialX / 16), (int) (potentialY / 16));
                    if (knockBackThisFrame.y < 0) {
                        if (cell != null) {
                            float cellUpperBound = (int) (potentialY / 16) * 16 + 16;
                            this.playerY = cellUpperBound - 6;      // collision box Y is 6 pixels above the actual player's Y position
                        }
                    }
                    if (knockBackThisFrame.x < 0) {
                        if (cell != null) {
                            float cellRightBound = (int) (potentialX / 16) * 16 + 16;
                            this.playerX = cellRightBound - 4;
                        }
                    }
                    beingKnockedBack = false;
                }
            }
        }

        // check to see if the player is invincible, if invincible, count down the timer
        if (isInvincible) {
            invincibility_timer -= delta;
            if (invincibility_timer <= 0) {
                isInvincible = false;
            }
        }

        // restrict so that the player can't go outside the maze walls
        if (playerX < borderTiles * 16) playerX = borderTiles * 16;
        if (playerX > (mapHeight + borderTiles - 1) * 16) playerX = (mapHeight + borderTiles - 1) * 16;
        if (playerY < borderTiles * 16) playerY = borderTiles * 16;
        if (playerY > (mapHeight + borderTiles - 1) * 16) playerY = (mapHeight + borderTiles - 1) * 16;

        // move player according to the input
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            facingDirection = Direction.LEFT;
            stateTime += delta;
            currentAnimation = playerAnimations.get("left");
            hitBox.height = 15;
            float potentialX = collisionBox.x - (speed * delta);
            if (!isCellBlocked(potentialX, collisionBox.y) && !isCellBlocked(potentialX, collisionBox.y + collisionBox.height)) {
                playerX -= speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            facingDirection = Direction.RIGHT;
            stateTime += delta;
            currentAnimation = playerAnimations.get("right");
            hitBox.height = 15;
            float potentialX = collisionBox.x + (speed * delta) + 1;
            if (!isCellBlocked(potentialX + collisionBox.width, collisionBox.y) && !isCellBlocked(potentialX + collisionBox.width, collisionBox.y + collisionBox.height)) {
                playerX += speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            facingDirection = Direction.UP;
            stateTime += delta;
            currentAnimation = playerAnimations.get("up");
            hitBox.height = 10;
            float potentialY = collisionBox.y + (speed * delta) + 1;
            if (!isCellBlocked(collisionBox.x, potentialY + collisionBox.height) && !isCellBlocked(collisionBox.x + collisionBox.height, potentialY + collisionBox.height)) {
                playerY += speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            facingDirection = Direction.DOWN;
            stateTime += delta;
            currentAnimation = playerAnimations.get("down");
            hitBox.height = 15;
            float potentialY = collisionBox.y - (speed * delta);
            if (!isCellBlocked(collisionBox.x, potentialY) && !isCellBlocked(collisionBox.x + collisionBox.width, potentialY)) {
                playerY -= speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            attacking = true;
            attackStateTime += delta;
            if (isSwingingSword()) {
                updateAttackBox(facingDirection);
                attackSound.play();
            }
        }

        this.collisionBox.setLocation((int) playerX + 4, (int) playerY + 6);
        this.hitBox.setLocation((int) playerX + 4, (int) playerY + 8);
    }

    private boolean isCellBlocked(float x, float y) {
        TiledMapTileLayer.Cell cell = collisionLayer.getCell((int) (x / 16), (int) (y / 16));
        return cell != null && cell.getTile() != null && cell.getTile().getId() == 0;
    }

    public void draw (Batch batch) {
        if (attacking) {
            if (facingDirection.equals(Direction.LEFT)) {
                batch.draw(
                        playerAnimations.get("attack-left").getKeyFrame(attackStateTime * 1.2f, true),
                        playerX - 8,
                        playerY,
                        32,
                        32
                );
            } else if (facingDirection.equals(Direction.RIGHT)) {
                batch.draw(
                        playerAnimations.get("attack-right").getKeyFrame(attackStateTime * 1.2f, true),
                        playerX - 8,
                        playerY,
                        32,
                        32
                );
            } else if (facingDirection.equals(Direction.UP)) {
                batch.draw(
                        playerAnimations.get("attack-up").getKeyFrame(attackStateTime * 1.2f, true),
                        playerX - 8,
                        playerY,
                        32,
                        32
                );
            } else if (facingDirection.equals(Direction.DOWN)) {
                batch.draw(
                        playerAnimations.get("attack-down").getKeyFrame(attackStateTime * 1.2f, true),
                        playerX - 8,
                        playerY,
                        32,
                        32
                );
            }
        } else {
            batch.draw(
                    currentAnimation.getKeyFrame(stateTime, true),
                    playerX,
                    playerY,
                    playerWidth,
                    playerHeight
            );
        }
    }

    /**
     * Updates the location and size of the attackBox based on the player's facing direction.
     * <p>
     * This method adjusts the attackBox's position and dimensions to align with the player's
     * current direction. The attackBox represents the area where the player's attack can hit.
     * The method takes into account four possible directions: LEFT, RIGHT, UP, and DOWN.
     * Depending on the direction, the attackBox is repositioned and resized accordingly to
     * reflect the player's attack range and orientation.
     * </p>
     *
     * @param direction The direction the player is facing. It should be one of the enum values:
     *                  LEFT, RIGHT, UP, or DOWN. This parameter dictates how the attackBox
     *                  will be updated in terms of location and size.
     * @throws NullPointerException if the direction is null.
     * @see Direction
     */
    public void updateAttackBox(Direction direction) {
        switch (direction){
            case LEFT:
                attackBox.setLocation((int) playerX - 6, (int) playerY + 4);
                attackBox.setSize(7, 16);
                break;
            case RIGHT:
                attackBox.setLocation((int) playerX + 14, (int) playerY + 4);
                attackBox.setSize(7, 16);
                break;
            case UP:
                attackBox.setLocation((int) playerX, (int) playerY + 18);
                attackBox.setSize(16, 7);
                break;
            case DOWN:
                attackBox.setLocation((int) playerX, (int) playerY + 1);
                attackBox.setSize(16, 7);
                break;
        }
    }

    private Map<String, Animation<TextureRegion>> loadAnimations() {
        Map<String, Animation<TextureRegion>> animationMap = new HashMap<>();

        // define frame width, height and total frame per circle (there are 4 frames in 1 walking circle)
        int FRAME_WIDTH = 16;
        int FRAME_HEIGHT = 32;
        int ANIMATION_FRAMES = 4;

        // LibGDX Array to load each frame needed for the animation
        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        // walking down (1st row, 4 columns)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT));
        }
        animationMap.put("down", new Animation<>(0.1f, walkFrames));
        walkFrames.clear();

        // walking up (3rd row, 4 columns)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * FRAME_WIDTH, 2 * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
        }
        animationMap.put("up", new Animation<>(0.1f, walkFrames));
        walkFrames.clear();

        // walking left (4th row, 4 columns)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * FRAME_WIDTH, 3 * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
        }
        animationMap.put("left", new Animation<>(0.1f, walkFrames));
        walkFrames.clear();

        // walking right (2nd row, 4 columns)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT));
        }
        animationMap.put("right", new Animation<>(0.1f, walkFrames));
        walkFrames.clear();

        // attacking face down (32 x 32, 5th row)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * 32, 4 * FRAME_HEIGHT, 32, 32));
        }
        animationMap.put("attack-down", new Animation<>(.15f, walkFrames));
        walkFrames.clear();

        // attacking face up (32 x 32, 6th row)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * 32, 5 * FRAME_HEIGHT, 32, 32));
        }
        animationMap.put("attack-up", new Animation<>(.15f, walkFrames));
        walkFrames.clear();

        // attacking face right (32 x 32, 7th row)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * 32, 6 * FRAME_HEIGHT, 32, 32));
        }
        animationMap.put("attack-right", new Animation<>(.15f, walkFrames));
        walkFrames.clear();

        // attacking face left (32 x 32, 8th row)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * 32, 7 * FRAME_HEIGHT, 32, 32));
        }
        animationMap.put("attack-left", new Animation<>(.15f, walkFrames));
        walkFrames.clear();

        return animationMap;
    }

    /**
     * Determines if the player is currently swinging his sword during the attack animation cycle.
     * <p>
     * This method calculates the current animation frame index based on the attack state time.
     * It specifically checks if the animation is in the frames where the sword swing occurs,
     * typically identified by frame index 3. These frames are considered to represent
     * the sword swinging action in the attack cycle.
     * </p>
     *
     * @return {@code true} if the current animation frame is either 2 or 3, indicating a sword swing;
     *         {@code false} otherwise.
     *
     * @see Player#update(float, int, int, int)
     */
    public boolean isSwingingSword() {
        int frameIndex = (int) ((attackStateTime % (.15f * 4)) / .15f);
        if (frameIndex == 3) {
            return true;
        }
        return false;
    }

    public void takeDamage() {
        if (!isInvincible) {
            playerLives--;
            isInvincible = true;
            invincibility_timer = INVINCIBILITY_DURATION;
        }
    }

    public void applyKnockBack(Mob mob, float knockBackDistance) {
        knockBackVector = new Vector2(
                this.playerX - mob.getX(),
                this.playerY - mob.getY()
        );

        knockBackVector.nor();
        knockBackVector.scl(knockBackDistance);

        // set the knockBackTimer
        knockBackTime = KNOCKBACKDURATION;
        beingKnockedBack = true;
    }

    public Texture getSpriteSheet() {
        return spriteSheet;
    }

    public void setSpriteSheet(Texture spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    public float getPlayerX() {
        return playerX;
    }

    public void setPlayerX(float playerX) {
        this.playerX = playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public void setPlayerY(float playerY) {
        this.playerY = playerY;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getPlayerWidth() {
        return playerWidth;
    }

    public void setPlayerWidth(int playerWidth) {
        this.playerWidth = playerWidth;
    }

    public int getPlayerHeight() {
        return playerHeight;
    }

    public void setPlayerHeight(int playerHeight) {
        this.playerHeight = playerHeight;
    }

    public Map<String, Animation<TextureRegion>> getPlayerAnimations() {
        return playerAnimations;
    }

    public void setPlayerAnimations(Map<String, Animation<TextureRegion>> playerAnimations) {
        this.playerAnimations = playerAnimations;
    }

    public Animation<TextureRegion> getCurrentAnimation() {
        return currentAnimation;
    }

    public void setCurrentAnimation(Animation<TextureRegion> currentAnimation) {
        this.currentAnimation = currentAnimation;
    }

    public float getStateTime() {
        return stateTime;
    }

    public void setStateTime(float stateTime) {
        this.stateTime = stateTime;
    }

    public TiledMapTileLayer getCollisionLayer() {
        return collisionLayer;
    }

    public void setCollisionLayer(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
    }

    public Rectangle getCollisionBox() {
        return collisionBox;
    }

    public void setCollisionBox(Rectangle collisionBox) {
        this.collisionBox = collisionBox;
    }

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setHitBox(Rectangle hitBox) {
        this.hitBox = hitBox;
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public void setPlayerLives(int playerLives) {
        this.playerLives = playerLives;
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

    public boolean isHasKey() {
        return hasKey;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public float getAttackStateTime() {
        return attackStateTime;
    }

    public void setAttackStateTime(float attackStateTime) {
        this.attackStateTime = attackStateTime;
    }

    public Rectangle getAttackBox() {
        return attackBox;
    }

    public void setAttackBox(Rectangle attackBox) {
        this.attackBox = attackBox;
    }

    public void dispose() {
        spriteSheet.dispose();
        attackSound.dispose();
    }
}
