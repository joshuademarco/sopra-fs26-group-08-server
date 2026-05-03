package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Item;

@Repository("itemRepository")
public interface ItemRepository extends JpaRepository<Item, Long> {
    
}
