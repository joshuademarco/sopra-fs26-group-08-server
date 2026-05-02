package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("HAT")
public class Hat extends Item {
    
}
