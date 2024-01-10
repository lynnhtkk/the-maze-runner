package de.tum.cit.ase.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
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
    private final float INVINCIBILITY_FRAME;

    private int playerLives;

    private Map<String, Animation<TextureRegion>> playerAnimations;
    private Animation<TextureRegion> currentAnimation;

    private float sinusInput;

    public Player(float playerX, float playerY, TiledMapTileLayer collisionLayer) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.collisionLayer = collisionLayer;
        this.speed = 70f;
        this.playerWidth = 16;
        this.playerHeight = 32;
        this.playerLives = 3;
        isInvincible = false;
        invincibility_timer = 0f;
        INVINCIBILITY_FRAME = 2f;
        this.collisionBox = new Rectangle((int) playerX + 4, (int) playerY + 6, (int) (playerWidth * 0.5), (int) (playerHeight * 0.2));
        this.hitBox = new Rectangle((int) playerX + 3, (int) playerY + 8, 10, 15);
        this.sinusInput = 0f;
        this.spriteSheet = new Texture(Gdx.files.internal("character.png"));
        this.playerAnimations = this.loadAnimations();
        this.currentAnimation = this.playerAnimations.get("down");
    }

    public void update(float delta, int mapWidth, int mapHeight, int borderTiles) {
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
            sinusInput += delta;
            currentAnimation = playerAnimations.get("left");
            float potentialX = collisionBox.x - (speed * delta);
            if (!isCellBlocked(potentialX, collisionBox.y) && !isCellBlocked(potentialX, collisionBox.y + collisionBox.height)) {
                playerX -= speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            sinusInput += delta;
            currentAnimation = playerAnimations.get("right");
            float potentialX = collisionBox.x + (speed * delta) + 1;
            if (!isCellBlocked(potentialX + collisionBox.width, collisionBox.y) && !isCellBlocked(potentialX + collisionBox.width, collisionBox.y + collisionBox.height)) {
                playerX += speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            sinusInput += delta;
            currentAnimation = playerAnimations.get("up");
            float potentialY = collisionBox.y + (speed * delta) + 1;
            if (!isCellBlocked(collisionBox.x, potentialY + collisionBox.height) && !isCellBlocked(collisionBox.x + collisionBox.height, potentialY + collisionBox.height)) {
                playerY += speed * delta;
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            sinusInput += delta;
            currentAnimation = playerAnimations.get("down");
            float potentialY = collisionBox.y - (speed * delta);
            if (!isCellBlocked(collisionBox.x, potentialY) && !isCellBlocked(collisionBox.x + collisionBox.width, potentialY)) {
                playerY -= speed * delta;
            }
        }

        this.collisionBox.setLocation((int) playerX + 4, (int) playerY + 6);
        this.hitBox.setLocation((int) playerX + 3, (int) playerY + 8);
    }

    private boolean isCellBlocked(float x, float y) {
        TiledMapTileLayer.Cell cell = collisionLayer.getCell((int) (x / 16), (int) (y / 16));
        return cell != null && cell.getTile() != null && cell.getTile().getId() == 0;
    }

    public void draw (Batch batch) {
        batch.draw(
                currentAnimation.getKeyFrame(sinusInput, true),
                playerX,
                playerY,
                playerWidth,
                playerHeight
        );
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

        return animationMap;
    }

    public void takeDamage() {
        if (!isInvincible) {
            playerLives--;
            isInvincible = true;
            invincibility_timer = INVINCIBILITY_FRAME;
        }
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

    public float getSinusInput() {
        return sinusInput;
    }

    public void setSinusInput(float sinusInput) {
        this.sinusInput = sinusInput;
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

    public void dispose() {
        spriteSheet.dispose();
    }
}
