package de.tum.cit.ase.maze;

import com.badlogic.gdx.graphics.g2d.Batch;

import java.awt.*;

public abstract class Mob {

    private float x;
    private float y;

    private Rectangle hitBox;

    public Mob(float x, float y, int hitBoxWidth, int hitBoxHeight) {
        this.x = x;
        this.y = y;
        hitBox = new Rectangle((int) x, (int) y, hitBoxWidth, hitBoxHeight);
    }

    public abstract void update(float delta);

    public abstract void draw(Batch batch);

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

    public Rectangle getHitBox() {
        return hitBox;
    }

    public void setHitBox(Rectangle hitBox) {
        this.hitBox = hitBox;
    }

    public abstract void dispose();
}

