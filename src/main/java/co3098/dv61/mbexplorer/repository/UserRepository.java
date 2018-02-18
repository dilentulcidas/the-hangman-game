package co3098.dv61.mbexplorer.repository;

import co3098.dv61.mbexplorer.domain.ItemText;
import co3098.dv61.mbexplorer.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    public User findByEmail(String email);

}
