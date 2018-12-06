package org.deer.hundred.hysteric.tugriks.hystrix;

import static org.deer.hundred.hysteric.tugriks.hystrix.FindByIdRequestCollapserTest.MAX;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.deer.hundred.hysteric.tugriks.dto.Offer;
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

  public static final int UPPER_BOUND_ID = 100;

  @Autowired
  private OfferRepository repository;

  private ExecutorService executorService;

  @Before
  public void init() {
    repository.saveAll(IntStream.range(0, UPPER_BOUND_ID)
        .mapToObj(i -> {
          final Offer offer = new Offer();
          offer.setCreatedAt(LocalDateTime.now());
          offer.setId(String.valueOf(i));
          offer.setName("offer" + i);
          return offer;
        }).collect(Collectors.toList()));

    executorService = Executors.newFixedThreadPool(100);
  }

  @After
  public void close() {
    executorService.shutdown();
  }

  @Test
  public void testFindByIdNoncollapsed() {
    final long start = System.currentTimeMillis();
    final CompletableFuture[] all = new CompletableFuture[MAX];
    final Random random = new Random();
    for (int i = 0; i < MAX; i++) {
      int index = random.nextInt(UPPER_BOUND_ID * 2);
      all[i] = CompletableFuture
          .supplyAsync(() -> repository.findById(String.valueOf(index)), executorService)
          .thenAcceptAsync(offer -> {
            final String id = offer.map(Offer::getId)
                .orElse(null);

            if (id == null && index >= UPPER_BOUND_ID) {
              return;
            }

            if (!String.valueOf(index).equals(id)) {
              throw new IllegalStateException(id + " returned instead of " + index);
            }
          });
    }

    CompletableFuture.allOf(all).join();
    final long resultTime = System.currentTimeMillis() - start;
    System.out.println(MAX + " requests in " + resultTime + " milliseconds");
  }
}
