package dev.lyze.parallelworlds.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.impl.BlendingTransition;
import dev.lyze.parallelworlds.logger.Logger;
import dev.lyze.parallelworlds.screens.EndScene;
import dev.lyze.parallelworlds.screens.LoadingScreen;
import dev.lyze.parallelworlds.screens.game.gamepads.VirtualGamepadGroup;
import dev.lyze.parallelworlds.statics.Statics;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Objects;

public class GameScreen extends ManagedScreen {
    private static final Logger<GameScreen> logger = new Logger<>(GameScreen.class);

    private final Stage ui = new Stage(new ExtendViewport(1280, 720));
    private final Stage mobileUi = new Stage(new ExtendViewport(320 * 0.75f, 160 * 0.75f));

    private Label coinLabel, mapTextLabel;

    @Getter
    private String mapPath;

    @Getter
    private Level level;

    private ArrayList<VirtualGamepadGroup> gamepads = new ArrayList<>();

    private int totalDeaths, totalCoins;

    @Override
    protected void create() {
        var root = new Table();
        root.setFillParent(true);

        var leftInnerTable = new Table();
        leftInnerTable.add(new Image(Statics.assets.getGame().getParticlesAtlas().getCoins_idle().first())).size(25);
        coinLabel = new Label("0", Statics.assets.getGame().getSkin());
        leftInnerTable.add(coinLabel).padLeft(12).padTop(6);

        var rightInnerTable = new Table();
        mapTextLabel = new Label("", Statics.assets.getGame().getSkin());
        rightInnerTable.add(mapTextLabel).padLeft(12).padTop(6);

        root.add(leftInnerTable).expand().top().left().padLeft(12).padTop(12);
        root.add(rightInnerTable).expand().top().right().padRight(12).padTop(12);
        ui.addActor(root);

        var hiddenTable = new Table();
        hiddenTable.setFillParent(true);
        var pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        var skipButton = new ImageButton(new TextureRegionDrawable(new Texture(pixmap)));
        skipButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                level.loadNextLevel();
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        hiddenTable.add(skipButton).expand().top().right();
        mobileUi.addActor(hiddenTable);

        addInputProcessor(mobileUi);
    }

    @Override
    public void show() {
        super.show();

        mapPath = (String) Objects.requireNonNull(pushParams)[0];
        var map = Statics.assets.getGame().get(mapPath);
        level = new Level(this, map);
        level.initialize();

        mapTextLabel.setText(level.getMap().getText());

        gamepads.forEach(VirtualGamepadGroup::dispose);
        gamepads.clear();
        level.getPlayers().getPlayers().forEach(p -> gamepads.add(new VirtualGamepadGroup(p, gamepads.size(), mobileUi)));

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private float actualDeltaTime = 0.0f;
    private final float targetDeltaTime = 0.01f;
    private double currentTime = System.currentTimeMillis();
    private float accumulator = 0f;

    @Override
    public void render(float delta) {
        var newTime = System.currentTimeMillis();
        var frameTime = (newTime - currentTime) / 1000f;
        accumulator += frameTime;
        currentTime = newTime;

        while (accumulator >= targetDeltaTime) {
            update();

            accumulator -= targetDeltaTime;
            actualDeltaTime = targetDeltaTime;
        }

        render();
    }

    private void update() {
        gamepads.forEach(g -> g.update(actualDeltaTime));

        level.update(actualDeltaTime);
        coinLabel.setText(level.getCoinCount());

        ui.getViewport().apply();
        ui.act(actualDeltaTime);

        if (Statics.isMobileDevice) {
            mobileUi.getViewport().apply();
            mobileUi.act(actualDeltaTime);
        }
        gamepads.forEach(g -> g.reset(actualDeltaTime));
    }

    private void render() {
        Gdx.gl.glClearColor(0.2f, 0.1f, 0.4f, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        level.render();

        ui.getViewport().apply();
        ui.draw();

        if(Statics.isMobileDevice) {
            mobileUi.getViewport().apply();
            mobileUi.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        level.resize(width, height);
        ui.getViewport().update(width, height, true);
        mobileUi.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    public void restartLevel() {
        totalDeaths++;
        setLevel(mapPath);
    }

    public void nextLevel(String mapPath) {
        totalCoins += level.getCoinCount();

        setLevel(mapPath);
    }

    private void setLevel(String mapPath) {
        logger.logInfo("Loading level " + mapPath);
        level.dispose();

        if (mapPath == null)
            Statics.parallelWorlds.getScreenManager().pushScreen(EndScene.class.getName(), BlendingTransition.class.getName(), totalCoins, totalDeaths);
        else
            Statics.parallelWorlds.getScreenManager().pushScreen(LoadingScreen.class.getName(), BlendingTransition.class.getName(), mapPath);
    }
}