package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("todoRepository")
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUser_Id(Long userId);

    //get only incomplete todos
    List<Todo> findByUser_IdAndCompleted(Long userId, Boolean completed);
}
