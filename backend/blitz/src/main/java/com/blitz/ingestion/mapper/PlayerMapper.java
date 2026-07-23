package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.Player;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class PlayerMapper {

    private final PositionGroupRouter positionGroupRouter;

    public PlayerMapper(PositionGroupRouter positionGroupRouter) {
        this.positionGroupRouter = positionGroupRouter;
    }

    public Player map(CsvRow row, Function<String, Team> teamLookup) {
        Player player = new Player();
        player.setNflverseId(row.getString("gsis_id"));
        player.setDisplayName(row.getString("display_name"));
        player.setFirstName(row.getString("first_name"));
        player.setLastName(row.getString("last_name"));

        String position = row.getString("position");
        player.setPosition(position);
        String routedGroup = positionGroupRouter.route(position).positionGroup();
        player.setPositionGroup(routedGroup != null ? routedGroup : row.getString("position_group"));

        player.setBirthDate(row.getLocalDate("birth_date"));
        player.setHeightInches(row.getInt("height"));
        player.setWeightLbs(row.getInt("weight"));
        player.setCollege(row.getString("college_name"));

        Integer draftYear = row.getInt("draft_year");
        player.setEntryYear(draftYear != null ? draftYear : row.getInt("rookie_season"));

        String draftTeamAbbr = row.getString("draft_team");
        player.setDraftTeam(draftTeamAbbr != null ? teamLookup.apply(draftTeamAbbr) : null);

        player.setDraftRound(row.getInt("draft_round"));
        player.setDraftPick(row.getInt("draft_pick"));
        player.setHeadshotUrl(row.getString("headshot"));
        player.setStatus(row.getString("status"));

        Integer rookieSeason = row.getInt("rookie_season");
        Integer lastSeason = row.getInt("last_season");
        if (rookieSeason != null && lastSeason != null) {
            player.setYearsActive(rookieSeason + "-" + lastSeason);
        }

        player.setIsHof(false);
        return player;
    }
}
