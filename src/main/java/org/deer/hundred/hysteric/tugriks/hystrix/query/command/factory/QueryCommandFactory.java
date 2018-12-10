package org.deer.hundred.hysteric.tugriks.hystrix.query.command.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.deer.hundred.hysteric.tugriks.dto.Offer;
import org.deer.hundred.hysteric.tugriks.hystrix.query.command.CompositeQueryRequestCollapser;
import org.deer.hundred.hysteric.tugriks.hystrix.query.command.FindOfferRankGtOrNameCommand;
import org.deer.hundred.hysteric.tugriks.hystrix.query.param.CompositeQueryCommandParam;
import org.springframework.data.mongodb.core.MongoTemplate;

public class QueryCommandFactory {

  private final Map<String, Function<CompositeQueryCommandParam<?>,
      CompositeQueryRequestCollapser<?>>> requestCreatorFunctionRegistry;

  public QueryCommandFactory(MongoTemplate template) {
    requestCreatorFunctionRegistry = new HashMap<>();
    requestCreatorFunctionRegistry.put(
        "org.deer.hundred.hysteric.tugriks.dto.Offer_$or_rank_$gt_name",
        param -> new FindOfferRankGtOrNameCommand((CompositeQueryCommandParam<Offer>) param,
            template));
  }

  public <T> CompositeQueryRequestCollapser<T> createCommand(CompositeQueryCommandParam<T> param) {
    return (CompositeQueryRequestCollapser<T>) Optional
        .ofNullable(requestCreatorFunctionRegistry.get(param.toKey()))
        .map(function -> function.apply(param))
        .orElse(null);
  }
}
