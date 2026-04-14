package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.Habit;
import ch.uzh.ifi.hase.soprafs26.entity.User;
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
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(source = "email", target = "email")
	@Mapping(source = "password", target = "password")
	@Mapping(source = "username", target = "username")
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "status", target = "status")
    @Mapping(source = "level", target = "level")
    @Mapping(source = "health", target = "health")
    @Mapping(source = "strength", target = "strength")
    @Mapping(source = "intelligence", target = "intelligence")
    @Mapping(source = "resilience", target = "resilience")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "title", target = "title")
	@Mapping(source = "category", target = "category")
	@Mapping(source = "frequency", target = "frequency")
	@Mapping(source = "positive", target = "positive")
	Habit convertHabitPostDTOtoEntity(HabitPostDTO habitPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "title", target = "title")
	@Mapping(source = "category", target = "category")
	@Mapping(source = "frequency", target = "frequency")
	@Mapping(source = "positive", target = "positive")
	@Mapping(source = "completed", target = "completed")
	@Mapping(source = "streak", target = "streak")
	@Mapping(source = "dueAt", target = "dueAt")
	@Mapping(source = "createdAt", target = "createdAt")
	@Mapping(source = "completedAt", target = "completedAt")
	HabitGetDTO convertEntityToHabitGetDTO(Habit habit);
}
