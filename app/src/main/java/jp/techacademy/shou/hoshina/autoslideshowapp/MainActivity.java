package jp.techacademy.shou.hoshina.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private Cursor cursor;
    Timer mTimer;
    Handler mHandler = new Handler();

    private Button mNextButton;
    private Button mPreviousButton;
    private Button mRunStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Android 6.0以降の場合
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //パーミッションの許可状態を確認する
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d("ANDROID", "許可されている");
                //許可されている
                InitContentsInfo();

            }else{
                Log.d("ANDROID", "許可されていない");
                //許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }else{
            Log.d("ANDROID", "Android 5系以下");
            //Android 5系以下の場合
            InitContentsInfo();

        }

        //次へボタン
        mNextButton = (Button)findViewById(R.id.btnNext);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getContentsInfo(1, true);
            }
        });

        //戻るボタン
        mPreviousButton = (Button)findViewById(R.id.btnPrevious);
        mPreviousButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getContentsInfo(-1, true);
            }
        });

        //再生・停止ボタン
        mRunStop = (Button)findViewById(R.id.btnRunStop);
        mRunStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mTimer == null) {
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getContentsInfo(1, true);
                                }
                            });
                        }
                    }, 2000, 2000);
                    mRunStop.setText("停止");
                    mNextButton.setEnabled(false);
                    mPreviousButton.setEnabled(false);

                }else{
                    mTimer.cancel();
                    mTimer = null;
                    mNextButton.setEnabled(true);
                    mPreviousButton.setEnabled(true);
                    mRunStop.setText("再生");
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cursor.close();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    InitContentsInfo();

                }
                break;
            default:
                break;
        }
    }

    //初期化
    public void InitContentsInfo(){
        //画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if(cursor.moveToFirst()){
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            ImageView imageVIew = (ImageView)findViewById(R.id.imageView);
            imageVIew.setImageURI(imageUri);
        }
    }

    //次の画像を表示する
    //moveFlg -1:Previous 1:Next
    //isLoop ループする/しない
    public void getContentsInfo(int moveFlg, boolean isLoop){

        if(cursor == null){
            return;
        }
        //写真が1つもなかった場合処理を抜ける
        if(cursor.getCount() == 0){
            return;
        }

        int nowCursorPosition = cursor.getPosition();
        int tmpCursorPosition = nowCursorPosition + moveFlg;

        if(isLoop){
            if(-1 == tmpCursorPosition){
                tmpCursorPosition = cursor.getCount() -1;
            }else if(tmpCursorPosition == cursor.getCount()){
                tmpCursorPosition = 0;
            }
        }

        if (-1 < tmpCursorPosition && tmpCursorPosition < cursor.getCount()){
            //カーソル位置を変更
            cursor.moveToPosition(tmpCursorPosition);
        }

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView)findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);

    }
}
