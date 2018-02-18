package co3098.dv61.mbexplorer.service;

import co3098.dv61.mbexplorer.domain.ItemText;
import co3098.dv61.mbexplorer.repository.ItemTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemTextService {

    @Autowired
    ItemTextRepository itemTextRepository;

    // adds itemText to database to ItemText table
    public void addItemText(ItemText itemText){

        // add to repository
        itemTextRepository.save(itemText);
    }

    // gets the specific itemText with the respective path
    public ItemText getItemText(String path){
        return itemTextRepository.findByPath(path);
    }

    // updates itemText to db
    public void updateItemText(ItemText itemText){

        // add to repository the new version
        itemTextRepository.save(itemText);
    }

    // remove itemText from db
    public void deleteItemText(ItemText itemText){
        // remove from repo
        itemTextRepository.delete(itemText);
    }

}
