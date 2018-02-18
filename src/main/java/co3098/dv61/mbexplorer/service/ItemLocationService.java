package co3098.dv61.mbexplorer.service;

import co3098.dv61.mbexplorer.domain.ItemLocation;
import co3098.dv61.mbexplorer.repository.ItemLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemLocationService {

    @Autowired
    ItemLocationRepository itemLocationRepository;

    // adds itemLocation to database to ItemLink table
    public void addItemLocation(ItemLocation itemLocation){

        // add to repository
        itemLocationRepository.save(itemLocation);
    }

    // gets the specific itemLocation with the respective path
    public ItemLocation getItemLocation(String path){
        return itemLocationRepository.findByPath(path);
    }

    // updates itemLocation to db
    public void updateItemLocation(ItemLocation itemLocation){

        // add to repository the new version
        itemLocationRepository.save(itemLocation);
    }

    // remove itemLocation from db
    public void deleteItemLocation(ItemLocation itemLocation){
        // remove from repo
        itemLocationRepository.delete(itemLocation);
    }

}
