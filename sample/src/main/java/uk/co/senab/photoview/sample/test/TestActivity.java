package uk.co.senab.photoview.sample.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import uk.co.senab.photoview.sample.R;


public class TestActivity extends Activity {

    private TestImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        imageView = (TestImageView) findViewById(R.id.imageView);
    }
}
