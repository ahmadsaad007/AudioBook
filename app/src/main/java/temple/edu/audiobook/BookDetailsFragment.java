package temple.edu.audiobook;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import androidx.fragment.app.Fragment;
public class BookDetailsFragment extends Fragment {

    private static final String BOOK_TITLE_KEY = "bookKey";
    private BookDetailsInterface mListener;
    Context c;
    private Book book;
    TextView textView;
    ImageView coverImageView;
    ImageButton PlayButton;
    Button deleteButton;
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
        deleteButton = v.findViewById(R.id.downDelete);
        if (book != null) {
            displayBook(book);
            PlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.playBook(book);
                }
            });
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isDownloaded()){
                    deleteButton.setText("Delete");
                    String file = getActivity().getFilesDir()
                            + File.separator + book.id  + ".mp3";
                    File fp = new File(file);
                    fp.delete();
                    Toast.makeText(getActivity(),"File Deleted",Toast.LENGTH_SHORT).show();
                    deleteButton.setText("Download");
                }
                else {
                    deleteButton.setText("Download");
                    new Download(getActivity()).execute();
                    Toast.makeText(getActivity(),"File Downloaded",Toast.LENGTH_SHORT).show();
                    deleteButton.setText("Delete");
                }
                //downlaod the book
            }
        });
        return v;
    }
    public boolean isDownloaded(){
        String file = getActivity().getFilesDir()
                + File.separator + book.id  + ".mp3";
        File fp = new File(file);
        if(fp.exists()){
            return  true;
        }
        return false;
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


    private  class Download extends AsyncTask<Void, Integer, Boolean> {
        Context context;

        public Download(Context context) {
            this.context = context;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                URL url = new URL("https://kamorris.com/lab/audlib/download.php?id="+book.id);
                URLConnection urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                int contentLength = urlConnection.getContentLength();
                String filePath = context.getFilesDir()
                        + File.separator + book.id + ".mp3";
                Log.e("value",filePath+"");
                int downloadSize = 0;
                byte[] bytes = new byte[1024];
                int length;
                OutputStream outputStream = new FileOutputStream(filePath);
                while ((length = inputStream.read(bytes)) != -1){
                    outputStream.write(bytes,0,length);
                    downloadSize += length;
                    publishProgress(downloadSize * 100/contentLength);
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("value",e.getLocalizedMessage()+"");
                return false;
            }
            return true;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.e("value",values[0]+"");
        }
        @Override
        protected void onPostExecute(Boolean download) {
            if(download) {
                Toast.makeText(context, "File Downloaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "File Download Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
