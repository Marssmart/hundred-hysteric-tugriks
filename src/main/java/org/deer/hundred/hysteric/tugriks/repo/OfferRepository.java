package org.deer.hundred.hysteric.tugriks.repo;

import java.util.List;
import org.deer.hundred.hysteric.tugriks.dto.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OfferRepository extends MongoRepository<Offer, String> {

  List<Offer> findAllByRankGreaterThanAndName(final int rank, final String name);

  List<Offer> findAllByRankGreaterThanOrName(final int rank, final String name);
}
