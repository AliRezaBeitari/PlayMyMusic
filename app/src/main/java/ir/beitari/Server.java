package ir.beitari;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {
    private final static int PORT = 8080;

    private Music[] musics;

    private final static String htmlHeader = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
            "    <title>Play My Music</title>\n" +
            "</head>\n" +
            "<body>";

    private final static String htmlFooter = "</body>\n" +
            "</html>";

    Server() {
        super(PORT);
    }

    void setMusics(Music[] musics) {
        this.musics = musics;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String html;
        String url = session.getUri();

        if (url.equals("/")) {
            html = homePage();
        } else {
            String musicId = url.split("/")[1];

            try {
                int id = Integer.parseInt(musicId);
                return getMusicPage(id);
            } catch (Exception e) {
                e.printStackTrace();
                html = notFoundPage();
            }
        }

        return newFixedLengthResponse(html);
    }


    /**
     * Returns home page html
     *
     * @return String
     */
    private String homePage() {
        StringBuilder html = new StringBuilder("<h1>Home</h1><br>");

        for (int i = 0; i < musics.length; i++) {
            html.append("<a href=\"/").append(i).append("\"><h4>").append(musics[i].getTitle()).append("</h4></a><br>");
        }

        return html.toString();
    }


    /**
     * Returns music file with id of @id
     *
     * @param id int
     * @return Response
     */
    private Response getMusicPage(int id) {
        if (id < 0 || id >= musics.length) {
            return newFixedLengthResponse("<h1>Invalid Music ID!</h1>");
        }

        Music music = musics[id];

        String mime = NanoHTTPD.getMimeTypeForFile(music.getFilePath());

        File file = new File(music.getFilePath());
        FileInputStream fis = null;

        try {
            if (file.exists()) {
                fis = new FileInputStream(file);

                return newFixedLengthResponse(Response.Status.OK, mime, fis, file.length());
            } else {
                return newFixedLengthResponse("<h1>Music File Does Not Exist!</h1>");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return newFixedLengthResponse("<h1>Some Internal Error Happened!</h1>");
    }


    /**
     * Returns 404 not found page html
     *
     * @return String
     */
    private String notFoundPage() {
        return "404";
    }
}
