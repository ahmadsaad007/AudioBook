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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
    int nowPlayingBookStatus;

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
        seekbar = findViewById(R.id.seekBar);
        pauseButton = findViewById(R.id.imageButton);
        stopButton = findViewById(R.id.imageButton3);

        bindService(new Intent(this, AudiobookService.class), serviceConnection, BIND_AUTO_CREATE);

        getBook(searchText);

        if (!onePane) {
            addFragment(listFragment, R.id.listfrag);
            addFragment(detailsFragment, R.id.detailfrag);
        } else {
            addFragment(viewPagerFragment, R.id.ViewPager);
        }
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText = searchString.getText().toString();
                getBook(searchText);
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    if(connected){
                        seekBar.setProgress(i);
                    }
                    else{
                        nowPlayingBookStatus = i;
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
                unbindService(serviceConnection);
                seekbar.setProgress(0);
                setTitle("Now Playing: ");
            }
        });
    }
    //Service Connection
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaControlBinder = ((AudiobookService.MediaControlBinder) service);
            connected = true;
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
            seekbar.setProgress(msg.what);
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

    public void addFragment(Fragment fragment, int id) {
        getSupportFragmentManager().
                beginTransaction().
                replace(id, fragment).
                addToBackStack(null).
                commit();
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
        seekbar.setMax(book.duration);
        String title = "Now Playing: " + book.name;
        setTitle(title);
        nowPlayingBookTitle= book.name;
        nowPlayingBookDuration = book.duration;
        //now playing title
        mediaControlBinder.play(book.id);
    }
}