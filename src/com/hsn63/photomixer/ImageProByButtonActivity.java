package com.hsn63.photomixer;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ImageProByButtonActivity extends Activity {
    private GestureDetector gGestureDetector;
    private GestureDetector oGestureDetector;
    private RelativeLayout rGroundLayout;
    private RelativeLayout rOverlayLayout;
    private DrawImageView groundView;
    DrawImageView overlayView;
    private SeekBar alphaSeekBar;
    private SeekBar zoomSeekBar;
    private ToggleButton focusTButton;
    private ToggleButton tapControlTButton;
    private String touchEnable = "overlayView";
    private String touchMode = "MOVE";
    private Button strokeWidthButton;
    private AlertDialog.Builder alertDialog;
    private int strokeWidth = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //メニューバー非表示
        window.requestFeature(Window.FEATURE_NO_TITLE); //タイトルバー非表示
        final int displayWidth = window.getWindowManager().getDefaultDisplay().getWidth();
        final int displayHeight = window.getWindowManager().getDefaultDisplay().getHeight();
        
        FrameLayout fLayout = new FrameLayout(this);
        setContentView(fLayout);
        
        rGroundLayout = new RelativeLayout(this);
        fLayout.addView(rGroundLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        groundView = new DrawImageView(this, "groundImg", false);
        gGestureDetector = new GestureDetector(this, groundView.simpleOnGestureListener);
        rGroundLayout.addView(groundView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        rOverlayLayout = new RelativeLayout(this);
        fLayout.addView(rOverlayLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        overlayView = new DrawImageView(this, "overlayImg", true);
        oGestureDetector = new GestureDetector(this, overlayView.simpleOnGestureListener);
        rOverlayLayout.addView(overlayView);
        rOverlayLayout.requestFocus();
        
        alphaSeekBar = new SeekBar(this);
        alphaSeekBar.setId(1);
        alphaSeekBar.setMax(254);
        alphaSeekBar.setProgress(254);
        alphaSeekBar.setVisibility(View.INVISIBLE);
        LayoutParams asbRL = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        asbRL.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rOverlayLayout.addView(alphaSeekBar, asbRL);
        alphaSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            // トラッキング開始時
            public void onStartTrackingTouch(SeekBar alphaSeekBar) {
                Log.v("onStartTrackingTouch()",
                    String.valueOf(alphaSeekBar.getProgress()));
                if (touchEnable != "overlayView") {
                    groundView.alpha = alphaSeekBar.getProgress();
                    groundView.invalidate();
                }else {
                    overlayView.alpha = alphaSeekBar.getProgress();
                    overlayView.invalidate();
                }
            }
            // トラッキング中
            public void onProgressChanged(SeekBar alphaSeekBar, int progress, boolean fromTouch) {
                Log.v("onProgressChanged()",
                    String.valueOf(progress) + ", " + String.valueOf(fromTouch));
                if (String.valueOf(fromTouch) != null) {
                    if (touchEnable != "overlayView") {
                        groundView.alpha = alphaSeekBar.getProgress();
                        groundView.invalidate();
                    }else {
                        overlayView.alpha = alphaSeekBar.getProgress();
                        overlayView.invalidate();
                    }
                }
            }
            // トラッキング終了時
            public void onStopTrackingTouch(SeekBar alphaSeekBar) {
                Log.v("onStopTrackingTouch()",
                    String.valueOf(alphaSeekBar.getProgress()));
//                view.alpha = alphaSeekBar.getProgress();
//                view.invalidate();
            }
        });
        
        zoomSeekBar = new SeekBar(this);
        zoomSeekBar.setId(2);
        zoomSeekBar.setMax(150);
        zoomSeekBar.setVisibility(View.INVISIBLE);
        LayoutParams zsbRL = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        zsbRL.addRule(RelativeLayout.ABOVE, 1);
        rOverlayLayout.addView(zoomSeekBar, zsbRL);
        zoomSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int barDelta = 0;
         // トラッキング開始時
            public void onStartTrackingTouch(SeekBar zoomSeekBar) {
                Log.v("onStartTrackingTouch()",
                    String.valueOf(zoomSeekBar.getProgress()));
            }
            // トラッキング中
            public void onProgressChanged(SeekBar zoomSeekBar, int progress, boolean fromTouch) {
                Log.v("onProgressChanged()",
                    String.valueOf(progress) + ", " + String.valueOf(fromTouch));
                if (String.valueOf(fromTouch) != null) {
                    if (touchEnable != "overlayView") {
                        Log.v("barDelta",""+barDelta);
                        if (zoomSeekBar.getProgress() + 100 - (int)(groundView.fImageScale / groundView.fMatchScale * 100) != 0) {
                            groundView.scaleFactor = (float)(zoomSeekBar.getProgress() + 100) * groundView.fMatchScale / 100f / groundView.fImageScale;
                            groundView.fImageScale = ((float)zoomSeekBar.getProgress() / 100 + 1f) * groundView.fMatchScale;
                            Log.v("scale","MatchScale:"+groundView.fMatchScale+", ImageScale:"+groundView.fImageScale);
                            groundView.setZoomMatrix((float)displayWidth / 2, (float)displayHeight / 2);
                            groundView.invalidate();
                        }
                    }else {
                        Log.v("barDelta",""+barDelta);
                        if (zoomSeekBar.getProgress() + 100 - (int)(overlayView.fImageScale / overlayView.fMatchScale * 100) != 0) {
                            overlayView.scaleFactor = (float)(zoomSeekBar.getProgress() + 100) * overlayView.fMatchScale / 100f / overlayView.fImageScale;
                            overlayView.fImageScale = ((float)zoomSeekBar.getProgress() / 100 + 1f) * overlayView.fMatchScale;
                            Log.v("scale","MatchScale:"+overlayView.fMatchScale+", ImageScale:"+overlayView.fImageScale);
                            overlayView.setZoomMatrix((float)displayWidth / 2, (float)displayHeight / 2);
                            overlayView.invalidate();
                        }
                    }
                }
            }
            // トラッキング終了時
            public void onStopTrackingTouch(SeekBar alphaSeekBar) {
                Log.v("onStopTrackingTouch()",
                    String.valueOf(alphaSeekBar.getProgress()));
//                view.alpha = alphaSeekBar.getProgress();
//                view.invalidate();
            }
        });
        
        focusTButton = new ToggleButton(this);
        focusTButton.setId(3);
        focusTButton.setTextOn("GroundImg");
        focusTButton.setTextOff("OverlayImg");
        focusTButton.setChecked(false);
        focusTButton.setVisibility(View.INVISIBLE);
        LayoutParams fbRL = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        fbRL.addRule(RelativeLayout.ABOVE, 2);
        rOverlayLayout.addView(focusTButton, fbRL);
        focusTButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                Log.v("focusTButton","call OnCheckdChangeListener");
                if (touchEnable != "overlayView") {
                    touchEnable = "overlayView";
                    alphaSeekBar.setProgress(overlayView.alpha);
                    zoomSeekBar.setProgress((int)(overlayView.fImageScale / overlayView.fMatchScale * 100) - 100);
                }else {
                    touchEnable = "groundView";
                    alphaSeekBar.setProgress(groundView.alpha);
                    zoomSeekBar.setProgress((int)(groundView.fImageScale / groundView.fMatchScale * 100) - 100);
                }
            }
        });
        
        tapControlTButton = new ToggleButton(this);
        tapControlTButton.setId(4);
        tapControlTButton.setTextOn("DRAW");
        tapControlTButton.setTextOff("MOVE");
        tapControlTButton.setChecked(false);
        tapControlTButton.setVisibility(View.INVISIBLE);
        LayoutParams tcbRL = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tcbRL.addRule(RelativeLayout.ABOVE, 2);
        tcbRL.addRule(RelativeLayout.RIGHT_OF, 3);
        rOverlayLayout.addView(tapControlTButton, tcbRL);
        tapControlTButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                Log.v("tapControlTButton","call OnCheckdChangeListener");
                if (touchMode != "MOVE") {
                    touchMode = "MOVE";
                }else {
                    touchMode = "DRAW";
                }
            }
        });

        strokeWidthButton = new Button(this);
        strokeWidthButton.setId(5);
        strokeWidthButton.setVisibility(View.INVISIBLE);
        LayoutParams swbRL = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        swbRL.addRule(RelativeLayout.ABOVE, 3);
        rOverlayLayout.addView(strokeWidthButton, swbRL);
        alertDialog = new AlertDialog.Builder(ImageProByButtonActivity.this);
        final CharSequence[] strokeWidthSet = {"1", "3", "6", "9"};
        strokeWidthButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Log.v("strokeWidthButton","call onClick");
                alertDialog.setTitle("Select StrokeWidth.");
                alertDialog.setSingleChoiceItems(strokeWidthSet, 1, new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int num) {
                        // TODO Auto-generated method stub
                        strokeWidthButton.setText(String.format("%s Point",strokeWidthSet[num]));
                        strokeWidth = Integer.parseInt(strokeWidthSet[num].toString());
                    }
                });
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                }).show();
            }
        });
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int d;
        if (touchEnable == "groundView") {
            Log.v("return","gGD");
            return gGestureDetector.onTouchEvent(event);
        }else {
            Log.v("return","oGD");
            if (touchMode == "DRAW" && event.getAction() == MotionEvent.ACTION_DOWN) {
//                Log.v("Action_down","X:"+event.getX()+", Y:"+event.getY());
                overlayView.drawPosX = (event.getX() - overlayView.matrixValues[Matrix.MTRANS_X]) / overlayView.fImageScale;
                overlayView.drawPosY = (event.getY() - overlayView.matrixValues[Matrix.MTRANS_Y]) / overlayView.fImageScale;
                d = alphaSeekBar.getProgress();
                alphaSeekBar.setProgress(0);
                alphaSeekBar.setProgress(254);
                alphaSeekBar.setProgress(d);
            }else if (touchMode == "DRAW" && event.getAction() == MotionEvent.ACTION_MOVE) {
                overlayView.canvasPaint.setStyle(Paint.Style.STROKE);
                overlayView.canvasPaint.setStrokeWidth(strokeWidth);
                overlayView.canvasPaint.setStrokeCap(Paint.Cap.ROUND);
                overlayView.canvasPaint.setAntiAlias(true);
                overlayView.canvasPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                overlayView.drawCanvas.drawLine(overlayView.drawPosX, overlayView.drawPosY, (event.getX()-overlayView.matrixValues[Matrix.MTRANS_X])/overlayView.fImageScale,
                        (event.getY()-overlayView.matrixValues[Matrix.MTRANS_Y])/overlayView.fImageScale, overlayView.canvasPaint);
                overlayView.drawPosX = (event.getX() - overlayView.matrixValues[Matrix.MTRANS_X]) / overlayView.fImageScale;
                overlayView.drawPosY = (event.getY() - overlayView.matrixValues[Matrix.MTRANS_Y]) / overlayView.fImageScale;
                overlayView.invalidate();
                return true;
            }
            return oGestureDetector.onTouchEvent(event);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu,menu);  
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
        case R.id.menu_save:
//            groundView.saveToFile();
//            overlayView.saveToFile();
            saveToFile();
            break;
        case R.id.menu_clean:
            groundView.clearDrawStatus();
            overlayView.clearDrawStatus();
            break;
        case R.id.menu_finish:
            finish();
            break;
        }
        return true;
    }
    

    public void saveToFile () {
        Bitmap saveBmp = null;
        if(!sdcardWriteReady()) {
            Toast.makeText(ImageProByButtonActivity.this, "SDcard not Found", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/test/");
        try{
            if(!file.exists()){
                file.mkdir();
            }
        } catch (SecurityException e) {
            Log.v("SecurityException",e.toString());
        }
        String AttachName = file.getAbsolutePath() + "/";
        AttachName += System.currentTimeMillis()+".jpg";
        File saveFile = new File(AttachName);
        do {
            AttachName = file.getAbsolutePath() + "/" + System.currentTimeMillis() +".jpg";
            saveFile = new File(AttachName);
        } while (saveFile.exists());
        try {
            FileOutputStream out = new FileOutputStream(AttachName);
            saveBmp = mixBitmap();
            saveBmp.compress(CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(ImageProByButtonActivity.this, "save successed!", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(ImageProByButtonActivity.this, "exception occured!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private Bitmap mixBitmap() {
        Bitmap _bitmap = Bitmap.createBitmap(groundView.nImageWidth, groundView.nImageHeight, Bitmap.Config.ARGB_8888);
        Canvas _canvas = new Canvas(_bitmap);
        _canvas.drawBitmap(groundView._bitmap, 0, 0, null);
        Matrix _matrix = overlayView.matrix;
        float[] matValues = new float[9];
        _matrix.getValues(matValues);
        matValues[Matrix.MSCALE_X] = overlayView.fImageScale / groundView.fImageScale;
        matValues[Matrix.MSCALE_Y] = overlayView.fImageScale / groundView.fImageScale;
        matValues[Matrix.MTRANS_X] = matValues[Matrix.MTRANS_X] - groundView.fMarginX;
        matValues[Matrix.MTRANS_Y] = matValues[Matrix.MTRANS_Y] - groundView.fMarginY;
        _matrix.setValues(matValues);
        _canvas.drawBitmap(overlayView.canvasBmp, _matrix, overlayView.paint);
        return _bitmap;
    }
    
    private boolean sdcardWriteReady () {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }
    
    // viewSize取得
//    @Override
//    public void onWindowFocusChanged (boolean hasFocus) {
//        final int displayWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
//        final int displayHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
//        Log.v("onWindowFocusChanged", "displayWidth: " + displayWidth + ", displayHeight: " + displayHeight);
//    }
    
    
    class DrawImageView extends View {

        private Activity _context;
        private Bitmap _bitmap = null;
        final private int nViewWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
        final private int nViewHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
        private int nImageWidth;
        private int nImageHeight;
        private float fMatchScale; //画面サイズにするScale
        private float fImageScale;
        private float scaleFactor = 2.5f;
        private float fMargin = 2f;
        private float fMarginX;
        private float fMarginY;
        private Matrix matrix = new Matrix();
        private float[] matrixValues = new float[9];
//        private int imageScaleCount;
        private float leftViewLimit;
        private float rightViewLimit;
        private float upperViewLimit;
        private float lowerViewLimit;
        private ScrollDistance scrollDistance = new ScrollDistance();
        private Paint paint = new Paint();
        private Paint canvasPaint = new Paint();
        private Bitmap canvasBmp = null;
        private boolean bmpCopy;
        private Canvas drawCanvas;
        private float drawPosX;
        private float drawPosY;
        private int alpha = 254;
//        private Rect rect = new Rect(0, 0, nViewWidth, nViewHeight);

        public DrawImageView(Context context, String fileKey, boolean bitmapCopy) {
            super(context);

            _context = (Activity)context;
            bmpCopy = bitmapCopy;
            Bundle bundle = getIntent().getExtras();
            Uri receivedUri = (Uri) bundle.get(fileKey);
            _bitmap = loadBitmap(getPath(context, receivedUri), 1024);
            setInitDrawStatus();
            canvasPaint.setColor(Color.argb(0, 0, 0, 0));
            if (bitmapCopy == true) {
                canvasBmp = _bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        }
        
        public Bitmap loadBitmap(String filePath, int nLimitLength) {
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // trueのときサイズ情報のみの取得
            BitmapFactory.decodeFile(filePath, options);
            
            int nWidth = options.outWidth; //読込みたい画像のwidth
            int nHeight = options.outHeight; //読込みたい画像のheight
            
            if(nWidth == 0 || nHeight == 0) {
                Toast t = Toast.makeText(
                        ImageProByButtonActivity.this, "Error: Image Not Found", Toast.LENGTH_SHORT );
                t.show();
                return null;
            }
            int nScale = getNScale(nWidth, nHeight, nLimitLength);
            
            options.inJustDecodeBounds = false;
            options.inSampleSize = nScale;
            options.inPreferredConfig = Config.ARGB_8888;
//            imageScaleCount = 0;
            return BitmapFactory.decodeFile(filePath, options);
        }
        
        public void setInitDrawStatus() {
            nImageWidth = _bitmap.getWidth();
            nImageHeight = _bitmap.getHeight();
            if((float)nViewHeight / nImageHeight > (float)nViewWidth / nImageWidth ) {
                fMatchScale = (float)(nViewWidth - 2 * fMargin) / nImageWidth;
                fMarginX = fMargin;
                fMarginY = (nViewHeight - fMatchScale * nImageHeight) / 2 + fMargin;
            }else{
                fMatchScale = (float)(nViewHeight - 2 * fMargin) / nImageHeight;
                fMarginX = (nViewWidth - fMatchScale * nImageWidth) / 2 + fMargin;
                fMarginY = fMargin;
            }
            fImageScale = fMatchScale;
            setMatrix();
        }
        
        protected void onSizeChanged(int _width, int _height, int _oldWidth, int _oldHeight) {
            super.onSizeChanged(_width, _height, _oldWidth, _oldHeight);
            if (bmpCopy == true) {
                Log.v("onSizeChanged","drawCanvas");
                drawCanvas = new Canvas(canvasBmp);
            }
        }
        
        public void saveToFile () {
            if(!sdcardWriteReady()) {
                Toast.makeText(_context, "SDcard not Found", Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/test/");
            try{
                if(!file.exists()){
                    file.mkdir();
                }
            } catch (SecurityException e) {
                Log.v("SecurityException",e.toString());
            }
            String AttachName = file.getAbsolutePath() + "/";
            AttachName += System.currentTimeMillis()+".jpg";
            File saveFile = new File(AttachName);
            do {
                AttachName = file.getAbsolutePath() + "/" + System.currentTimeMillis() +".jpg";
                saveFile = new File(AttachName);
            } while (saveFile.exists());
            try {
                FileOutputStream out = new FileOutputStream(AttachName);
                if(bmpCopy =! true){
                    _bitmap.compress(CompressFormat.JPEG, 100, out);
                } else{
                    canvasBmp.compress(CompressFormat.JPEG, 100, out);
                }
                out.flush();
                out.close();
                Toast.makeText(_context, "save successed!", Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                Toast.makeText(_context, "exception occured!", Toast.LENGTH_SHORT).show();
            }
        }
        
        private boolean sdcardWriteReady () {
            String state = Environment.getExternalStorageState();
            return (Environment.MEDIA_MOUNTED.equals(state));
        }
        
        public void clearDrawStatus() {
            if (bmpCopy == true) {
                canvasBmp = _bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
            alpha = 254;
            setInitDrawStatus();
            invalidate();
        }
        
        public int getNScale (int nWidth, int nHeight, int nLimitLength) {
            int _nScale = 1;
            float fScaleX = (float)nWidth / nLimitLength;
            float fScaleY = (float)nHeight / nLimitLength;
            float fScale = Math.max(fScaleX, fScaleY);
            
            while(fScale >= _nScale) {
                _nScale *= 2;
            }
            return _nScale;
        }
        
        public void setZoomStatus(float posX, float posY) {
//            if (imageScaleCount < 3) {
//                fImageScale *= scaleFactor;
//                imageScaleCount++;
//                setZoomMatrix(posX, posY);
//            }else {
//                fImageScale = fMatchScale;
//                imageScaleCount = 0;
//                setMatrix();
//            }
            Log.v("scale","MatchScale:"+fMatchScale+", ImageScale:"+fImageScale+", posX:"+posX+", posY:"+posY);
            if (fImageScale / fMatchScale < 2.49f) {
                scaleFactor = 2.5f * fMatchScale / fImageScale;
                fImageScale = 2.5f * fMatchScale;
//                imageScaleCount++;
                Log.v("scale","MatchScale:"+fMatchScale+", ImageScale:"+fImageScale);
                setZoomMatrix(posX, posY);
            }else {
                fImageScale = fMatchScale;
//                imageScaleCount = 0;
                Log.v("scale","MatchScale:"+fMatchScale+", ImageScale:"+fImageScale);
                setMatrix();
            }
        }
        
        public void setMatrix () {
            matrix.getValues(matrixValues);
            matrixValues[Matrix.MTRANS_X] = fMarginX;
            matrixValues[Matrix.MTRANS_Y] = fMarginY;
            matrixValues[Matrix.MSCALE_X] = fImageScale;
            matrixValues[Matrix.MSCALE_Y] = fImageScale;
            matrix.setValues(matrixValues);
        }
        
        public void setZoomMatrix (float posX, float posY){
            Log.v("setZoomMatrix","scaleFactor:"+scaleFactor+", ImageScale:"+fImageScale+", posX:"+posX+", posY:"+posY);
            matrix.getValues(matrixValues);
//            matrixValues[Matrix.MTRANS_X] = posX - fMatchScale * (posX - matrixValues[Matrix.MTRANS_X]);
//            matrixValues[Matrix.MTRANS_Y] = posY - fMatchScale * (posY - matrixValues[Matrix.MTRANS_Y]);
            Log.v("setZoomMatrix","MTRANS_X:"+matrixValues[Matrix.MTRANS_X]+", MTRANS_Y:"+matrixValues[Matrix.MTRANS_Y]);
            Log.v("setZoomMatrix","MSCALE_X:"+matrixValues[Matrix.MSCALE_X]+", MSCALE_Y:"+matrixValues[Matrix.MSCALE_Y]);
            matrixValues[Matrix.MTRANS_X] = getMatrixPos(posX, matrixValues[Matrix.MTRANS_X], nViewWidth, matrixValues[Matrix.MSCALE_X] * nImageWidth);
            matrixValues[Matrix.MTRANS_Y] = getMatrixPos(posY, matrixValues[Matrix.MTRANS_Y], nViewHeight, matrixValues[Matrix.MSCALE_Y] * nImageHeight);
            Log.v("setZoomMatrix","MTRANS_X:"+matrixValues[Matrix.MTRANS_X]+", MTRANS_Y:"+matrixValues[Matrix.MTRANS_Y]);
            Log.v("setZoomMatrix","MSCALE_X:"+matrixValues[Matrix.MSCALE_X]+", MSCALE_Y:"+matrixValues[Matrix.MSCALE_Y]);
            matrixValues[Matrix.MSCALE_X] = fImageScale;
            matrixValues[Matrix.MSCALE_Y] = fImageScale;
            matrix.setValues(matrixValues);
        }
        
        public float getMatrixPos(float pos, float margin, int viewLength, float imageLength) {
            if (imageLength * scaleFactor + 2 * fMargin < viewLength) {
                Log.v("getMatrixPos","1");
                return (float)(viewLength - (int)(imageLength * scaleFactor)) / 2 + fMargin;
            }else if ((pos - margin) * scaleFactor + fMargin < pos ) {
                Log.v("getMatrixPos","2");
                return fMargin;
            }else if((margin + imageLength - pos) * scaleFactor + fMargin < viewLength - pos) {
                Log.v("getMatrixPos","3");
                return viewLength - (imageLength * scaleFactor + 2 * fMargin);
            }else {
                Log.v("getMatrixPos","4");
                return pos - (pos - margin) * scaleFactor;
            }
        }
        
        public void setScrollMatrix () {
//            matrix.postTranslate(- scrollDistance.getX(), - scrollDistance.getY());
            matrix.getValues(matrixValues);
            matrixValues[Matrix.MTRANS_X] -= scrollDistance.getX();
            matrixValues[Matrix.MTRANS_Y] -= scrollDistance.getY();
            matrix.setValues(matrixValues);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            if(_bitmap == null) {
                return;
            }
            
            paint.setColor(Color.argb(alpha, 0, 0, 0));
//            Log.v("paintColor", "" + Integer.toHexString(paint.getColor()));
            if (bmpCopy != true) {
                canvas.drawBitmap(_bitmap, matrix, paint);
            }else {
                canvas.drawBitmap(canvasBmp, matrix, paint);
            }
        }
        
        private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                setZoomStatus(event.getX(), event.getY());
                Log.v("eventViewer", "onDoubleTap" + ", zSbar:" + ((int)(fImageScale / fMatchScale * 100) - 100));
                zoomSeekBar.setProgress((int)(fImageScale / fMatchScale * 100) - 100);
                invalidate();
                return super.onDoubleTap(event);
            }
            
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                Log.v("eventViewer", "onSingleTapConfirmed");
                return super.onSingleTapConfirmed(event);
            }
            
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.v("eventViewer", "onScroll");
                Log.v("e1", ""+ e1);
                Log.v("e2", "" + e2);
                Log.v("disX, disY", "" + distanceX + ", " + distanceY);
                setScrollDistance(distanceX, distanceY);
//                Log.v("after setScrollDistance", "disX:" + scrollDistance.getX() + ", disY:" + scrollDistance.getY());
                if (Math.abs(scrollDistance.getX()) > 0.1f || Math.abs(scrollDistance.getY()) > 0.1f) {
                    setScrollMatrix();
                    invalidate();
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
            
            @Override
            public void onLongPress(MotionEvent event) {
                Log.v("eventViewer", "onLongPress");
                super.onLongPress(event);
                if (alphaSeekBar.getVisibility() != View.VISIBLE) {
                    alphaSeekBar.setVisibility(View.VISIBLE);
                    zoomSeekBar.setVisibility(View.VISIBLE);
                    focusTButton.setVisibility(View.VISIBLE);
                    tapControlTButton.setVisibility(View.VISIBLE);
                    strokeWidthButton.setVisibility(View.VISIBLE);
                }else {
                    alphaSeekBar.setVisibility(View.INVISIBLE);
                    zoomSeekBar.setVisibility(View.INVISIBLE);
                    focusTButton.setVisibility(View.INVISIBLE);
                    tapControlTButton.setVisibility(View.INVISIBLE);
                    strokeWidthButton.setVisibility(View.INVISIBLE);
                }
            }
        };
        
        public boolean getEventFlag(float posX, float posY) {
//          Log.v("getPos", "posX:" + posX + ", posY:" + posY);
            setViewRangeLimit();
            if (posX > leftViewLimit && posX < rightViewLimit
                    && posY > upperViewLimit && posY < lowerViewLimit) {
                return true;
            }else {
                return false;
            }
        }
        
        public void setScrollDistance(float distanceX, float distanceY) {
//            if (imageScaleCount == 0) {
//                scrollDistance.setValues(0f, 0f);
//                return;
//            }
            setViewRangeLimit();
            if (nImageWidth * fImageScale < nViewWidth - 4f) {
                distanceX = 0f;
            }else if (leftViewLimit - distanceX > 2f) {
                if (distanceX < 0) {
                    distanceX = leftViewLimit - 2f;
                }
            }else if ((rightViewLimit + 2f) - distanceX < nViewWidth) {
                if (distanceX > 0) {
                    distanceX = (rightViewLimit + 2f) - nViewWidth;
                }
            }
            if (nImageHeight * fImageScale < nViewHeight - 4f) {
                distanceY = 0f;
            }else if (upperViewLimit - distanceY > 2f) {
                if (distanceY < 0) {
                    distanceY = upperViewLimit - 2f;
                }
            }else if ((lowerViewLimit +2f) - distanceY < nViewHeight) {
                if (distanceY > 0) {
                    distanceY = (lowerViewLimit + 2f) - nViewHeight;
                }
            }
            scrollDistance.setValues(distanceX, distanceY);
        }
        
        public void setViewRangeLimit() {
            leftViewLimit = matrixValues[Matrix.MTRANS_X];
            rightViewLimit = (float) (matrixValues[Matrix.MTRANS_X] + nImageWidth * fImageScale);
            upperViewLimit = matrixValues[Matrix.MTRANS_Y];
            lowerViewLimit = (float) (matrixValues[Matrix.MTRANS_Y] + nImageHeight * fImageScale);
//          Log.v("limit", "left:" + leftViewLimit + ", right:" + rightViewLimit + ", upper:" + upperViewLimit + ", lower:" + lowerViewLimit);
        }
    }
    
    // UriからimagePath取得
    public String getPath(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String[] columns = { MediaStore.Images.Media.DATA };
        Cursor cursor = contentResolver.query(uri, columns, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(0);
        cursor.close();
        return path;
    }
}

class ScrollDistance {
    
    private float X;
    private float Y;
    
    public void setValues(float x, float y) {
        this.X = x;
        this.Y = y;
    }
    
    public float getX() {
        return this.X;
    }
    
    public float getY() {
        return this.Y;
    }
}
