package academy.tochkavhoda.competition.api.dto.requests;

import java.util.List;

/** Отбор по направлениям (подходит всё, что относится хотя бы к одному из них). */
public class DirectionsRequest {
    public final List<String> directions;

    public DirectionsRequest(List<String> directions) {
        this.directions = directions;
    }
}
