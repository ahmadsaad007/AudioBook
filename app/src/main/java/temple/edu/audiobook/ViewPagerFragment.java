package temple.edu.audiobook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerFragment extends Fragment {

    private static final String BOOKLIST_KEY = "booklist";

    ViewPager viewPager;
    BookFragmentAdapter bookFragmentAdapter;
    BookDetailsFragment bookDetailsFragment;
    Book bookObj;
    ArrayList<Book> books;


    public ViewPagerFragment() {}

    public static ViewPagerFragment newInstance(ArrayList<Book> book) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(BOOKLIST_KEY, book);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            books = getArguments().getParcelableArrayList(BOOKLIST_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_pager, container, false);

        viewPager = v.findViewById(R.id.Pager);
        bookFragmentAdapter = new BookFragmentAdapter(getFragmentManager());
        books = new ArrayList<>();
        viewPager.setAdapter(bookFragmentAdapter);

        return v;
    }

    public void addPager(final ArrayList<Book> bookArray){
        books.clear();
        books.addAll(bookArray);
        for(int i = 0; i < books.size(); i++) {
            bookObj = books.get(i);
            bookDetailsFragment = BookDetailsFragment.newInstance(bookObj);
            bookFragmentAdapter.add(bookDetailsFragment);
        }
        bookFragmentAdapter.getItemPosition(bookObj);
        bookFragmentAdapter.notifyDataSetChanged();
    }


    class BookFragmentAdapter extends FragmentStatePagerAdapter {

        ArrayList<BookDetailsFragment> bookFragments;


        public BookFragmentAdapter(FragmentManager fm) {
            super(fm);
            this.bookFragments = new ArrayList<>();
        }
        public void add(BookDetailsFragment fragment){
            bookFragments.add(fragment);
        }
        @Override
        public Fragment getItem(int i) {
            return bookFragments.get(i);
        }

        @Override
        public int getCount() {
            return bookFragments.size();
        }
    }

}
