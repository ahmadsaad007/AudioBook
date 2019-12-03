package temple.edu.audiobook;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import androidx.fragment.app.Fragment;
public class BookDetailsFragment extends Fragment {

    private static final String BOOK_TITLE_KEY = "bookKey";
    private BookDetailsInterface mListener;
    Context c;
    private Book book;
    TextView textView;
    ImageView coverImageView;
    ImageButton PlayButton;

    public BookDetailsFragment() {}

    public static BookDetailsFragment newInstance(Book bk) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(BOOK_TITLE_KEY, bk);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable(BOOK_TITLE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book_details, container, false);
        textView = v.findViewById(R.id.textView1);

        coverImageView = v.findViewById(R.id.imageView1);
        PlayButton = v.findViewById(R.id.imageButton2);

        if (book != null) {
            displayBook(book);
            PlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.playBook(book);
                }
            });
        }

        return v;
    }



    public void displayBook(final Book book) {
        textView.setText(book.name +"\n"+ book.author +"\n"+ book.published);
        Picasso.get().load(book.coverURL).fit().into(coverImageView);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookDetailsInterface) {
            mListener = (BookDetailsInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        this.c = context;
    }

    @Override
    public void onDetach(){
        super.onDetach();
        mListener = null;
    }
    public interface BookDetailsInterface{
        void playBook (Book book);
    }
}
