package org.deer.hundred.hysteric.tugriks;

import org.deer.hundred.hysteric.tugriks.config.MongoConfiguration;
import org.deer.hundred.hysteric.tugriks.repo.OfferRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class HystericApp {

  public static void main(String[] args) {
    final ConfigurableApplicationContext context = SpringApplication.run(HystericApp.class, args);

    final OfferRepository offerRepository = context.getBean(OfferRepository.class);

    offerRepository.findById("a").orElse(null);
  }
}
