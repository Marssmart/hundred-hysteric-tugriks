package org.deer.hundred.hysteric.tugriks.dto;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Offer implements MatchableById<String> {

  @Id
  private String id;

  private LocalDateTime createdAt;

  private String name;

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

  @Override
  public String toString() {
    return "Offer{" +
        "id='" + id + '\'' +
        ", createdAt=" + createdAt +
        ", name='" + name + '\'' +
        '}';
  }

  @Override
  public boolean match(String idValue) {
    return idValue.equals(id);
  }
}
