package org.deer.hundred.hysteric.tugriks.hystrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FindByIdRequestCollapserTest {

  public static final int MAX = 500000;

  @Autowired
  private OfferRepository repository;

  private ExecutorService executorService;

  @Before
  public void init() {
    repository.saveAll(IntStream.range(0, 100)
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
  public void testFindByIdCollapsed() {
    final long start = System.currentTimeMillis();
    final CompletableFuture[] all = new CompletableFuture[MAX];
    final Random random = new Random();
    for (int i = 0; i < MAX; i++) {
      int index = random.nextInt(100);
      all[i] = CompletableFuture
          .supplyAsync(() -> repository.findById(String.valueOf(index)), executorService)
          .thenAcceptAsync(offer -> {
            final String id = offer.map(Offer::getId)
                .orElse(null);

            if (id == null && index > 100) {
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