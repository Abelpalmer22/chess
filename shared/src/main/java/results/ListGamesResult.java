package results;

import java.util.Collection;
import java.util.Objects;

import model.GameData;

public record ListGamesResult(Collection<GameData> games) {
}
