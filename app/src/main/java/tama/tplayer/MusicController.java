package tama.tplayer;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

/**
 * Created by Tama on 16.03.2016.
 */
public class MusicController extends MediaController {

    public MusicController(Context c){
        super(c);
    }

    public void hide(){}

    public void show(){
        super.show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            super.hide();
            Context c = getContext();
            ((Activity) c).moveTaskToBack(true);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
