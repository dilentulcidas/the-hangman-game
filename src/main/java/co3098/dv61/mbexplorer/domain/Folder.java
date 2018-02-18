package co3098.dv61.mbexplorer.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.annotation.Id;

import java.util.List;

@JsonPropertyOrder({ "folder", "linkItems", "locationItems", "textItems", "subfolders" })
public class Folder {

    @JsonProperty("folder")
    String name;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<Folder> subfolders;
    @JsonIgnore
    Boolean locked;
    @Id @JsonIgnore
    String path;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ItemLink> linkItems;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ItemLocation> locationItems;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ItemText> textItems;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @JsonIgnore
    String href;

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

    public Folder(String name, List<Folder> subfolders, Boolean locked, String path) {
        this.name = name;
        this.subfolders = subfolders;
        this.locked = locked;
        this.path = path;
    }

    public Folder(String name, List<Folder> subfolders, Boolean locked, String path, String href) {
        this.name = name;
        this.subfolders = subfolders;
        this.locked = locked;
        this.path = path;
        this.href = href;
    }

    public List<Folder> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(List<Folder> subfolders) {
        this.subfolders = subfolders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

	public Folder(){}


}
