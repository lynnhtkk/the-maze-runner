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
        attackSound = Gdx.audio.newSound(Gdx.files.internal("swing.wav"));
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

        // apply knock back effect if beingKnockedBack is true
        if (beingKnockedBack) {
            // count down the knock back timer
            knockBackTime -= delta;
            // if the timer is up (reaches 0), set beingKnockedBack to false again
            if (knockBackTime <= 0) {
                beingKnockedBack = false;
            } else {
                float knockBackFactor = knockBackTime / KNOCKBACKDURATION;
                Vector2 knockBackThisFrame = knockBackVector.cpy().scl(knockBackFactor);

                // check for collision using potential position
                Vector2 potentialPosition = new Vector2(collisionBox.x, collisionBox.y).add(knockBackThisFrame);
                if (knockBackThisFrame.x < 0) {
                    if (!isCellBlocked(potentialPosition.x, potentialPosition.y) && !isCellBlocked(potentialPosition.x, potentialPosition.y + collisionBox.height)) {
                        playerX += knockBackThisFrame.x;
                    } else {
                        beingKnockedBack = false;
                    }
                } else if (knockBackThisFrame.x > 0) {
                    potentialPosition.add(1, 0);
                    if (!isCellBlocked(potentialPosition.x + collisionBox.width, potentialPosition.y) && !isCellBlocked(potentialPosition.x + collisionBox.width, potentialPosition.y + collisionBox.height)) {
                        playerX += knockBackThisFrame.x;
                    } else {
                        beingKnockedBack = false;
                    }
                } else if (knockBackThisFrame.y < 0) {
                    if (!isCellBlocked(potentialPosition.x, potentialPosition.y) && !isCellBlocked(potentialPosition.x + collisionBox.width, potentialPosition.y)) {
                        playerY += knockBackThisFrame.y;
                    } else {
                        beingKnockedBack = false;
                    }
                }
                else if (knockBackThisFrame.y > 0) {
                    potentialPosition.add(0, 1);
                    if (!isCellBlocked(potentialPosition.x, potentialPosition.y + collisionBox.height) && !isCellBlocked(potentialPosition.x + collisionBox.width, potentialPosition.y + collisionBox.height)) {
                        playerY += knockBackThisFrame.y;
                    } else {
                        beingKnockedBack = false;
                    }
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
        if (playerX > (mapWidth + borderTiles - 1) * 16) playerX = (mapWidth + borderTiles - 1) * 16;
        if (playerY < borderTiles * 16) playerY = borderTiles * 16;
        if (playerY > (mapHeight + borderTiles - 1) * 16) playerY = (mapHeight + borderTiles - 1) * 16;

        // move player according to the input
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            facingDirection = Direction.LEFT;
            stateTime += delta;
            currentAnimation = playerAnimations.get("left");
            float potentialX = collisionBox.x - (speed * delta);
            if (!isCellBlocked(potentialX, collisionBox.y) && !isCellBlocked(potentialX, collisionBox.y + collisionBox.height)) {
                playerX -= speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            facingDirection = Direction.RIGHT;
            stateTime += delta;
            currentAnimation = playerAnimations.get("right");
            float potentialX = collisionBox.x + (speed * delta) + 1;
            if (!isCellBlocked(potentialX + collisionBox.width, collisionBox.y) && !isCellBlocked(potentialX + collisionBox.width, collisionBox.y + collisionBox.height)) {
                playerX += speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            facingDirection = Direction.UP;
            stateTime += delta;
            currentAnimation = playerAnimations.get("up");
            float potentialY = collisionBox.y + (speed * delta) + 1;
            if (!isCellBlocked(collisionBox.x, potentialY + collisionBox.height) && !isCellBlocked(collisionBox.x + collisionBox.height, potentialY + collisionBox.height)) {
                playerY += speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            facingDirection = Direction.DOWN;
            stateTime += delta;
            currentAnimation = playerAnimations.get("down");
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

        // update the size and location of player's hit box accordingly
        this.hitBox.setLocation((int) playerX + 4, (int) playerY + 8);
        switch (facingDirection) {
            case UP, DOWN:
                hitBox.setSize(8, 10);
                break;
            case LEFT, RIGHT:
                hitBox.setSize(8, 15);
                break;
        }
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
        animationMap.put("attack-down", new Animation<>(.12f, walkFrames));
        walkFrames.clear();

        // attacking face up (32 x 32, 6th row)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * 32, 5 * FRAME_HEIGHT, 32, 32));
        }
        animationMap.put("attack-up", new Animation<>(.12f, walkFrames));
        walkFrames.clear();

        // attacking face right (32 x 32, 7th row)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * 32, 6 * FRAME_HEIGHT, 32, 32));
        }
        animationMap.put("attack-right", new Animation<>(.12f, walkFrames));
        walkFrames.clear();

        // attacking face left (32 x 32, 8th row)
        for (int col = 0; col < ANIMATION_FRAMES; col++) {
            walkFrames.add(new TextureRegion(spriteSheet, col * 32, 7 * FRAME_HEIGHT, 32, 32));
        }
        animationMap.put("attack-left", new Animation<>(.12f, walkFrames));
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
        int frameIndex = (int) ((attackStateTime % (.12f * 4)) / .12f);
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

    /**
     * Applies a knockback effect to the player based on the position of the colliding mob.
     * <p>
     * This method sets the player into a knockedback state, where the player is momentarily pushed
     * away from the mob they have collided with. The direction and distance of the knockback are
     * determined by the player's current facing direction and the position of the mob. The
     * {@code knockBackVector} is calculated to represent this direction and is scaled by the
     * specified knockback distance. The method also initializes {@code knockBackTime} to
     * {@code KNOCKBACKDURATION}, starting the timer for the knockback animation, and sets
     * {@code beingKnockedBack} to true, indicating the player is in the knockback state.
     * </p>
     *
     * @param mob The mob with which the player has collided. This is used to calculate the
     *            direction of the knockback.
     * @param knockBackDistance The distance the player should be knocked back. This value
     *                          scales the {@code knockBackVector}.
     * @throws NullPointerException if {@code mob} is null.
     *
     * @see Mob
     */
    public void applyKnockBack(Mob mob, float knockBackDistance) {
        switch (facingDirection) {
            case UP, DOWN:
                knockBackVector = new Vector2(0, playerY - mob.getY());
                break;
            case LEFT, RIGHT:
                knockBackVector = new Vector2(playerX - mob.getX(), 0);
                break;
        }

        knockBackVector.nor();
        knockBackVector.scl(knockBackDistance);

        knockBackTime = KNOCKBACKDURATION;
        beingKnockedBack = true;
    }

    public void increaseSpeed() {
        // When hitting an apple, increase speed
        speed *= 2;
    }

    public void resetSpeed() {
        // Return to normal speed
        speed = 80f;
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
