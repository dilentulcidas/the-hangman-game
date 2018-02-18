package co3098.dv61.mbexplorer.repository;

import co3098.dv61.mbexplorer.domain.Folder;
import co3098.dv61.mbexplorer.domain.ItemLink;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemLinkRepository extends MongoRepository<ItemLink, String> {

    public ItemLink findByPath(String path);

}
