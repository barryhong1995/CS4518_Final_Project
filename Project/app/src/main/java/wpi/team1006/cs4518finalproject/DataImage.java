package wpi.team1006.cs4518finalproject;

public class DataImage {
    private String image;
    private String[] tags;

    public DataImage() {}
    public DataImage(String image, String[] tags) {
        this.image = image;
        this.tags = tags;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}
