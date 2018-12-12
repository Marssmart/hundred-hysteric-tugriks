package org.deer.hundred.hysteric.tugriks.hystrix.or.two.conditions;

import static java.lang.String.format;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.deer.hundred.hysteric.tugriks.hystrix.MeasuredTest;
import org.deer.hundred.hysteric.tugriks.hystrix.TestConstants;
import org.deer.hundred.hysteric.tugriks.repo.OfferRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(basePackageClasses = OfferRepository.class)
@ContextConfiguration(classes = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class OrNonCollapsed {

  @Autowired
  private OfferRepository offerRepository;

  private ExecutorService executor;

  @Before
  public void init() {
    executor = Executors.newFixedThreadPool(TestConstants.EXECUTOR_THREADS_COUNT_OR);
  }

  @After
  public void close() {
    executor.shutdown();
  }

  //i've had to reduce the nr of threads here as the non collapsed version of this had
  //some serious perf problems
  @Test
  public void testRepoQueryOr() {
    final long time = MeasuredTest.measure(() -> {
      CompletableFuture[] futures = new CompletableFuture[TestConstants.TOTAL_OR_REQUESTS];
      final Random generator = new Random();
      for (int i = 0; i < TestConstants.TOTAL_OR_REQUESTS; i++) {
        futures[i] = CompletableFuture
            .supplyAsync(() -> offerRepository.findAllByRankGreaterThanOrName(generator.nextInt(10),
                "offer-" + generator.nextInt(10)), executor);
      }
      CompletableFuture.allOf(futures).join();
    }).run();
    System.out.println(this.getClass().getSimpleName()+" - testRepoQueryOr - "+time+" "
        + "milliseconds");
  }
}
