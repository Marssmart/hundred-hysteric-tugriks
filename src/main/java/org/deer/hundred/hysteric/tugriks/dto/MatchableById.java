package org.deer.hundred.hysteric.tugriks.dto;

import java.io.Serializable;

@FunctionalInterface
public interface MatchableById<T extends Serializable> {

  boolean match(T idValue);
}
