package org.deer.hundred.hysteric.tugriks.dto;

import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Offer implements MatchableById<String>{

  @Id
  private String id;

  private LocalDateTime createdAt;

  private String name;

  private int rank;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  @Override
  public String toString() {
    return "Offer{" +
        "id='" + id + '\'' +
        ", createdAt=" + createdAt +
        ", name='" + name + '\'' +
        ", rank=" + rank +
        '}';
  }

  @Override
  public boolean match(String idValue) {
    return idValue.equals(id);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Offer offer = (Offer) o;
    return rank == offer.rank &&
        Objects.equals(id, offer.id) &&
        Objects.equals(createdAt, offer.createdAt) &&
        Objects.equals(name, offer.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, createdAt, name, rank);
  }
}
