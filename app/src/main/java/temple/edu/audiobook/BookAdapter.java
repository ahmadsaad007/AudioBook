package temple.edu.audiobook;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BookAdapter extends BaseAdapter {
    Context context;
    ArrayList<Book> bookList;

    public BookAdapter(Context context, ArrayList<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView = new TextView(context);
        textView.setText(bookList.get(position).name);
        textView.setTextSize(25);

        return textView;
    }
}