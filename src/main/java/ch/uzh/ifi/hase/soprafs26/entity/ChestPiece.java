package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CHESTPIECE")
public class ChestPiece extends Item {
    @Override
    public String getItemType() {
        return "CHESTPIECE";
    }
}
