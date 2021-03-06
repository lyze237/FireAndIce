package dev.lyze.parallelworlds.screens.game.entities.filters;

import com.dongbat.jbump.CollisionFilter;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Response;
import dev.lyze.parallelworlds.screens.game.entities.TileEntity;
import dev.lyze.parallelworlds.screens.game.entities.impl.PortalTile;
import dev.lyze.parallelworlds.screens.game.entities.players.Player;

public class BatCheckForPlayerCollisionFilter implements CollisionFilter {
    public static final BatCheckForPlayerCollisionFilter instance = new BatCheckForPlayerCollisionFilter();

    @Override
    public Response filter(Item item, Item other) {
        if (other.userData instanceof TileEntity) {
            if (other.userData instanceof PortalTile)
                return Response.slide;

            return ((TileEntity) other.userData).isHitbox() ? Response.slide : Response.cross;
        }

        return other.userData instanceof Player ? Response.cross : null;
    }
}
