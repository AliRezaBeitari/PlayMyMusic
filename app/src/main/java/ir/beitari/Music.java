package ir.beitari;

public class Music {
    private String title;
    private String filePath;

    Music(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
    }

    String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
