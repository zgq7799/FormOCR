package com.formocr.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.formocr.R;
import com.formocr.util.ActivityCollector;
import com.formocr.util.Config;
import com.formocr.util.ParseJsonUtil;
import com.soundcloud.android.crop.Crop;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * OCR交互主页面
 * <p>
 * Created by ZhangGuanQun on 2017/5/18.
 */
public class MainActivity extends BaseActivity {
    /* 用于存放OCR识别结果的容器(TextView) */
    private TextView ocrResultView = null;
    /* OCR识别结果 */
    private String ocrResultStr = null;
    /* 被选中的图片的容器(ImageView) */
    private ImageView selectedPhotoView = null;
    /* 生成文件的按钮 */
    private Button createFileButton = null;
    /* 拍照文件的URI */
    private Uri captureURI = null;
    /* 照片裁剪的URI */
    private Uri croppedPhotoURI = null;
    /* 生成文件的URI */
    private Uri csvFileURI = null;
    /* 应用相关内容的存放路径 */
    private File fileDirectory = null;
    /* 被选中的图片 */
    Bitmap selectedBitMap = null;

    /* 拍照的Activity请求码 */
    private static final int PHOTO_CAPTURE = 0x00;
    /* 相册调用的Activity请求码 */
    private static final int CHOOSE_PHOTO = 0x01;
    /* 识别线程中正在识别的状态码 */
    private static final int IDENTIFYING = 0x20;
    /* 识别线程中识别结束的状态码 */
    private static final int IDENTIFIED = 0x21;
    /* 系统摄像头权限的请求码 */
    private static final int CAMERA_PERMISSION = 0x40;
    /* 系统外部存储读写权限的请求码 */
    private static final int WRITE_EXTERNAL_PERMISSION = 0x41;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* 相关文件放在应用关联缓存目录当中 */
        fileDirectory = getExternalCacheDir();
        /* 识别结果 */
        ocrResultView = (TextView) findViewById(R.id.ocr_result);
        /* 选取的图片 */
        selectedPhotoView = (ImageView) findViewById(R.id.selected_photo);
        /* 生成文件按钮 */
        createFileButton = (Button) findViewById(R.id.button_create_file);
        /* 拍照按钮 */
        Button capturePhotoButton = (Button) findViewById(R.id.button_camera);
        /* 相册按钮 */
        Button albumButton = (Button) findViewById(R.id.button_album);

        /* 申请必要的运行时权限 */
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.
                PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_PERMISSION);
        }

        /* 生成文件按钮监听 */
        createFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File csvFile = new File(getExternalFilesDir(null), "created_" +
                        new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",
                                Locale.getDefault()).format(new Date()) + ".csv");
                csvFileURI = getUriByFileProvider(csvFile);
                boolean createResult = createAsCsvFile(ocrResultStr, csvFile);
                if (!createResult) {
                    Toast.makeText(MainActivity.this, "生成失败!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "生成成功！路径是：" + csvFileURI, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(csvFileURI, "text/plain");
                    startActivity(intent);
                }
            }
        });

        /* 拍照按钮注册监听,并调用摄像头 */
        capturePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.
                        PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION);
                } else {
                    openCamera();
                }
            }
        });

        /* 相册按钮注册监听,并调用相册 */
        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum();
            }
        });
    }

    /**
     * 调用系统摄像头
     */
    private void openCamera() {
        File capturePhoto = new File(fileDirectory, "capture_" +
                new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date()));
        captureURI = getUriByFileProvider(capturePhoto);
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            /* 对目标应用临时授权该Uri所代表的文件 */
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, captureURI);
        startActivityForResult(intent, PHOTO_CAPTURE);
    }

    /**
     * 调用系统相册
     */
    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /**
     * 4.4以上版本解析相册图片Uri
     * <p>
     * 4.4版本以后,选取相册中的图片,返回的不是图片真实的uri,需要解析封装过的uri
     *
     * @param data Intent附带数据
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            /* 处理Document类型的uri*/
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            /* content类型的uri,正常处理 */
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            /* File类型的uri,直接获取图片路径 */
            imagePath = uri.getPath();
        }
        File photoFile = new File(imagePath);
        Uri photoURI = getUriByFileProvider(photoFile);
        photoCrop(photoURI);
    }

    /**
     * 通过文件提供器的方式获取Wrapper后的URI
     *
     * @param file 目标文件
     * @return 对应URI
     */
    private Uri getUriByFileProvider(File file) {
        Uri targetURI = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            /* 通过FileProvider创建一个content类型的Uri */
            targetURI = FileProvider.getUriForFile(
                    MainActivity.this, "com.formocr.fileprovider", file);
        } else {
            targetURI = Uri.fromFile(file);
        }
        return targetURI;
    }

    /**
     * 4.4以下版本解析相册图片Uri
     *
     * @param data Intent附带数据
     */
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        photoCrop(Uri.parse(imagePath));
    }

    /**
     * 获取图片路径
     *
     * @param uri       图片uri
     * @param selection 查询where的约束条件
     * @return 图片路径
     */
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 生成csv文件
     *
     * @param ocrResultStr 识别结果
     * @param csvFile      生成的csv文件
     */
    private boolean createAsCsvFile(String ocrResultStr, File csvFile) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(csvFile));
            bw.write(ocrResultStr);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /* 处理OCR识别线程的状态控制 */
    private Handler identifyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IDENTIFIED:
                    if (ocrResultStr == null) {
                        ocrResultView.setText("\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t识别异常!");
                    } else {
                        ocrResultView.setText(ocrResultStr);
                        /* 生成csv文件按钮生效 */
                        createFileButton.setEnabled(true);
                    }
                    break;
                case IDENTIFYING:
                    ocrResultView.setText("\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t正在识别,请稍候...");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "你禁止了打开摄像头的权限!", Toast.LENGTH_SHORT).show();
                }
                break;
            case WRITE_EXTERNAL_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "你禁止了打开外部存储的权限!", Toast.LENGTH_SHORT).show();
                    ActivityCollector.finishAll();
                }
                break;
            default:
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shares_item:
                if (csvFileURI != null) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, csvFileURI);
                    sendIntent.setType("text/comma-separated-values");
                    Toast.makeText(MainActivity.this, csvFileURI.toString(), Toast.LENGTH_LONG);
                    startActivity(Intent.createChooser(sendIntent, "share"));
                } else {
                    Toast.makeText(MainActivity.this, "还未生成待分享的文件！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* Intent进入一个新Activity后,返回数据给上一个Activity的回调方法 */
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        /* 处理拍照后的裁剪部分 */
        if (requestCode == PHOTO_CAPTURE) {
            photoCrop(captureURI);
        }

        /* 处理调用相册部分 */
        if (requestCode == CHOOSE_PHOTO) {
            if (Build.VERSION.SDK_INT >= 19) {
                /* 适配4.4以上的系统 */
                handleImageOnKitKat(data);
            } else {
                /* 适配4.4以下的系统 */
                handleImageBeforeKitKat(data);
            }
        }

        /* 对裁剪后图片的处理 */
        if (requestCode == Crop.REQUEST_CROP) {
            handleCroppedPhoto(croppedPhotoURI);
        }
    }

    /**
     * 调用相册裁剪后和相机拍照裁剪后的处理
     */
    private void handleCroppedPhoto(Uri photoURI) {
        /* 显示选择的图片Bitmap格式 */
        //selectedBitMap = decodeUriAsBitmap(photoURI);
        selectedBitMap = getImage(photoURI);
        selectedPhotoView.setImageBitmap(selectedBitMap);

        /* 启动一个线程来进行OCR识别 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                /* 传回正在识别的状态 */
                Message processMsg = new Message();
                processMsg.what = IDENTIFYING;
                identifyHandler.sendMessage(processMsg);
                ocrIdentify();
                /* 传回识别结束的状态 */
                Message finishMsg = new Message();
                finishMsg.what = IDENTIFIED;
                identifyHandler.sendMessage(finishMsg);
            }
        }).start();
    }

    /**
     * OCR识别API
     */
    private void ocrIdentify() {
        try {
            JSONObject response = Config.getYoutuInstance().commonOcr(selectedBitMap);
            ocrResultStr = ParseJsonUtil.getResultStr(response);
            if (null != selectedBitMap) {
                selectedBitMap.recycle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 执行图片裁剪
     *
     * @param sourceURI 图片对应的URI
     */
    public void photoCrop(Uri sourceURI) {
        File croppedPhotoFile = new File(fileDirectory, new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",
                Locale.getDefault()).format(new Date()));
        croppedPhotoURI = getUriByFileProvider(croppedPhotoFile);
        Crop.of(sourceURI, croppedPhotoURI).asSquare().start(this);
    }

    /**
     * 根据URI获取位图
     *
     * @param uri 需要转换的图片URI
     * @return 对应的位图
     */
    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * 获取压缩图片(配置压缩设置)
     *
     * @param uri 需要压缩图片的URI
     * @return 压缩后的BitMap格式的图片
     */
    private Bitmap getImage(Uri uri) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        //Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri), null, newOpts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri), null, newOpts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 压缩图片主方法
     *
     * @param image 需要压缩的图片
     * @return 压缩后的图片
     */
    private Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {
            //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }


}
