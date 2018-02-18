package co3098.dv61.mbexplorer.repository;

import co3098.dv61.mbexplorer.domain.ItemText;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemTextRepository extends MongoRepository<ItemText, String> {

    public ItemText findByPath(String path);

}
