package dev.lyze.parallelworlds.screens.game.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.Collision;
import com.dongbat.jbump.Collisions;
import com.dongbat.jbump.Response;
import com.dongbat.jbump.World;
import dev.lyze.parallelworlds.logger.Logger;
import dev.lyze.parallelworlds.screens.game.Level;
import dev.lyze.parallelworlds.screens.game.entities.filters.PlayerCollisionFilter;
import dev.lyze.parallelworlds.utils.MathUtils;
import dev.lyze.parallelworlds.utils.Vector3Pool;

public class AiEntity extends Entity {
    private static final Logger<AiEntity> logger = new Logger<>(AiEntity.class);

    private final float gravity = -4f;
    private final float movementSpeedIncrease = 10f;
    private final float maxSpeed = 20f;
    private final float friction = 2f;

    private final float jumpForce = 50f;

    private final int maxJumpsLeft = 2;

    protected final Vector2 velocity = new Vector2();
    protected final Vector2 inputVelocity = new Vector2();

    protected boolean isFacingRight = true;

    protected boolean invertedGravity = false;

    protected boolean wantsToMoveLeft;
    protected boolean wantsToMoveRight;
    protected boolean wantsToJump;

    private boolean isJumping;
    private boolean isGrounded;
    private int jumpsLeft = maxJumpsLeft;

    private final Collisions tempCollisions = new Collisions();
    private final GlyphLayout debugGlyphLayout = new GlyphLayout();

    public AiEntity(float x, float y, float width, float height, Level level) {
        super(x, y, width, height, level);
    }

    @Override
    public void update(World<Entity> world, float delta) {
        super.update(world, delta);

        setInput();
        checkGround(world);
        checkJump();
        checkMovementDirection();

        applyInput();
        applyGravity();
        applyFriction();

        checkCollisionsAndApplyVelocity(world, delta);
    }

    private void checkGround(World<Entity> world) {
        world.project(item, position.x, position.y, width, height, position.x, position.y - fixInverted(0.1f), PlayerCollisionFilter.instance, tempCollisions);
        isGrounded = tempCollisions.size() > 0;
    }

    private void checkJump() {
        if (!wantsToJump)
            return;


        if (isGrounded && !isJumping) {
            jumpsLeft = maxJumpsLeft - 1;
            velocity.y += fixInverted(jumpForce);
            isJumping = true;
        }
        else if (!isGrounded && jumpsLeft > 0) {
            velocity.y += fixInverted(jumpForce);
            jumpsLeft--;
        }
    }

    private void checkCollisionsAndApplyVelocity(World<Entity> world, float delta) {
        var response = world.move(item, position.x + velocity.x * delta, position.y + velocity.y * delta, PlayerCollisionFilter.instance);

        for (int i = 0; i < response.projectedCollisions.size(); i++) {
            onCollision(response.projectedCollisions.get(i));
        }

        position.set(response.goalX, response.goalY);
    }

    protected void onCollision(Collision collision) {
        if (collision.type != Response.slide)
            return;

        if (collision.normal.x != 0) {
            // wall
            velocity.x = 0;
        }
        if (collision.normal.y != 0) {
            // ceiling or floor
            velocity.y = 0;

            if (collision.normal.y == fixInverted(1)) {
                isJumping = false;
            }
        }
    }

    private void setInput() {
        inputVelocity.set(wantsToMoveLeft ? -movementSpeedIncrease : wantsToMoveRight ? movementSpeedIncrease : 0, 0);
    }

    private void checkMovementDirection() {
        if (isFacingRight && inputVelocity.x < 0) {
            flip();
        }
        else if (!isFacingRight && inputVelocity.x > 0) {
            flip();
        }
    }

    private void flip() {
        isFacingRight = !isFacingRight;
    }

    private void applyInput() {
        if (inputVelocity.x > 0) {
            velocity.x = MathUtils.approach(velocity.x, maxSpeed, inputVelocity.x);
        } else if (inputVelocity.x < 0) {
            velocity.x = MathUtils.approach(velocity.x, -maxSpeed, inputVelocity.x);
        }
    }

    private void applyFriction() {
        if (inputVelocity.x == 0)
            velocity.x = MathUtils.approach(velocity.x, 0, friction);
    }

    private void applyGravity() {
        velocity.y += fixInverted(gravity);

        if (isJumping && invertedGravity ? (velocity.y > 0) : (velocity.y < 0))
            isJumping = false;
    }

    private float fixInverted(float val) {
        return invertedGravity ? -val : val;
    }

    public void debugTextRender(BitmapFont font, Camera cam, SpriteBatch screenBatch) {
        var pos = Vector3Pool.instance.obtain();
        pos.set(position, 0);
        pos.add(width, height, 0);

        cam.project(pos);

        font.draw(screenBatch,
                "Pos: " + position.x + "/" + position.y + "\n" +
                "Jum: " + isJumping + " ; Grn: " + isGrounded + "\n" +
                "Vel: " + velocity.x + "/" + velocity.y + "\n" +
                "JuL: " + jumpsLeft  + "\n" +
                "Grv: " + fixInverted(gravity),
                pos.x, pos.y);




        String str = "WJ: " + wantsToJump + "\n" +
                "WL: " + wantsToMoveLeft + "\n" +
                "WR: " + wantsToMoveRight + "\n";

        debugGlyphLayout.setText(font, str);

        pos.set(position, 0);
        pos.add(0, height, 0);
        cam.project(pos);

        font.draw(screenBatch, str, pos.x - debugGlyphLayout.width, pos.y);
        Vector3Pool.instance.free(pos);
    }
}
