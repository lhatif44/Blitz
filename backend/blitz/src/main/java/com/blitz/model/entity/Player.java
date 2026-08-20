package com.blitz.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// ignores Hibernate's lazy-proxy internals when a Player reached via a lazy @ManyToOne
// (CareerStats.player, PassingStats.player, ...) gets serialized without being fully loaded
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "players")
@Getter
@Setter
public class Player {

    //ID Primary key
    @Id
    //auto generate UUID for new player
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    //mapping to nflverse_id col, unique and not null
    @Column(name = "nflverse_id", unique = true, nullable = false)
    private String nflverseId;
    // maps field name (camelCase) to column name (snake_case)
    @Column(name = "display_name", nullable = false)  
      private String displayName;
  
      @Column(name = "first_name")
      private String firstName;

      @Column(name = "last_name")
      private String lastName;

      private String position;

      @Column(name = "position_group")
      private String positionGroup;

      @Column(name = "birth_date")
      private LocalDate birthDate;

      @Column(name = "birth_city")
      private String birthCity;
  
      @Column(name = "birth_state")
      private String birthState;

      @Column(name = "height_inches")
      private Integer heightInches;

      @Column(name = "weight_lbs")
      private Integer weightLbs;

      private String college;

      @Column(name = "entry_year")
      private Integer entryYear;
      // many players can have one draft team — LAZY means don't load the team object unless we explicitly ask for it
      @ManyToOne(fetch = FetchType.LAZY)  
      // the foreign key column in the players table
      @JoinColumn(name = "draft_team")  
      private Team draftTeam;

      @Column(name = "draft_round")
      private Integer draftRound;

      @Column(name = "draft_pick")
      private Integer draftPick;
  
      @Column(name = "headshot_url")
      private String headshotUrl;

      private String status;

      @Column(name = "years_active")
      private String yearsActive;

      @Column(name = "is_hof")
      private Boolean isHof = false;

      @Column(name = "hof_induction_year")
      private Integer hofInductionYear;

      @OneToMany(mappedBy = "player", fetch = FetchType.LAZY, cascade = 
      CascadeType.ALL)
          // one player has many team entries , mappedBy points to the field name in PlayerTeam that carries relationship
          // cascade = ALL means if a player is deleted, their PlayerTeam records are also deleted
          private List<PlayerTeam> playerTeams;
    
          @OneToMany(mappedBy = "player", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
          // same idea — one player has many achievements
          private List<Achievement> achievements;

}
