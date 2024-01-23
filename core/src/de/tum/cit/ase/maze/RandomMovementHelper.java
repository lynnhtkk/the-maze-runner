package de.tum.cit.ase.maze;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class RandomMovementHelper {
    public Vector2 chooseNewTargetPosition(Vector2 originalPosition, float movableRange, int mapWidth, int mapHeight) {
        // Ensure the target position is within the map boundaries
        float minX = Math.max(originalPosition.x - movableRange, 0); // Lower bound
        float maxX = Math.min(originalPosition.x + movableRange, mapWidth); // Upper bound
        float minY = Math.max(originalPosition.y - movableRange, 0); // Lower bound
        float maxY = Math.min(originalPosition.y + movableRange, mapHeight); // Upper bound

        float randomX = MathUtils.random(minX, maxX);
        float randomY = MathUtils.random(minY, maxY);

        return null;
    }
}
