package dev.lyze.parallelworlds.screens.game.gamepads;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dev.lyze.parallelworlds.screens.game.entities.Player;
import dev.lyze.parallelworlds.screens.game.entities.PlayerColor;

public class KeyboardGamepad extends VirtualGamepad {
    public KeyboardGamepad(Player player) {
        super(player);
    }

    @Override
    public void update(float delta) {
        leftPressed = Gdx.input.isKeyPressed(player.getColor() == PlayerColor.Blue ? Input.Keys.A : Input.Keys.LEFT);
        rightPressed = Gdx.input.isKeyPressed(player.getColor() == PlayerColor.Blue ? Input.Keys.D : Input.Keys.RIGHT);
        jumpJustPressed = Gdx.input.isKeyJustPressed(player.getColor() == PlayerColor.Blue ? Input.Keys.W : Input.Keys.UP);
    }

    @Override
    public void reset(float delta) {

    }
}