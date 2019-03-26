package tama.tplayer;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Klasa określająca kontroler muzyki. Rozszerza domyślną klasę kontrolera MediaController.
 */
public class MusicController extends MediaController {

    /**
     * Publiczny konstruktor wywołujący konstruktro klasy bazowe
     *
     * @param c context
     */
    public MusicController(Context c) {
        super(c);
    }

    /**
     * Metoda pokazująca odtwarzacz, wywołuje metodę z klasy bazowej.
     */
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
    }

    /**
     * Metoda obsługująca zdarzenia klawiszy. W naszym wypadku chowa odtwarzacz i kontunuuje odtwarzanie
     * w tle.
     *
     * @param event obiekt klasy KeyEvent
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.hide();
            Context c = getContext();
            ((Activity) c).moveTaskToBack(true);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
