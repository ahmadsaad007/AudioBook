package temple.edu.audiobook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import edu.temple.audiobookplayer.AudiobookService;

import androidx.fragment.app.Fragment;
public class BookListFragment extends Fragment {

    private BookInterface mListener;
    AudiobookService.MediaControlBinder mediaControlBinder;

    ListView listView;
    Context c;
    ArrayList<Book> bookList;
    Book books;
    BookAdapter adapter;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(String param1, String param2) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book_list, container, false);
        listView = v.findViewById(R.id.list);
        bookList = new ArrayList<>();

        return v;
    }

    public void getBooks(final ArrayList<Book> bookArray){
        adapter = new BookAdapter(c, bookArray);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                books = bookArray.get(position);
                ((BookInterface) c).bookSelected(books);
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookInterface) {
            mListener = (BookInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        this.c = context;
    }

    public interface BookInterface {
        void bookSelected(Book bookObj);
    }
}