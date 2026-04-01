package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

public class UserGetDTO {

	private Long id;
	private String name;
	private String username;
	private UserStatus status;
	private Integer level;
    private Integer health;
    private Integer strength;
    private Integer intelligence;
    private Integer resilience;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
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

    public Integer getStrength() { 
        return strength; 
    }

    public void setStrength(Integer strength) { 
        this.strength = strength; 
    }   

    public Integer getIntelligence() { 
        return intelligence; 
    }

    public void setIntelligence(Integer intelligence) { 
        this.intelligence = intelligence; 
    }   

    public Integer getResilience() { 
        return resilience; 
    }

    public void setResilience(Integer resilience) { 
        this.resilience = resilience; 
    }   
}
