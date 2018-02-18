package co3098.dv61.mbexplorer.repository;

import co3098.dv61.mbexplorer.domain.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends MongoRepository<Folder, String> {

    public Folder findByPath(String path);

}
