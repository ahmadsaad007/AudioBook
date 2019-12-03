package temple.edu.audiobook;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookInterface, BookDetailsFragment.BookDetailsInterface {
    FragmentManager fm;
    boolean connected; //check if service is still bound
    AudiobookService.MediaControlBinder mediaControlBinder;
    boolean onePane; //check if landscape or portrait
    BookDetailsFragment detailsFragment;
    ViewPagerFragment viewPagerFragment;
    ArrayList<Book> books;
    JSONArray bookjson;
    BookListFragment listFragment;
    EditText searchString;
    Button searchButton;
    String searchText="";
    SeekBar seekbar;
    ImageButton pauseButton, stopButton;
    String nowPlayingBookTitle;
    int nowPlayingBookDuration;
    boolean isPlaying;
    Intent service;
    int bookId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fm = getSupportFragmentManager();
        searchString = findViewById(R.id.searchText);
        searchButton = findViewById(R.id.searchButton);
        onePane = findViewById(R.id.detailfrag) == null;

        books = new ArrayList<>();
        searchText = "";
        detailsFragment = new BookDetailsFragment();
        listFragment = new BookListFragment();
        viewPagerFragment = new ViewPagerFragment();

        pauseButton = findViewById(R.id.imageButton);
        stopButton = findViewById(R.id.imageButton3);

        seekbar = findViewById(R.id.seekBar);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (bookId!=-1) {
                    if (b && connected) {
                        mediaControlBinder.play(bookId, progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText = searchString.getText().toString();
                getBook(searchText);
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (!onePane) {
            fragmentTransaction.replace( R.id.listfrag, listFragment);
            fragmentTransaction.replace( R.id.detailfrag, detailsFragment);
        } else {
            fragmentTransaction.replace(R.id.ViewPager, viewPagerFragment);
        }
        fragmentTransaction.commit();

        service = new Intent(this, AudiobookService.class);
        bindService(service, serviceConnection, BIND_AUTO_CREATE);


        //Clicking Pause Button
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connected){
                    //if pause is true and you click again it should play
                    mediaControlBinder.pause();
                }
            }
        });
        //Clicking Stop Button
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaControlBinder.stop();
                setTitle("Now Playing: ");
                seekbar.setProgress(0);
                isPlaying = false;
                bookId = -1;
                stopService(service);
            }
        });
    }

    //Service Connection
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaControlBinder = ((AudiobookService.MediaControlBinder) service);
            connected = true;
            mediaControlBinder.setProgressHandler(progressHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            mediaControlBinder = null;
        }
    };

    //Seek Bar Handler
    Handler progressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.obj != null) {
                AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) msg.obj;
                if (bookProgress.getProgress() < nowPlayingBookDuration) {
                    seekbar.setProgress(bookProgress.getProgress());
                } else if (bookProgress.getProgress() == nowPlayingBookDuration) {
                    mediaControlBinder.stop();
                    setTitle("Now Playing: ");
                    seekbar.setProgress(0);
                    isPlaying = false;
                    bookId = -1;
                }
            }
            return false;
        }

    });

    //Handler for JSON data
    Handler urlHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                bookjson = new JSONArray((String) msg.obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            books.clear();
            for (int i = 0; i < bookjson.length(); i++) {
                try {
                    JSONObject jb = bookjson.getJSONObject(i);
                    books.add(new Book(jb));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if (!onePane) {
                listFragment.getBooks(books);
            } else {
                viewPagerFragment.addPager(books);
            }
            return false;
        }
    });


    //getting books from json
    public void getBook(final String text){
        new Thread() {
            public void run() {
                try {
                    String urlString = "https://kamorris.com/lab/audlib/booksearch.php?search="+text;
                    URL url = new URL(urlString);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder builder = new StringBuilder();
                    String tmpString;
                    while ((tmpString = reader.readLine()) != null) {
                        builder.append(tmpString);
                    }
                    Message msg = Message.obtain();
                    msg.obj = builder.toString();
                    urlHandler.sendMessage(msg);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void bookSelected(Book bookObj) {
        detailsFragment.displayBook(bookObj);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(connected) {
            unbindService(serviceConnection);
            connected = false;
        }
    }

    @Override
    public void playBook(Book book) {
        //add seekBar values
        startService(service);
        bookId = book.id;
        seekbar.setMax(book.duration);
        String title = "Now Playing: " + book.name;
        setTitle(title);
        nowPlayingBookTitle= book.name;
        nowPlayingBookDuration = book.duration;
        //now playing title
        mediaControlBinder.play(book.id);
        isPlaying = true;
    }

}