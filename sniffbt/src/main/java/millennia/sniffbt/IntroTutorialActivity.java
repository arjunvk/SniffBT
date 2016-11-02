package millennia.sniffbt;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class IntroTutorialActivity extends IntroActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){

        /* Enable/disable fullscreen */
        setFullscreen(true);

        super.onCreate(savedInstanceState);

        // Slide #1
        addSlide(new SimpleSlide.Builder()
                 .title(R.string.slide1_title)
                 .description(R.string.slide1_description)
                 .background(R.color.slide1_background)
                 .build());
    }
}
