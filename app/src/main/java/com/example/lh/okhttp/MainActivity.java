package com.example.lh.okhttp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tv;
    private Button btn;
    private OkHttpClient client;
    private UIHandler mHandler;
    private ImageView imageView;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!checkStoragePermissions(this)) {
            String[] string = new String[2];
            string[0] = Manifest.permission.READ_EXTERNAL_STORAGE;
            string[1] = Manifest.permission.READ_SMS;
            requestPermissions(string, 1);
        }
        tv =  findViewById(R.id.tv);
        btn =  findViewById(R.id.btn);
        imageView = findViewById(R.id.image);
        progressBar = findViewById(R.id.pb);
        btn.setOnClickListener(this);
        client = new OkHttpClient();
        progressBar.setMax(100);
        progressBar.setProgress(0);
        mHandler = new UIHandler(tv);

    }


    private void connectInternet() {
        final String url = "http://mbdapp.iqiyi.com/j/ap/qiyi.196.apk";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Message msg = new Message();
                msg.what = 0;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                InputStream inputStream;
                long currentSize = 0;
                int len = 0;
                inputStream = response.body().byteStream();
                byte[] buf = new byte[2048];
                int[] translate = new int[2];
                long fileLength = response.body().contentLength();
                Log.i("lihang","length  ="+fileLength);
                int i = 0;
//                BitmapFactory.Options ops = new BitmapFactory.Options();
//                ops.inJustDecodeBounds = false;
               // Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, ops);
                String path = Environment.getExternalStorageDirectory().toString()+"/Movies";
                Log.i("lihang","path ="+path);
                String fileName = getFileName(url);
                File file = new File(path, fileName);
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
               // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                while((len = inputStream.read(buf)) != -1) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    msg.what = 2;
                    currentSize += len;
                    //Log.i("lihang","currentSize ="+currentSize);
                    bundle.putLong("fileLength", fileLength);
                    bundle.putLong("currentSize", currentSize);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    fileOutputStream.write(buf);

                }
                Log.i("lihang","Download Completed");
                fileOutputStream.flush();
                inputStream.close();
                fileOutputStream.close();

            }
        });
    }

    public String getFileName(String url) {
        int index  = url.lastIndexOf("/");
        String subString = url.substring(index,url.length());
        return subString;
    }

    @Override
    public void onClick(View v) {
        connectInternet();
        if("联网".equals(btn.getText())) {
            btn.setText("已联网");
        } else {
            btn.setText("联网");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public  boolean checkStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    class UIHandler extends Handler {
        private TextView tv;
        public UIHandler(TextView tv) {
           this.tv = tv;
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            float rote;
            if (msg.what == 1) {
                tv.setText("sucess");
                imageView.setImageBitmap((Bitmap)msg.obj);
            } else if (msg.what == 2) {
                Bundle bundle = msg.getData();
                long currentSize = bundle.getLong("currentSize");
                long fileLength = bundle.getLong("fileLength");
                if(currentSize > 0 && fileLength > 0) {
                    rote = currentSize * 100 / fileLength;
                } else {
                    rote = 0;
                }
                progressBar.setProgress((int)rote);
                tv.setText(rote+"%");
            } else {
                tv.setText("error");
            }
        }
    }

}
