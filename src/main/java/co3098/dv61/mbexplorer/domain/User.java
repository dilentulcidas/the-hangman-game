package co3098.dv61.mbexplorer.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection="users")
public class User {
    @Id
    String email;

    public User(String email, List<Folder> folders, List<ItemLink> linkItems, List<ItemLocation> locationItems, List<ItemText> textItems) {
        this.email = email;
        this.folders = folders;
        this.linkItems = linkItems;
        this.locationItems = locationItems;
        this.textItems = textItems;
    }

    List<Folder> folders = new ArrayList<>();
    List<ItemLink> linkItems = new ArrayList<>();
    List<ItemLocation> locationItems = new ArrayList<>();
    List<ItemText> textItems = new ArrayList<>();

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<ItemLink> getLinkItems() {
        return linkItems;
    }

    public void setLinkItems(List<ItemLink> linkItems) {
        this.linkItems = linkItems;
    }

    public List<ItemLocation> getLocationItems() {
        return locationItems;
    }

    public void setLocationItems(List<ItemLocation> locationItems) {
        this.locationItems = locationItems;
    }

    public List<ItemText> getTextItems() {
        return textItems;
    }

    public void setTextItems(List<ItemText> textItems) {
        this.textItems = textItems;
    }

    public User(){

    }
}
