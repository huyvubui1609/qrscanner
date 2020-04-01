package com.example.qrscanner.Main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrscanner.Dialog.ExitDialog;
import com.example.qrscanner.Dialog.WarningDialog;
import com.example.qrscanner.Model.QRGeoModel;
import com.example.qrscanner.Model.QRURLModel;
import com.example.qrscanner.Model.QRVCard;
import com.example.qrscanner.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.FileNotFoundException;
import java.io.InputStream;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ClipboardManager clipboardManager;
    private ClipData clipData;
    private ZXingScannerView scannerView;
    private TextView tvResult;
    private Button btnSearch, btnBrowse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        innit();
        EventHandler();
    }

    private void innit() {
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        scannerView = findViewById(R.id.scanner_view);
        tvResult = findViewById(R.id.tv_result);
        btnSearch = findViewById(R.id.btn_search);
        btnBrowse = findViewById(R.id.btn_browse);
    }

    private void EventHandler() {
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                scannerView.setResultHandler(MainActivity.this);
                scannerView.startCamera();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(MainActivity.this, "App won't start without your permission!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            }
        }).check();
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (tvResult.getText().toString().trim().equals("...")) {
                    WarningDialog dialog = new WarningDialog(MainActivity.this);
                } else {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/#q=" + tvResult.getText())));
                }
            }
        });
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(pickIntent, 111);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //the case is because you might be handling multiple request codes here
            case 111:
                if (data == null || data.getData() == null) {
                    Log.e("TAG", "The uri is null, probably the user cancelled the image selection process using the back button.");
                    return;
                }
                Uri uri = data.getData();
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    Log.e("TAG", "uri is not a bitmap," + uri.toString());
                    return;
                }
                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmap.recycle();
                bitmap = null;
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                MultiFormatReader reader = new MultiFormatReader();
                try {
                    Result result = null;
                    try {
                        result = reader.decode(bBitmap);
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        tvResult.setText(result.getText());
                    } catch (Exception e) {
                        WarningDialog dialog = new WarningDialog(this);
                    }

                } catch (Resources.NotFoundException e) {
                    Log.e("TAG", "decode exception", e);
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onBackPressed() {
        ExitDialog dialog = new ExitDialog(this);
    }

    @Override
    public void handleResult(Result rawResult) {
        processRawResult(rawResult.getText());
    }

    @SuppressLint("SetTextI18n")
    private void processRawResult(String text) {
        if (text.startsWith("BEGIN:")) {
            String[] tokens = text.split("\n");
            QRVCard qrvCard = new QRVCard();
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].startsWith("BEGIN:")) {
                    qrvCard.setType(tokens[i].substring("BEGIN:".length()));
                } else if (tokens[i].startsWith("N:")) {
                    qrvCard.setType(tokens[i].substring("N:".length()));
                } else if (tokens[i].startsWith("ORG:")) {
                    qrvCard.setType(tokens[i].substring("ORG:".length()));
                } else if (tokens[i].startsWith("TEL:")) {
                    qrvCard.setType(tokens[i].substring("TEL:".length()));
                } else if (tokens[i].startsWith("URL:")) {
                    qrvCard.setType(tokens[i].substring("URL:".length()));
                } else if (tokens[i].startsWith("EMAIL:")) {
                    qrvCard.setType(tokens[i].substring("EMAIL:".length()));
                } else if (tokens[i].startsWith("ADR:")) {
                    qrvCard.setType(tokens[i].substring("ADR:".length()));
                } else if (tokens[i].startsWith("NOTE:")) {
                    qrvCard.setType(tokens[i].substring("NOTE:".length()));
                } else if (tokens[i].startsWith("SUMMARY:")) {
                    qrvCard.setType(tokens[i].substring("SUMMARY:".length()));
                } else if (tokens[i].startsWith("DTSTART:")) {
                    qrvCard.setType(tokens[i].substring("DTSTART:".length()));
                } else if (tokens[i].startsWith("DTEND:")) {
                    qrvCard.setType(tokens[i].substring("DTEND:".length()));
                }
                tvResult.setText(qrvCard.getType());
            }
        } else if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("http://")) {
            QRURLModel qrurlModel = new QRURLModel(text);
            tvResult.setText(qrurlModel.getUrl());
        } else if (text.startsWith("geo:")) {
            QRGeoModel qrGeoModel = new QRGeoModel();
            String delims = "[ , ?q= ]+";
            String[] tokens = text.split(delims);

            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].startsWith(" geo:")) {
                    qrGeoModel.setLat(tokens[i].substring("geo:".length()));
                }
            }
            qrGeoModel.setLat(tokens[0].substring("geo:".length()));
            qrGeoModel.setLng(tokens[1]);
            qrGeoModel.setGeo_place(tokens[2]);
            tvResult.setText(qrGeoModel.getLat() + "/" + qrGeoModel.getLng());
        } else {
            tvResult.setText(text);
        }

        scannerView.resumeCameraPreview(MainActivity.this);
    }


    @Override
    protected void onResume() {
        scannerView.resumeCameraPreview(MainActivity.this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }
}
