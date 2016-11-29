package millennia.sniffbt;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import static millennia.sniffbt.CommonFunctions.isLocationServicesAvailable;

public class IntroTutorialActivity extends IntroActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        /* Enable/disable fullscreen */
        setFullscreen(true);
        setButtonBackVisible(false);
        setButtonCtaVisible(true);
        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);
        TypefaceSpan labelSpan = new TypefaceSpan(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "sans-serif-medium" : "sans serif");
        SpannableString label = SpannableString.valueOf(getString(R.string.intro_tutorial_btn));
        label.setSpan(labelSpan, 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setButtonCtaLabel(label);

        setPageScrollDuration(500);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPageScrollInterpolator(android.R.interpolator.fast_out_slow_in);
        }

        // Slide #1
        addSlide(new SimpleSlide.Builder()
                 .title(R.string.slide1_title)
                 .description(R.string.slide1_description)
                 .image(R.drawable.slide_1)
                 .background(R.color.slide1_background)
                 .build());

        // Slide #2
        addSlide(new SimpleSlide.Builder()
                .title(R.string.slide2_title)
                .description(R.string.slide2_description)
                .image(R.drawable.slide_2)
                .background(R.color.slide2_background)
                .build());

        // Slide #3
        addSlide(new SimpleSlide.Builder()
                .title(R.string.slide3_title)
                .description(R.string.slide3_description)
                .image(R.drawable.slide_3)
                .background(R.color.slide3_background)
                .build());

        // Slide #4
        addSlide(new SimpleSlide.Builder()
                .title(R.string.slide4_title)
                .description(R.string.slide4_description)
                .image(R.drawable.slide_4)
                .background(R.color.slide4_background)
                .build());

        // Slide #5
        addSlide(new SimpleSlide.Builder()
                .title(R.string.slide5_title)
                .description(R.string.slide5_description)
                .background(R.color.slide5_background)
                .build());

        if(!isLocationServicesAvailable(this.getApplicationContext())) {
            // slide for obtaining permissions
            addSlide(new SimpleSlide.Builder()
                    .title(R.string.permissions_title)
                    .description(R.string.permissions_description)
                    .background(R.color.permissions_background)
                    .image(R.drawable.location)
                    .permissions(new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION})
                    .build());
        }

        // Slide #6
        addSlide(new SimpleSlide.Builder()
                .title(R.string.slide6_title)
                .description(R.string.slide6_description)
                .image(R.drawable.slide_6)
                .background(R.color.slide6_background)
                .build());
    }
}
