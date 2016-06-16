package tama.tplayer;

import java.io.IOException;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {
	
	//song list and layout
	private ArrayList<Song> songs;
	private LayoutInflater songInf;
	private Context context;
	
	//constructor
	public SongAdapter(Context c, ArrayList<Song> theSongs){
		songs=theSongs;
		songInf=LayoutInflater.from(c);
		context=c;
	}

	@Override
	public int getCount() {
		return songs.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//map to song layout
        RelativeLayout songLay = (RelativeLayout) songInf.inflate(R.layout.song, parent, false);
		//get title and artist views
		ImageView albumCoverView = (ImageView) songLay.findViewById(R.id.song_albumCover);
		TextView songView = (TextView)songLay.findViewById(R.id.song_title);
		TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
		//get song using position
		Song currSong = songs.get(position);
		//get title and artist strings
		songView.setText(currSong.getTitle());
		artistView.setText(currSong.getArtist());
		Bitmap bmp = null;
		try {
			bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), currSong.getAlbum());
		} catch (IOException e) {
			e.printStackTrace();
		}
		albumCoverView.setImageBitmap(bmp);
		//set position as tag
		songLay.setTag(position);
		return songLay;
	}
}
