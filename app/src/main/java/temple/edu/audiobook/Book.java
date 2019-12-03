package temple.edu.audiobook;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Book implements Parcelable {
    int id;
    String name;
    String author;
    int published;
    String coverURL;
    int duration;

    public Book(JSONObject obj) throws JSONException {
        this.name = obj.getString("title");
        this.author = obj.getString("author");
        this.coverURL = obj.getString("cover_url");
        this.id = obj.getInt("book_id");
        this.published = Integer.parseInt(obj.getString("published"));
        this.duration = obj.getInt("duration");
    }

    protected Book(Parcel in) {
        id = in.readInt();
        name = in.readString();
        author = in.readString();
        published = in.readInt();
        coverURL = in.readString();
        //duration = in.readInt();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public int getDuration() {
        return duration;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(author);
        parcel.writeInt(published);
        parcel.writeString(coverURL);
    }
}
