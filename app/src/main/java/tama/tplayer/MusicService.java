package tama.tplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Usługa obsługująca odtwarzanie muzyki w programie. Imlementuje ona wybrane metody z
 * interfejsu MediaPlayer.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    /**
     * Metoda obsługująca zdarzenie onCreate. Służy jako konstruktor i przypisuje początkowe
     * wartośi wybraym polom w klasie.
     */
    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        rand=new Random();
        player = new MediaPlayer();
        initMusicPlayer();
    }

    /**
     * Metoda inicjalizująca odtwarzacz, którym jest prywatny obiekt MediaPlayer.
     */
    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    /**
     * Setter przypisujący wartość do prywatnego pola przechowującego listę piosenek.
     *
     * @param theSongs referencja do listy piosenek typu ArrayList
     */
    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    /**
     * Wewnętrzna klasa rozszerzajaca klasę Binder. Potrzebna do połączenia usługi z klasą głównej
     * aktywności. Zawiera metodę zwracającą obiekt do klasy głównej, ponieważ to ona jest usługą.
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    /**
     * Metoda inicjalizująca odtwarzanie dźwięku. Pobiera wybraną piosenkę z listy i próbuje
     * ją odtworzyć.
     */
    public void playSong(){
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        songTitle=playSong.getTitle();
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    /**
     * Włącza lub wyłącza odtwarzanie losowe po kliknięciu na przycisk.
     */
    public boolean setShuffle() {
        return shuffle = !shuffle;
    }

    /**
     * Uruchamia aktywność equalizera po kliknięciu na przycisk.
     */
    public void setEqualizer(){
        Intent intent = new Intent(this, EqualizerActivity.class);
        intent.putExtra("eq", player.getAudioSessionId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Ustawia wskaźnik na aktualnie wybraną piosenkę.
     *
     * @param songIndex pozycja wybranej piosenki na liście
     */
    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    /**
     * Zwraca aktualny czas odtwarzania
     *
     * @return zwraca czas w milisekundach
     */
    public int getPosn(){
        return player.getCurrentPosition();
    }

    /**
     * Zwraca długość aktualnie odtwarzanego utworu
     *
     * @return długość utworu w milisekundach, -1 gdy długość nie jest dostępna
     */
    public int getDur(){
        return player.getDuration();
    }

    /**
     * Zwraca aktualny stan odtwarzania
     *
     * @return true jeżeli aktualnie jest odtwarzany dźwięk
     */
    public boolean isPng(){
        return player.isPlaying();
    }

    /**
     * Metoda służąca do zapauzowania odtwarzacza
     */
    public void pausePlayer() {
        player.pause();
    }

    /**
     * Przewija odtwarzanie do wskazanej pozycji.
     *
     * @param posn offset od początku utworu, o jaki chcemy przesunąć odtwarzanie
     */
    public void seek(int posn){
        player.seekTo(posn);
    }

    /**
     * Wznawia odtwarzanie. Jeżeli było wcześniej zapauzowane, wznawia od momentu pauzy. Jeżeli nie,
     * zaczyna odtwarzanie od początku.
     */
    public void go(){
        player.start();
    }

    /**
     * Odtwarza utwór poprzedzajacy aktualny na liście. Jeżeli użyjemy tej odpcji dla pierwszego elementu,
     * odtworzony zostanie ostatni na liście.
     */
    public void playPrev(){
        songPosn--;
        if(songPosn<0) songPosn=songs.size()-1;
        playSong();
    }

    /**
     * Odtwarza następny utwór na liście. Jeżeli właczone jest odtwarzanie losowe, odtwarza losowy
     * utwór.
     */
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn>=songs.size()) songPosn=0;
        }
        playSong();
    }

    /**
     * Obługuje zdarzenie onBind. Zwraca obiekt klasy MusicBinder, służącej do połączenia głównej
     * aktywności z usługą.
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    /**
     * Metoda obsługująca zdarzenie onUnbind. Dba o prawidłowe rozłączenie z usługą.
     *
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    /**
     * Metoda obsługujące zdarzenie onCompletition, mające miejsce gdy odtwarzanie dobiegnie końca.
     * W naszym przypadku odtwarza kolejną piosenkę.
     *
     * @param mp referencja do obiektu klasy MediaPlayer dla którego nastąpiło zdarzenie
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    /**
     * Metoda obłsugująca zdarzenie onError. W przypadku wystąpienia błędu resetuje odtwarzacz.
     *
     * @param mp referencja do obiektu klasy MediaPlayer dla którego wystąpiło zdarzenie
     * @param what etykieta opisująca błąd
     * @param extra dodatkowe informacje o błędzie
     * @return false
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    /**
     * Metoda obsługująca zdarzenie onPrepared, występuje gdy odtwarzacz jest gotowy do odtwarzania
     * dźwięku. Dodajemy w niej panel odtwarzacza do okna głównej aktywności.
     *
     * @param mp uchwyt dla którego nastąpiło zdarzenie
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
        .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
        MainActivity.getController().show(0);
    }

    /**
     * Metoda obsługująca zdarzenie onDestroy. Usuwa aktywność z pierwszego planu, umożliwiając
     * zabicie jej gdy potrzeba więcej pamięci.
     */
    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;

    private final IBinder musicBind = new MusicBinder();
    private String songTitle=""; // tytuł aktualnie odtwarzanej piosenki
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false; // flaga określająca czy losowe odtwarzanie jest wlączone
    private Random rand;
}
