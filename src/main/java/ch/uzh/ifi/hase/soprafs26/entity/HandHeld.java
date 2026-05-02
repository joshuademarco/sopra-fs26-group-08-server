package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("HANDHELD")
public class HandHeld extends Item {
    
}
