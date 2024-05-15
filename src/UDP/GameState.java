// src/main/java/common/GameState.java
package UDP;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<Integer, Player> players = new HashMap<>();

    public synchronized void addPlayer(Player player) {
        players.put(player.getId(), player);
    }

    public synchronized void updatePlayer(Player player) {
        players.put(player.getId(), player);
    }

    public synchronized Map<Integer, Player> getPlayers() {
        return new HashMap<>(players);
    }
}
