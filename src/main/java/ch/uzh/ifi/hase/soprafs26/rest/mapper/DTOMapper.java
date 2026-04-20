package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.Character;
import ch.uzh.ifi.hase.soprafs26.entity.Group;
import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.CharacterGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.HabitGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.HabitPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

 /**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */ 

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class); 

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "username", target = "username")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "level", target = "level")
    @Mapping(source = "health", target = "health")
    @Mapping(source = "maxHealth", target = "maxHealth")
    @Mapping(source = "experience", target = "experience")
    @Mapping(source = "strength", target = "strength")
    @Mapping(source = "resilience", target = "resilience")
    @Mapping(source = "intelligence", target = "intelligence")
    @Mapping(source = "skinColor", target = "skinColor")
    @Mapping(source = "type", target = "type")
    CharacterGetDTO convertEntityToCharacterGetDTO(Character character);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "title", target = "title")
	@Mapping(source = "description", target = "description")
	@Mapping(source = "category", target = "category")
	@Mapping(source = "weight", target = "weight")
	@Mapping(source = "frequency", target = "frequency")
	@Mapping(source = "positive", target = "positive")
	Habit convertHabitPostDTOtoEntity(HabitPostDTO habitPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "title", target = "title")
	@Mapping(source = "description", target = "description")
	@Mapping(source = "category", target = "category")
	@Mapping(source = "frequency", target = "frequency")
	@Mapping(source = "positive", target = "positive")
	@Mapping(source = "weight", target = "weight")
	@Mapping(source = "completed", target = "completed")
	@Mapping(source = "streak", target = "streak")
	@Mapping(source = "dueAt", target = "dueAt")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(source = "completedAt", target = "completedAt")
	HabitGetDTO convertEntityToHabitGetDTO(Habit habit);

	@Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    Group convertGroupPostDTOtoEntity(GroupPostDTO groupPostDTO);

	@Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    GroupGetDTO convertEntityToGroupGetDTO(Group group);
}