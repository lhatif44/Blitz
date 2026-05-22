package com.blitz.repository;

import com.blitz.model.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {

    // find all teams in a conference e.g. "AFC" or "NFC"
    List<Team> findByConference(String conference);

    // find all teams in a division e.g. "AFC East"
    List<Team> findByDivision(String division);
}
