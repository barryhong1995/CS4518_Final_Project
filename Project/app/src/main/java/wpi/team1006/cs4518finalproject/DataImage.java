package wpi.team1006.cs4518finalproject;

import java.util.List;

public class DataImage {
    private String image;
    private List<String> tags;
    private String time;

    public DataImage() {}
    public DataImage(String image, List<String> tags, String time) {
        this.image = image;
        this.tags = tags;
        this.time = time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
