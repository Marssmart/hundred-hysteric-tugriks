package org.deer.hundred.hysteric.tugriks.repo;

import org.deer.hundred.hysteric.tugriks.dto.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OfferRepository extends MongoRepository<Offer, String> {

}
