package org.deer.hundred.hysteric.tugriks.hystrix.id;

import static org.deer.hundred.hysteric.tugriks.hystrix.TestConstants.EXECUTOR_THREADS_COUNT;
import static org.deer.hundred.hysteric.tugriks.hystrix.TestConstants.UPPER_BOUND_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FindByIdRequestCollapserTest {

  @Autowired
  private OfferRepository repository;

  private ExecutorService executorService;

  @Before
  public void init() {
    final Random rankGenerator = new Random();
    IntStream.range(0, UPPER_BOUND_ID)
        .mapToObj(i -> {
          final Offer offer = new Offer();
          offer.setCreatedAt(LocalDateTime.now());
          offer.setId(String.valueOf(i));
          offer.setName("offer" + i);
          offer.setRank(rankGenerator.nextInt(10));
          return offer;
        }).forEach(repository::save);

    executorService = Executors.newFixedThreadPool(EXECUTOR_THREADS_COUNT);
  }

  @After
  public void close() {
    executorService.shutdown();
  }

  @Test
  public void testFindByIdCollapsed() {
    final long start = System.currentTimeMillis();
    final CompletableFuture[] all = new CompletableFuture[TestConstants.TOTAL_REQUESTS];
    final Random random = new Random();
    for (int i = 0; i < TestConstants.TOTAL_REQUESTS; i++) {
      int index = random.nextInt(UPPER_BOUND_ID);
      all[i] = CompletableFuture
          .supplyAsync(() -> repository.findById(String.valueOf(index)), executorService)
          .thenAcceptAsync(offer -> {
            final String id = offer.map(Offer::getId)
                .orElse(null);

            if (id == null && index > UPPER_BOUND_ID) {
              return;
            }

            if (!String.valueOf(index).equals(id)) {
              throw new IllegalStateException(id + " returned instead of " + index);
            }
          });
    }

    CompletableFuture.allOf(all).join();
    final long resultTime = System.currentTimeMillis() - start;
    System.out.println(TestConstants.TOTAL_REQUESTS + " requests in " + resultTime + " milliseconds");
  }
}