package org.deer.hundred.hysteric.tugriks.hystrix.or.two.conditions;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class OrCollapsed {

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

  @Test
  public void testRepoQueryOr() {
    final long time = MeasuredTest.measure(() -> {
      CompletableFuture[] futures = new CompletableFuture[200];
      final Random generator = new Random();
      for (int i = 0; i < 200; i++) {
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
