package com.blitz.model.entity;

  import com.fasterxml.jackson.annotation.JsonIgnore;
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

      // many achievements can belong to one player — ignored on serialization,
      // this is always reached by walking player.achievements, so re-emitting it would recurse forever
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "player_id", nullable = false)
      @JsonIgnore
      private Player player;

      // e.g. "PRO_BOWL", "ALL_PRO_1ST", "LEAGUE_MVP", "HOF"
      @Column(nullable = false)
      private String type;
  
      // year the award was earned — null for HOF since it is not season-specific
      private Integer season;

      // extra context e.g. "1st Team", "NFC", "Super Bowl LVIII"
      private String detail;
  }
