package org.deer.hundred.hysteric.tugriks.hystrix.query.param.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.bson.Document;
import org.deer.hundred.hysteric.tugriks.dto.Offer;
import org.deer.hundred.hysteric.tugriks.hystrix.query.param.CompositeQueryCommandParam;

public class QueryCommandParamFactory {

  private final Map<Class<?>, Function<Document, CompositeQueryCommandParam<?>>> queryParamSupplierRegistry;

  public QueryCommandParamFactory() {
    queryParamSupplierRegistry = new HashMap<>();
    queryParamSupplierRegistry
        .put(Offer.class, document -> new CompositeQueryCommandParam<>(Offer.class, document));
  }

  public <T> CompositeQueryCommandParam<T> createQueryCommandParam(Class<T> entityClass,
      Document query) {
    return (CompositeQueryCommandParam<T>) Optional
        .ofNullable(queryParamSupplierRegistry.get(entityClass))
        .map(function -> function.apply(query))
        .orElse(null);
  }
}
