package com.hsn63.photomixer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //メニューバー非表示
        window.requestFeature(Window.FEATURE_NO_TITLE); //タイトルバー非表示
        setContentView(R.layout.activity_main);

        View openButton = findViewById(R.id.button_start);
        openButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch(view.getId()) {
        case R.id.button_start:
            Intent intent2 = new Intent(this, Pick2GalleryActivity.class);
            startActivity(intent2);
            break;
        }

    }
}
