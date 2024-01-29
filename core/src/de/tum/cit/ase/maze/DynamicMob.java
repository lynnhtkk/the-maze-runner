package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a dynamic mob (moving entity) in the game.
 * <p>
 * This class extends the {@link Mob} class to create dynamic entities in the game that are capable
 * of movement, reacting to attacks, and being killed. Unlike static mobs, dynamic mobs can move
 * in random directions, are subject to knockback when attacked, and have a finite number of lives (typically three).
 * Once all lives are lost, the dynamic mob is considered destroyed. The dynamic nature of these mobs
 * adds complexity and challenge to the game, requiring players to interact with them in various ways.
 * The class includes functionality for handling the mob's movement, response to attacks (including knockback),
 * and tracking the number of lives for determining when the mob is destroyed.
 * </p>
 */
public class DynamicMob extends Mob {

    private Animation<TextureRegion> animation;
    private Texture spriteSheet;

    private Vector2 originalPosition;
    private Vector2 targetPosition;
    private float movableRange;
    private float speed;

    private int lives;

    private boolean isInvincible;
    private float invincibility_timer;
    private final float INVINCIBILITY_DURATION;

    private Vector2 knockBackVector;
    private float knockBackTime;
    private final float KNOCKBACKDURATION;
    private boolean beingKnockedBack;

    private TiledMapTileLayer collisionLayer;


    /**
     * Constructs a new DynamicMob with specified initial position and collision layer of the map.
     * <p>
     * This constructor initializes a DynamicMob with a specific position on the map
     * and associates it with a given collision layer. It sets various properties of the mob,
     * such as its movable range, speed, lives, and spritesheet. It also initializes default
     * values for knockback duration, invincibility state, and invincibility duration. After
     * setting these properties, it loads the animation for the mob.
     * </p>
     *
     * @param x The initial x-coordinate of the mob.
     * @param y The initial y-coordinate of the mob.
     * @param collisionLayer The tiled map tile layer representing the collision layer of the map,
     *                       used for collision detection.
     *
     * @see TiledMapTileLayer
     * @see Vector2
     * @see Texture
     */
    public DynamicMob(float x, float y, TiledMapTileLayer collisionLayer) {
        super(x, y, 8, 6);
        this.collisionLayer = collisionLayer;
        originalPosition = new Vector2(x, y);
        targetPosition = new Vector2(x, y);
        movableRange = 32f;
        speed = 20f;
        lives = 3;
        spriteSheet = new Texture(Gdx.files.internal("mobs.png"));
        KNOCKBACKDURATION = 1f;
        beingKnockedBack = false;
        isInvincible = false;
        INVINCIBILITY_DURATION = 1f;
        loadAnimation();
    }


    /**
     * Updates the state and behavior of the DynamicMob in each frame.
     * <p>
     * This method is called during each frame refresh in the game's render loop, as part of the GameScreen class.
     * It performs several key functions to update the mob's state:
     * <ul>
     *     <li>Updates the {@code stateTime}.</li>
     *     <li>Updates the position of the mob and aligns the hitbox with the mob's position.</li>
     *     <li>Manages the invincibility state of the mob, providing a brief duration during which the mob cannot be damaged again.</li>
     *     <li>Applies a knockback effect if the mob is in a knocked back state, with collision checks for this movement as well.</li>
     * </ul>
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds. Used for calculating movement and timing.
     * @throws NullPointerException if any required game objects or states are null.
     *
     * @see Vector2
     * @see GameScreen#render(float)
     */
    @Override
    public void update(float delta) {
        super.stateTime += delta;

        moveTowardsRandomTarget(delta);

        super.getHitBox().setLocation((int) super.getX() + 4, (int) super.getY() + 6);

        updateInvincibility(delta);

        applyKnockBackEffect(delta);
    }


    /**
     * Moves the mob towards a randomly chosen target position.
     * <p>
     * This method updates the mob's position, moving it towards a previously set target position.
     * The target position is determined by the {@code chooseNewTargetPosition} method. Movement
     * towards this target occurs each frame, based on the time elapsed since the last frame
     * (represented by {@code delta}). The mob's new position is chosen in two scenarios:
     * <ol>
     *     <li>When the mob reaches its current target position.</li>
     *     <li>When the mob encounters a wall, indicated by a collision detection.</li>
     * </ol>
     * If either of these conditions is met, a new target position is chosen, and the mob
     * moves towards this new destination.
     * </p>
     *
     * @param delta The time span between the current and the last frame in seconds.
     * @see Vector2
     */
    private void moveTowardsRandomTarget(float delta) {
        Vector2 currentPosition = new Vector2(super.getX(), super.getY());
        if (!currentPosition.epsilonEquals(targetPosition, 1f)) {
            Vector2 moveDirection = targetPosition.cpy().sub(currentPosition).nor();
            // check if the mob is going to collide with the wall
            Vector2 potentialPosition = currentPosition.cpy().add(moveDirection.scl(speed * delta));
            if (!isCellBlocked(potentialPosition.x + 2, potentialPosition.y + 2)) {
                super.setX(potentialPosition.x);
                super.setY(potentialPosition.y);
            } else {
                chooseNewTargetPosition();
            }
        } else {
            chooseNewTargetPosition();
        }
    }


    /**
     * Updates the mob's invincibility state.
     * <p>
     * This method counts down the invincibility timer. Once the timer runs out,
     * it sets the mob's invincibility state to false.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds.
     */
    private void updateInvincibility(float delta) {
        if (isInvincible) {
            invincibility_timer -= delta;
            if (invincibility_timer <= 0) {
                isInvincible = false;
            }
        }
    }


    /**
     * Applies the knockback effect to the mob when the {@code beingKnockedBack} flag is set.
     * <p>
     * This method manages the knockback movement of the mob each frame, creating a natural
     * effect where the speed of the knockback decreases over time and stops when knockback timer {@code knockBackTime} is up.
     * The knockback is executed by adjusting the mob's position according to the {@code knockBackVector},
     * which is scaled down as the {@code knockBackTime} decreases.
     * The method performs the following actions:
     * <ul>
     *     <li>Counts down the knockback timer each frame based on {@code delta}.</li>
     *     <li>Calculates the knockback factor and updates the mob's position for each frame.</li>
     *     <li>Checks for potential collisions and stops the knockback if a collision is detected.</li>
     *     <li>Resets the {@code beingKnockedBack} flag to false when the knockback timer runs out.</li>
     * </ul>
     * The knockback effect ends either when the timer runs out or when a collision is detected.
     * </p>
     *
     * @param delta The time span between the current and last frame in seconds, used to update
     *              the knockback effect and count down the timer.
     */
    private void applyKnockBackEffect(float delta) {
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
                Vector2 potentialPosition = new Vector2(super.getX(), super.getY()).add(knockBackThisFrame);
                if (!isCellBlocked(potentialPosition.x + 16 / 2, potentialPosition.y + 16 /2)) {
                    super.setX(super.getX() + knockBackThisFrame.x);
                    super.setY(super.getY() + knockBackThisFrame.y);
                } else {
                    beingKnockedBack = false;
                }
            }
        }
    }


    /**
     * Draws the mob with its current animation frame.
     * <p>
     * This method overrides the draw method to render the animated mob on the screen.
     * It retrieves the appropriate frame from the animation sequence based on the
     * current state time and then draws it at the mob's current position. The size of
     * the drawn frame is set to a fixed width and height. This method is typically
     * called within the game's rendering loop to continuously update the mob's appearance
     * on the screen.
     * </p>
     *
     * @param batch The batch used to draw the texture.
     * @throws NullPointerException if {@code batch} is null.
     *
     * @see Batch
     * @see Animation
     */
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
     * Loads the movement animation for this dynamic mob.
     * <p>
     * This private method initializes the animation for the mob's movement by creating
     * a sequence of texture frames. The animation is constructed from a sprite sheet
     * where each frame represents a different stage of the mob's walking animation.
     * The method sets up a specific number of frames (defined by {@code FRAMES}), each
     * with a specified width and height (defined by {@code FRAME_WIDTH} and {@code FRAME_HEIGHT}).
     * These frames are then added to the animation sequence. The animation is stored in
     * the {@code animation} field of the DynamicMob class.
     * </p>
     *
     * @see Texture
     * @see TextureRegion
     * @see Animation
     */
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


    /**
     * Selects a random target position within a defined range for the dynamic mob's movement.
     * <p>
     * This private method is used to give a sense of intelligent movement to the mob by randomly
     * choosing a new destination to move towards. The target position is calculated within a
     * specified range of the mob's original position, ensuring that the mob stays within a
     * certain area. The method uses the {@code originalPosition} of the mob and a defined
     * {@code movableRange} to calculate a new target position. The new position is set as
     * {@code targetPosition} of the mob, which can then be used by other methods to move
     * the mob towards this point.
     * </p>
     *
     * @since 1.0 // Replace with the actual version of your API
     * @see Vector2 // Replace with the actual link to the Vector2 class, if available
     */
    private void chooseNewTargetPosition() {
        float randomX = MathUtils.random(originalPosition.x - movableRange, originalPosition.x + movableRange);
        float randomY = MathUtils.random(originalPosition.y - movableRange, originalPosition.y + movableRange);
        targetPosition.set(randomX, randomY);
    }


    /**
     * Determines if a specified map cell is blocked, indicating a wall.
     * <p>
     * This method checks if a cell at given coordinates in the {@code collisionLayer} is blocked.
     * The {@code collisionLayer} is the second layer of the map object, parsed during
     * the creation of a {@code DynamicMob} instance. A cell is considered blocked
     * (representing a wall) if the following conditions are met:
     * - The cell at the specified coordinates is not null.
     * - The tile of the cell is not null.
     * - The ID of the tile is 0, which is used to indicate a wall in the map design.
     * The method returns true if all these conditions are satisfied, signifying the cell is blocked.
     * </p>
     *
     * @param x The x-coordinate of the cell to check, in map units.
     * @param y The y-coordinate of the cell to check, in map units.
     * @return {@code true} if the cell is blocked, {@code false} otherwise.
     *
     * @see TiledMapTileLayer
     */
    private boolean isCellBlocked(float x, float y) {
        TiledMapTileLayer.Cell cell = collisionLayer.getCell((int) (x / 16), (int) (y / 16));
        return cell != null && cell.getTile() != null && cell.getTile().getId() == 0;
    }


    /**
     * Applies a knockback effect to this dynamic mob when attacked by a player.
     * <p>
     * This method sets the mob into a knockback state in response to an attack by a player.
     * The direction and distance of the knockback are determined based on the player's attacking
     * direction and the relative positions of the player and the mob. The {@code knockBackVector}
     * is calculated to represent the direction of the knockback, and is scaled by the specified
     * knockback distance. The method also initializes {@code knockBackTime} to {@code KNOCKBACKDURATION},
     * starting the timer for the knockback animation, and sets {@code beingKnockedBack} to true,
     * indicating the mob is in the knockback state.
     * </p>
     *
     * @param player The player attacking the mob, used to calculate the direction of the knockback.
     * @param knockBackDistance The distance the mob should be knocked back. This value scales
     *                          the {@code knockBackVector}.
     * @throws NullPointerException if {@code player} is null.
     *
     * @see Vector2
     * @see Player#getFacingDirection()
     */
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

