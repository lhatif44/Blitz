package com.blitz.ingestion.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PositionGroupRouterTest {

    private final PositionGroupRouter router = new PositionGroupRouter();

    @ParameterizedTest
    @CsvSource({
            "QB, PASSING, QB",
            "RB, RUSHING, RB",
            "FB, RUSHING, RB",
            "WR, RECEIVING, WR",
            "TE, RECEIVING, TE",
            "DE, PASS_RUSH, DE",
            "DT, PASS_RUSH, DT",
            "NT, PASS_RUSH, DT",
            "ILB, LINEBACKER, LB",
            "MLB, LINEBACKER, LB",
            "OLB, LINEBACKER, LB",
            "LB, LINEBACKER, LB",
            "LLB, LINEBACKER, LB",
            "RLB, LINEBACKER, LB",
            "LILB, LINEBACKER, LB",
            "RILB, LINEBACKER, LB",
            "CB, SECONDARY, CB",
            "S, SECONDARY, S",
            "SS, SECONDARY, S",
            "FS, SECONDARY, S",
            "K, KICKING, K",
            "P, PUNTING, P",
    })
    void route_mapsKnownPositionsToTheCorrectTableAndGroup(String position, String expectedCategory, String expectedGroup) {
        PositionGroupRouter.Route route = router.route(position);

        assertThat(route.category()).isEqualTo(PositionGroupRouter.StatCategory.valueOf(expectedCategory));
        assertThat(route.positionGroup()).isEqualTo(expectedGroup);
    }

    @ParameterizedTest
    @ValueSource(strings = {"OL", "OT", "OG", "C", "LS", "FL"})
    void route_returnsNoneForUnmappedPositions(String position) {
        PositionGroupRouter.Route route = router.route(position);

        assertThat(route.category()).isEqualTo(PositionGroupRouter.StatCategory.NONE);
        assertThat(route.positionGroup()).isNull();
    }

    @ParameterizedTest
    @NullSource
    void route_returnsNoneForNullPosition(String position) {
        PositionGroupRouter.Route route = router.route(position);

        assertThat(route.category()).isEqualTo(PositionGroupRouter.StatCategory.NONE);
    }

    @Test
    void route_isCaseInsensitiveAndTrimsWhitespace() {
        assertThat(router.route(" qb ").category()).isEqualTo(PositionGroupRouter.StatCategory.PASSING);
        assertThat(router.route("Wr").category()).isEqualTo(PositionGroupRouter.StatCategory.RECEIVING);
    }
}
