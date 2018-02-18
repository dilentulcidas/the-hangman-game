package co3098.dv61.mbexplorer.repository;

import co3098.dv61.mbexplorer.domain.ItemLink;
import co3098.dv61.mbexplorer.domain.ItemLocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemLocationRepository extends MongoRepository<ItemLocation, String> {

    public ItemLocation findByPath(String path);

}
