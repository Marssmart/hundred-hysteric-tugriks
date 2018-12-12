package org.deer.hundred.hysteric.tugriks.hystrix.duplicates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
public class CheckDuplicateDataButDifferentInstances {

  private static final String ID = "abcabcabc";

  @Autowired
  private OfferRepository repository;

  @Before
  public void init(){
    final Offer offer = new Offer();
    offer.setId(ID);
    repository.save(offer);
  }

  @After
  public void close(){
    repository.deleteById(ID);
  }

  @Test
  public void testDuplicateButNotIdenticalInstances(){
    CompletableFuture<Optional<Offer>>[] futures = new CompletableFuture[]{findById(), findById(),
        findById()};

    CompletableFuture.allOf(futures).join();
    final Offer first = futures[0].join().get();
    final Offer second = futures[1].join().get();
    final Offer third = futures[2].join().get();

    assertEquals(first,second);
    assertEquals(first,third);
    assertNotSame(first, second);
    assertNotSame(first, third);
  }

  private CompletableFuture<Optional<Offer>> findById() {
    return CompletableFuture.supplyAsync(() -> repository.findById(ID));
  }
}
