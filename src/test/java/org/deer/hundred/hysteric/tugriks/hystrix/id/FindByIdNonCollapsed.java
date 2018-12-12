package org.deer.hundred.hysteric.tugriks.hystrix.id;

import static org.deer.hundred.hysteric.tugriks.hystrix.TestConstants.TOTAL_REQUESTS;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.deer.hundred.hysteric.tugriks.dto.Offer;
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
public class FindByIdNonCollapsed {

  @Autowired
  private OfferRepository repository;

  private ExecutorService executorService;

  @Before
  public void init() {
    final Random rankGenerator = new Random();
    IntStream.range(0, TestConstants.UPPER_BOUND_ID)
        .mapToObj(i -> {
          final Offer offer = new Offer();
          offer.setCreatedAt(LocalDateTime.now());
          offer.setId(String.valueOf(i));
          offer.setName("offer" + i);
          offer.setRank(rankGenerator.nextInt(10));
          return offer;
        }).forEach(repository::save);

    executorService = Executors.newFixedThreadPool(TestConstants.EXECUTOR_THREADS_COUNT);
  }

  @After
  public void close() {
    executorService.shutdown();
  }

  @Test
  public void testFindByIdNoncollapsed() {
    final long start = System.currentTimeMillis();
    final CompletableFuture[] all = new CompletableFuture[TOTAL_REQUESTS];
    final Random random = new Random();
    for (int i = 0; i < TOTAL_REQUESTS; i++) {
      int index = random.nextInt(TestConstants.UPPER_BOUND_ID * 2);
      all[i] = CompletableFuture
          .supplyAsync(() -> repository.findById(String.valueOf(index)), executorService)
          .thenAcceptAsync(offer -> {
            final String id = offer.map(Offer::getId)
                .orElse(null);

            if (id == null && index >= TestConstants.UPPER_BOUND_ID) {
              return;
            }

            if (!String.valueOf(index).equals(id)) {
              throw new IllegalStateException(id + " returned instead of " + index);
            }
          });
    }

    CompletableFuture.allOf(all).join();
    final long resultTime = System.currentTimeMillis() - start;
    System.out.println(TOTAL_REQUESTS + " requests in " + resultTime + " milliseconds");
  }
}
