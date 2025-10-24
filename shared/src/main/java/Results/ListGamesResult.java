package Results;

import java.util.Collection;
import java.util.Objects;

import model.GameData;

public record ListGamesResult(Collection<GameData> games) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListGamesResult that = (ListGamesResult) o;
        return Objects.equals(games(), that.games());
    }

    @Override
    public int hashCode() {
        return Objects.hash(games());
    }

}
