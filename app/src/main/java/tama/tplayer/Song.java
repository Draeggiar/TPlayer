package tama.tplayer;

import android.net.Uri;

public class Song {
	
	private long id;
	private String title;
	private String artist;
	private Uri album;
	
	public Song(long songID, String songTitle, String songArtist, Uri albumArtUri){
        if (songID == 0 && songTitle == null && songArtist == null && albumArtUri == null) {
            id = 0;
            title = " ";
            artist = " ";
            album = Uri.parse("content://media/external/audio/albumart/1");
        }
        else {
            id = songID;
            title = songTitle;
            artist = songArtist;
            album = albumArtUri;
        }
	}
	
	public long getID(){return id;}
	public String getTitle(){return title;}
	public String getArtist(){return artist;}
	public Uri getAlbum(){return  album;}
}
