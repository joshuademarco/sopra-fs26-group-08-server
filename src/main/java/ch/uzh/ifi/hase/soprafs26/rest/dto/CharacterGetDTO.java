package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class CharacterGetDTO {
    private Long id;
    private Integer level;
    private Integer health;
    private Integer maxHealth;
    private Integer xp;
    private Integer xpToNextLevel;
    private Integer strength;
    private Integer resilience;
    private Integer intelligence;
    private String skinColor;
    private String type;

    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Integer getLevel() { 
        return level; 
    }

    public void setLevel(Integer level) { 
        this.level = level; 
    }

    public Integer getHealth() { 
        return health; 
    }

    public void setHealth(Integer health) { 
        this.health = health; 
    }
    public Integer getMaxHealth() { 
        return maxHealth; 
    }
    public void setMaxHealth(Integer maxHealth) { 
        this.maxHealth = maxHealth; 
    }

    public Integer getXp() { 
        return xp; 
    }

    public void setXp(Integer xp) { 
        this.xp = xp; 
    }

    public Integer getXpToNextLevel() { 
        return xpToNextLevel; 
    }

    public void setXpToNextLevel(Integer xpToNextLevel) { 
        this.xpToNextLevel = xpToNextLevel; 
    }

    public Integer getStrength() { 
        return strength; 
    }

    public void setStrength(Integer strength) { 
        this.strength = strength; 
    }

    public Integer getResilience() { 
        return resilience; 
    }

    public void setResilience(Integer resilience) { 
        this.resilience = resilience; 
    }

    public Integer getIntelligence() { 
        return intelligence; 
    }

    public void setIntelligence(Integer intelligence) { 
        this.intelligence = intelligence; 
    }

    public String getSkinColor() { 
        return skinColor; 
    }

    public void setSkinColor(String skinColor) { 
        this.skinColor = skinColor; 
    }

    public String getType() { 
        return type; 
    }

    public void setType(String type) { 
        this.type = type; 
    }
}
