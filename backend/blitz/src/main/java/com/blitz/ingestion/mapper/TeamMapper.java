package com.blitz.ingestion.mapper;

import com.blitz.ingestion.csv.CsvRow;
import com.blitz.model.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {

    public Team map(CsvRow row) {
        Team team = new Team();
        team.setAbbr(row.getString("team_abbr"));
        team.setFullName(row.getString("team_name"));
        team.setConference(row.getString("team_conf"));
        team.setDivision(row.getString("team_division"));
        team.setPrimaryColor(row.getString("team_color"));
        team.setLogoUrl(row.getString("team_logo_espn"));
        return team;
    }
}
