package org.deer.hundred.hysteric.tugriks.hystrix.query.command;

import com.mongodb.BasicDBList;
import java.util.Objects;
import org.bson.Document;
import org.deer.hundred.hysteric.tugriks.dto.Offer;
import org.deer.hundred.hysteric.tugriks.hystrix.query.param.CompositeQueryCommandParam;
import org.springframework.data.mongodb.core.MongoTemplate;

public class FindOfferRankGtOrNameCommand extends CompositeQueryRequestCollapser<Offer> {

  public FindOfferRankGtOrNameCommand(CompositeQueryCommandParam<Offer> param,
      MongoTemplate template) {
    super(param, template, Offer.class, FindOfferRankGtOrNameCommand::match);
  }

  public static boolean match(CompositeQueryCommandParam<Offer> requestParam, Offer result) {
    final BasicDBList orCondition = (BasicDBList) requestParam.getQuery().get("$or");

    final Integer rank = (Integer) ((Document)((Document) orCondition.get(0)).get("rank")).get("$gt");
    final String name = (String) ((Document) orCondition.get(1)).get("name");
    return Objects.equals(name, result.getName()) || result.getRank() == rank;
  }
}
