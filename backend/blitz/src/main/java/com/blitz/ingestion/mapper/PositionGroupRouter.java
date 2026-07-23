package com.blitz.ingestion.mapper;

import org.springframework.stereotype.Component;

//routes a raw position string (nflverse "position" or PFR "pos") to the stat table
@Component
public class PositionGroupRouter {

    public enum StatCategory {
        PASSING, RUSHING, RECEIVING, PASS_RUSH, LINEBACKER, SECONDARY, KICKING, PUNTING, NONE
    }

    public record Route(StatCategory category, String positionGroup) {
    }

    private static final Route NONE_ROUTE = new Route(StatCategory.NONE, null);

    public Route route(String position) {
        if (position == null) {
            return NONE_ROUTE;
        }
        return switch (position.trim().toUpperCase()) {
            case "QB" -> new Route(StatCategory.PASSING, "QB");
            case "RB", "FB" -> new Route(StatCategory.RUSHING, "RB");
            case "WR" -> new Route(StatCategory.RECEIVING, "WR");
            case "TE" -> new Route(StatCategory.RECEIVING, "TE");
            case "DE" -> new Route(StatCategory.PASS_RUSH, "DE");
            case "DT", "NT" -> new Route(StatCategory.PASS_RUSH, "DT");
            case "ILB", "MLB", "OLB", "LB", "LLB", "RLB", "LILB", "RILB" -> new Route(StatCategory.LINEBACKER, "LB");
            case "CB" -> new Route(StatCategory.SECONDARY, "CB");
            case "S", "SS", "FS" -> new Route(StatCategory.SECONDARY, "S");
            case "K" -> new Route(StatCategory.KICKING, "K");
            case "P" -> new Route(StatCategory.PUNTING, "P");
            default -> NONE_ROUTE;
        };
    }
}
