package dev.lyze.parallelworlds.statics.assets.mainMenu;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import dev.lyze.parallelworlds.statics.utils.DynamicAssets;
import dev.lyze.parallelworlds.statics.utils.LoadAssetFromFile;
import dev.lyze.parallelworlds.statics.utils.LoadAssetFromTextureAtlas;
import lombok.Getter;

public class MainMenuAssets extends DynamicAssets {
    @Getter @LoadAssetFromTextureAtlas("skins/mainMenu/mainMenu.json")
    private MainMenuTextureAtlas atlas;

    @Getter @LoadAssetFromFile("skins/mainMenu/mainMenu.json")
    private Skin skin;

    public MainMenuAssets(AssetManager ass) {
        super(ass);
    }
}
