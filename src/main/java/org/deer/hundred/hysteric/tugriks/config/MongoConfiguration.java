package org.deer.hundred.hysteric.tugriks.config;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.deer.hundred.hysteric.tugriks.hystrix.query.command.factory.QueryCommandFactory;
import org.deer.hundred.hysteric.tugriks.hystrix.query.param.factory.QueryCommandParamFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.HystrixMongoTemplate;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.deer.hundred.hysteric.tugriks.repo")
public class MongoConfiguration {

  @Bean
  public HystrixRequestContext context() {
    return HystrixRequestContext.initializeContext();
  }

  @Bean
  public MongoOperations mongoOperations(MongoDbFactory mongoDbFactory, MongoConverter converter,
      HystrixRequestContext requestContext) {
    return new HystrixMongoTemplate(mongoDbFactory, converter, requestContext);
  }

  @Bean
  MongoTemplate mongoTemplate(HystrixMongoTemplate operations) {
    return operations;
  }
}
