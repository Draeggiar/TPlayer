package tama.tplayer;

import android.net.Uri;

/**
 * Klasa zawierająca definicję obiektu Song, czyli definicję pojedynczej piosenki.
 */
public class Song {

    /**
     * Parametry piosenki: id, tytuł, wykonawca, ścieżka do miniatury albumu
     */
	private long id;
	private String title;
	private String artist;
	private Uri album;

    /**
     * Konstruktor wypełniający dane piosenki
     *
     * @param songID
     * @param songTitle
     * @param songArtist
     * @param albumArtUri
     */
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

    /**
     * Gettery do poszczególnych atrybutów piosenek
     */
	public long getID(){return id;}
	public String getTitle(){return title;}
	public String getArtist(){return artist;}
	public Uri getAlbum(){return  album;}
}
