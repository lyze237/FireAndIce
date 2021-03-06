package dev.lyze.parallelworlds.screens.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dongbat.jbump.World;
import dev.lyze.parallelworlds.logger.Logger;
import dev.lyze.parallelworlds.screens.game.entities.Entity;
import dev.lyze.parallelworlds.statics.Statics;
import lombok.Getter;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;

public class Level {
    private static final Logger<Level> logger = new Logger<>(Level.class);
    private final Viewport viewport = new ExtendViewport(80, 40, new GameCamera());
    private final GameScreen game;

    private final SpriteBatch spriteBatch = new SpriteBatch();
    private final ShapeDrawer shapeDrawer;

    @Getter
    private final Map map;

    @Getter
    private final World<Entity> world;

    @Getter
    private final Players players;
    private boolean playersDead;

    @Getter
    private int coinCount;

    @Getter
    private final ArrayList<Entity> entities = new ArrayList<>();
    private final ArrayList<Entity> entitiesToAdd = new ArrayList<>();
    private final ArrayList<Entity> entitiesToRemove = new ArrayList<>();

    private final BitmapFont debugFont;

    private boolean finished;

    public Level(GameScreen game, TiledMap tiledMap) {
        this.game = game;

        world = new World<>(4);
        map = new Map(game, tiledMap);

        players = new Players(this);

        shapeDrawer = new ShapeDrawer(spriteBatch, new TextureRegion(Statics.assets.getGame().getPixel()));
        shapeDrawer.setDefaultLineWidth(0.1f);

        debugFont = Statics.assets.getMainMenu().getSkin().getFont("Debug");
    }

    public void initialize() {
        map.initialize();
        map.getMusic().setLooping(true);
        map.getMusic().play();

        for (int i = 0; i < 100; i++) {
            ((GameCamera) viewport.getCamera()).update(players.getFirePlayer().getPosition(), players.getIcePlayer().getPosition(), map.getBoundaries(), 0.1f);
        }
    }

    public void update(float delta) {
        viewport.apply();

        if (playersDead || finished)
            return;

        players.update(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F12))
            killPlayer();
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11))
            loadNextLevel();

        entities.forEach(e -> e.update(world, delta));
        if (entitiesToAdd.size() > 0) {
            entities.addAll(entitiesToAdd);
            entitiesToAdd.clear();
        }

        entitiesToRemove.forEach(e -> {
            entities.remove(e);
            world.remove(e.getItem());
        });
        entitiesToRemove.clear();

        ((GameCamera) viewport.getCamera()).update(players.getFirePlayer().getPosition(), players.getIcePlayer().getPosition(), map.getBoundaries(), delta);
    }

    public void render() {
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        spriteBatch.setColor(map.getTopColor());
        spriteBatch.draw(Statics.assets.getGame().getPixel(), 0, map.getMapHeight() / 2f, map.getMapWidth(), map.getMapHeight());
        spriteBatch.setColor(map.getBottomColor());
        spriteBatch.draw(Statics.assets.getGame().getPixel(), 0, map.getMapHeight() / 2f, map.getMapWidth(), -map.getMapHeight());
        spriteBatch.end();

        map.render((OrthographicCamera) viewport.getCamera());

        spriteBatch.begin();

        spriteBatch.setColor(Color.WHITE);
        players.render(spriteBatch);
        entities.forEach(e -> e.render(spriteBatch));
        spriteBatch.end();

        //DEBUG LINES
        if (!Statics.debugging)
            return;

        spriteBatch.begin();
        players.debugRender(shapeDrawer);

        shapeDrawer.setColor(Color.GREEN);
        entities.forEach(e -> e.debugRender(shapeDrawer));

        shapeDrawer.setColor(Color.CYAN);
        map.debugRender(shapeDrawer);
        shapeDrawer.circle(viewport.getCamera().position.x, viewport.getCamera().position.y, 1);
        spriteBatch.end();

        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        spriteBatch.begin();
        players.debugTextRender(debugFont, viewport.getCamera(), spriteBatch);
        debugFont.draw(spriteBatch, "Fps: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
        spriteBatch.end();
    }

    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
        entity.addToWorld(world);
    }

    public void addStaticEntity(Entity entity) {
        entity.addToWorld(world);
    }

    public void removeEntity(Entity entity) {
        if (entitiesToRemove.contains(entity))
            return;

        entitiesToRemove.add(entity);
    }

    public void addCoin() {
        coinCount++;
    }

    public void killPlayer() {
        if (finished)
            return;

        Statics.assets.getSound().getFall().play(0.6f);

        game.restartLevel();
        playersDead = true;
        finished = true;
    }

    public void loadNextLevel() {
        if (finished)
            return;

        game.nextLevel(map.getNextLevel());
        finished = true;
    }


    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    public void dispose() {
        map.getMusic().stop();
    }
}
