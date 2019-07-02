package ir.beitari;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private RecyclerView musicsListRecyclerView;
    private MusicAdapter musicAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Music[] musics;

    private Server server;
    private String hostname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicsListRecyclerView = findViewById(R.id.musicsListRecyclerView);
        layoutManager = new LinearLayoutManager(this);

        musicsListRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        musicsListRecyclerView.setHasFixedSize(true);
        musicsListRecyclerView.setLayoutManager(layoutManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission is NOT granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            // Permission is granted
            getListOfAllMusics();
        }
    }


    @Override
    protected void onDestroy() {
        if (server != null) {
            server.stop();
        }

        super.onDestroy();
    }


    /**
     * Prints the list of all musics on user's device
     */
    private void getListOfAllMusics() {
        ContentResolver cr = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);

        if (cur != null) {
            int count = cur.getCount();


            if (count > 0) {
                musics = new Music[count];
                int i = 0;

                while (cur.moveToNext()) {
                    String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String path = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    Log.i("MUSIC", title + ": " + path);
                    musics[i] = new Music(title, path);
                    i++;
                }

                musicAdapter = new MusicAdapter(musics);
                musicsListRecyclerView.setAdapter(musicAdapter);

                startServer();
            } else {
                Toast.makeText(this, "No music found!", Toast.LENGTH_LONG).show();
            }

            cur.close();
        }
    }


    /**
     * Returns local IP address
     *
     * @return String
     */
    private String getIP() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        if (wifiManager == null) {
            return null;
        }

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        return String.format(Locale.US, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }


    /**
     * Start the HTTP server
     */
    private void startServer() {
        try {
            server = new Server();
            server.setMusics(musics);
            server.start();
            hostname = getIP();
            Toast.makeText(this, "Server is running on port http://" + hostname + ":" + server.getListeningPort(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot start the server!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, yay!
                getListOfAllMusics();
            } else {
                // Permission denied, boo!
                Toast.makeText(this, "Cannot access to musics!", Toast.LENGTH_LONG).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
