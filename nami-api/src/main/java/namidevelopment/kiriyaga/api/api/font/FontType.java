package namidevelopment.kiriyaga.api.api.font;

public enum FontType {
    VERDANA("verdana.ttf"),
    VERDANAPRO("verdanapro.ttf"),
    ARIAL("arial.ttf"),
    ROBOTO("roboto.ttf");

    private final String fileName;

    FontType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
