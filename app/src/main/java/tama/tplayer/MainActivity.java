package tama.tplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.ContentUris;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import tama.tplayer.MusicService.MusicBinder;
import android.widget.MediaController.MediaPlayerControl;

/**
 * Klasa zawierająca główną aktywność programu. Implementuje ona interfejs MediaPlayerControl, pozwalający zarządzać multimediami.
 */

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {
	/**
	 * Metoda OnCreate, wywoływana przy tworzeniu aktywności.
	 * Inicjujemy w niej obiekt listy, przechowujący listę piosenek pobieranych z urządzenia za pomocą
	 * metody getSongList. Następnie do obiekty listy dodawany jest adapter, odpowiadający za poprawne
	 * wyświetlanie jej na ekranie.
	 *
	 * @param savedInstanceState
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//retrieve list view
		songView = (ListView)findViewById(R.id.song_list);
		//instantiate list
		songList = new ArrayList<Song>();
		//get songs from device
		getSongList();
		//sort alphabetically by title
		Collections.sort(songList, new Comparator<Song>(){
			public int compare(Song a, Song b){
				return a.getTitle().compareTo(b.getTitle());
			}
		});
		//create and set adapter
		SongAdapter songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);
		setController();
	}

	/**
	 * Prywatne pole, w którym za pomocą klasy anonimowej zarządza połączeniem z usługą odtwarzającą
	 * muzykę.
	 */
	private ServiceConnection musicConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, final IBinder service) {
			MusicBinder binder = (MusicBinder)service;
			//get service
			musicSrv = binder.getService();
			//pass list
			musicSrv.setList(songList);
			musicBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	/**
	 * Publiczna metoda, wywoływana po kliknięciu na piosenkę z listy. Przekazuje ona wybraną piosenkę
	 * do usługi, oraz nicjuje odtwarzanie. Dodatkowo, jeżeli odtwarzanie było wcześniej zapauzowane,
	 * wznawia je.
	 *
	 * @param view widok z którego pobieramy kliknęcie, w naszym przypadku aktywnośc z listą
     */
	public void songPicked(View view){
		musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
		musicSrv.playSong();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}

	/**
	 * Metoda, która po uruchomieniu aplikacji, łączy się z usługą odtwarzającą muzykę.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if(playIntent==null){
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}

	/**
	 * Metoda dodająca elementy do górnego menu w aktywności. Używa do tego celu przygotowanego wcześniej
	 * pliku layoutu.
	 *
	 * @param menu referencja do górnego menu
	 * @return zwraca true jeżeli menu jest wyświetlane
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Metdoda obsługująca kliknięcia w górnym menu. Sprawdza który przycisk został kliknięty i w zależności
	 * od tego podejmuję odpowiednią akcję.
	 *
	 * @param item referencja do kliknitętego przycisku
	 * @return
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_equalizer:
                musicSrv.setEqualizer();
                break;
			case R.id.action_shuffle:
				musicSrv.setShuffle();
				break;
			case R.id.action_end:
				stopService(playIntent);
				musicSrv=null;
				System.exit(0);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Metoda wywoływana przy niszczeniu okna głównej aktywności. Zatrzymuje ona usługę odtwarzającą
	 * muzykę.
	 */
	@Override
	protected void onDestroy() {
		stopService(playIntent);
		musicSrv=null;
		super.onDestroy();
	}

	/**
	 * Metoda pobiera listę piosenek z urządzenia. Pobiera tylko pliki które są oznaczone w urządzeniu
	 * jako muzyka. Uzyskuje następujące informacje o każdym utworze: Id, Wykonawca, Tytuł oraz Id albumu.
	 * Tak pobrane informacje zapisuje w obiekcie Song reprezentującym piosenki, a następnie dodaje
	 * do listy.
	 */
	public void getSongList(){
		//query external audio
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
		String[] projection = {
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ALBUM_ID,
		};
		Cursor musicCursor = musicResolver.query(musicUri, projection, selection, null, null);
		//iterate over results if valid
		if(musicCursor!=null && musicCursor.moveToFirst()){
			//get columns
			int titleColumn = musicCursor.getColumnIndex
					(MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex
					(MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex
					(MediaStore.Audio.Media.ARTIST);
			int albumColumn = musicCursor.getColumnIndex
					(MediaStore.Audio.Media.ALBUM_ID);

			//add songs to list
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				long thisAlbumId = musicCursor.getLong(albumColumn);

				final Uri ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
				Uri albumArtUri = ContentUris.withAppendedId(ART_CONTENT_URI, thisAlbumId);

				songList.add(new Song(thisId, thisTitle, thisArtist, albumArtUri));
			}while (musicCursor.moveToNext());
		}
	}

	/**
	 * Metoda obsługująca kliknięcie przycisku "cofnij"
	 */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
		super.onBackPressed();
    }

	/**
	 * Metoda interfejsu MusicPlayerControll. Każe usłudze odtwarzającej muzykę zapauzować odtwarzanie
	 * i ustawia flgę pauzy na true.
	 */
	@Override
	public void pause() {
		playbackPaused=true;
		musicSrv.pausePlayer();
	}

	/**
	 * Metoda interfejsu MusicPlayerControll. Poprzez usługę MusicService ustawia odtwarzanie na
	 * pozycji @pos
	 *
	 * @param pos pozycje od której chcemy odtwarzać piosenkę
     */
    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

	/**
	 * Metoda interfejsu MusicPlayerControll. Informuje usługę MusicMusic service aby zaczęła odtwarzanie.
	 */
	@Override
	public void start() {
		musicSrv.go();
	}

	/**
	 * Metoda interfejsu MusicPlayerControll. Wysyła do usługi odtwarzającej zapytanie o długość
	 * odtwarzanego utworu, a następnie ją zwraca. W przypadku gdy nie mamy połączenia z udługą zwraca 0.
	 *
	 * @return zwraca długość obecnie odtwarzanego utworu, lub 0 gdy nie mamy połączenia z usługą.
     */
	@Override
	public int getDuration() {
		if(musicSrv!=null && musicBound && musicSrv.isPng())
			return musicSrv.getDur();
		else return 0;
	}

	/**
	 * Metoda interfejsu MusicPlayerControll.
	 *
	 * @return Zwraca aktualną pozycję na pasku odtwarzania lub 0
	 * gdy nie mamy połączenia z usługą.
     */
	@Override
	public int getCurrentPosition() {
		if(musicSrv!=null && musicBound && musicSrv.isPng())
			return musicSrv.getPosn();
		else return 0;
	}

	/**
	 * Metoda interfejsu MusicPlayerControll.
	 *
	 * @return zwraca true jeżeli aktualnie jest odtwarzany jakiś utwór i false w przeciwnym wypadku.
     */
	@Override
	public boolean isPlaying() {
		if(musicSrv!=null && musicBound)
			return musicSrv.isPng();
		return false;
	}

	/**
	 * Metoda interfejsu MusicPlayerControll. Nie jest używana w programie
	 *
	 * @return 0
     */
	@Override
	public int getBufferPercentage() {
		return 0;
	}

	/**
	 * Metoda interfejsu MusicPlayerControll. Określa czy odtwarzanie może zostać zapauzowane.
	 *
	 * @return true
     */
	@Override
	public boolean canPause() {
		return true;
	}

	/**
	 * Metoda wywoływana gdy aktywność przechodzi w stan paused. Pauzuje odtwarzanie.
	 */
	@Override
	protected void onPause(){
		super.onPause();
		paused=true;
	}

	/**
	 * Metoda obsługująca zdarzenie Resume. Jeżeli odtwarzanie było zapauzowane, wznawia je.
	 */
	@Override
	protected void onResume(){
		super.onResume();
		if(paused){
			setController();
			controller.show();
			paused=false;
		}
	}

	/**
	 * Metoda obsługująca zdarzenie Stop. Ukrywa pasek odtwarzacza.
	 */
	@Override
	protected void onStop() {
		controller.hide();
		super.onStop();
	}

	/**
	 * Metoda interfejsu MusicPlayerControll. Określa czy utwór może być przewijany w tył.
	 *
	 * @return true
     */
	@Override
	public boolean canSeekBackward() {
		return true;
	}

	/**
	 * Metoda interfejsu MusicPlayerControll. Określa czy utwór może być przewijany w przód.
	 *
	 * @return true
     */
	@Override
	public boolean canSeekForward() {
		return true;
	}

	/**
	 * Metoda interfejsu MusicPlayerControll. Nie używana w programie.
	 *
	 * @return 0
     */
	@Override
	public int getAudioSessionId() {
		return 0;
	}

	/**
	 * Dodaje do aktywności obiekt controllera, z poziomu którego zarządzamy odtwarzaniem.
	 */
	private void setController(){
		if(controller == null)
			controller = new MusicController(this);
		else
			controller.invalidate();

		controller.setPrevNextListeners(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playNext();
			}
		}, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPrev();
			}
		});

		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
	}

    /**
     * Metoda obsługująca kliknięcie na przycisk odtwarzania kolejnego utworu.
     */
	private void playNext(){
		musicSrv.playNext();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}

    /**
     * Metoda obsługująca kliknięcie na przycisk odtwarzania poprzedniego utworu.
     */
	private void playPrev(){
		musicSrv.playPrev();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}

	//zmienne listy piosenek
	private ArrayList<Song> songList;
	private ListView songView;

    // prywatne zmienne służące do komunikacji z usługą odtwarzajacą muzykę
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound=false;

    /**
     * @return getter zwracający obiekt kontrolera
     */
    public static MusicController getController() {
        return controller;
    }

    private static MusicController controller;  //prywatna zmienna kontrolera
	private boolean paused=false, playbackPaused=false; //prywatne zmiene flag przechowujących stan odtwarzania
}
