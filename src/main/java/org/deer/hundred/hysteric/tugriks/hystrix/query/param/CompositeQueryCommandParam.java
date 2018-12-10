package org.deer.hundred.hysteric.tugriks.hystrix.query.param;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;

public class CompositeQueryCommandParam<T> {

  private final Class<T> entityClass;
  private final Document query;

  public CompositeQueryCommandParam(Class<T> entityClass,
      Document query) {
    this.entityClass = entityClass;
    this.query = query;
  }

  public Document getQuery() {
    return query;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CompositeQueryCommandParam<?> that = (CompositeQueryCommandParam<?>) o;
    return Objects.equals(this.toKey(), that.toKey());
  }

  @Override
  public int hashCode() {
    return Objects.hash(toKey());
  }

  public String toKey() {
    return entityClass.getName() + "_" + getAllKeys(query).map(Object::toString)
        .collect(Collectors.joining("_"));
  }

  private static Stream<Object> getAllKeys(Document query) {
    return query.keySet().stream()
        .flatMap(o -> {
          final Object obj = query.get(o);
          if (obj instanceof Document) {
            return Stream.concat(Stream.of(o), getAllKeys((Document) obj));
          } else if (obj instanceof List) {
            return Stream.concat(Stream.of(o),
                List.class.cast(obj).stream().flatMap(o1 -> getAllKeys((Document) o1)));
          } else {
            return Stream.of(o);
          }
        });
  }
}
