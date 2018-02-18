package co3098.dv61.mbexplorer.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

public class ItemLocation{

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public ItemLocation(String path, String location, double latitude, double longitude) {
		this.path = path;
		this.location = location;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public ItemLocation(String path, String location, double latitude, double longitude, String href) {
		this.path = path;
		this.location = location;
		this.latitude = latitude;
		this.longitude = longitude;
		this.href = href;
	}

	@Id @JsonInclude
    String path; // will have directory path + location. ex: "Documents|Books|<location>"
	@JsonProperty("name")
	String location;
	@JsonIgnore
	double latitude;
	@JsonIgnore
	double longitude;

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	@JsonIgnore
	String href;

	public ItemLocation(){

	}

}
