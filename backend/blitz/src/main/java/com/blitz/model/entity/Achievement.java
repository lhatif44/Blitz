package com.blitz.model.entity;

  import jakarta.persistence.*;
  import lombok.Getter;
  import lombok.Setter;

  import java.util.UUID;

  @Entity
  @Table(name = "achievements")
  @Getter
  @Setter
  public class Achievement {

      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private UUID id;

      // many achievements can belong to one player
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "player_id", nullable = false)
      private Player player;

      // e.g. "PRO_BOWL", "ALL_PRO_1ST", "LEAGUE_MVP", "HOF"
      @Column(nullable = false)
      private String type;
  
      // year the award was earned — null for HOF since it is not season-specific
      private Integer season;

      // extra context e.g. "1st Team", "NFC", "Super Bowl LVIII"
      private String detail;
  }
