package co3098.dv61.mbexplorer.service;

import co3098.dv61.mbexplorer.domain.ItemLink;
import co3098.dv61.mbexplorer.repository.ItemLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ItemLinkService {

    @Autowired
    ItemLinkRepository itemLinkRepository;

    // adds itemLink to database to FOLDER table
    public void addItemLink(ItemLink itemLink){

        // add to repository
        itemLinkRepository.save(itemLink);
    }

    // gets the specific itemLink with the respective path
    public ItemLink getItemLink(String path){
        return itemLinkRepository.findByPath(path);
    }

    // updates itemLink to db
    public void updateItemLink(ItemLink itemLink){

        // add to repository the new version
        itemLinkRepository.save(itemLink);
    }

    // remove itemLink from db
    public void deleteItemLink(ItemLink itemLink){
        // remove from repo
        itemLinkRepository.delete(itemLink);
    }

}
