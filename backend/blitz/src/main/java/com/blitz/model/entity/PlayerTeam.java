package com.blitz.model.entity;

  import jakarta.persistence.*;
  import lombok.Getter;
  import lombok.Setter;

  import java.util.UUID;

  @Entity
  @Table(name = "player_teams")
  @Getter
  @Setter
  public class PlayerTeam {

      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;

      // many player-team entries can belong to one player
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "player_id", nullable = false)
      private Player player;

      // many player-team entries can belong to one team
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "team_abbr", nullable = false)
      private Team team;

      @Column(name = "season_start", nullable = false)
      private Integer seasonStart;

      // null means they are currently still on this team
      @Column(name = "season_end")
      private Integer seasonEnd;
  }
