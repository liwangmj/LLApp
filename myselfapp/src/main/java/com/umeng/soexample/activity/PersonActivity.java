package com.umeng.soexample.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;

import com.android.core.base.AbsBaseActivity;
import com.bumptech.glide.Glide;
import com.umeng.soexample.R;
import com.umeng.soexample.custom.AppSelectPicsDialog;
import com.heaton.liulei.utils.custom.RoundImageView;
import com.heaton.liulei.utils.utils.SPUtils;
import com.heaton.liulei.utils.utils.ToastUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by liulei on 2016/5/31.
 */
public class PersonActivity extends AbsBaseActivity {

    @Bind(R.id.aviter)
    RoundImageView aviter;

    public static final String PHOTO_PATH = Environment.getExternalStorageDirectory() + "/heaton_";
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int CROP_REQUEST_CODE = 3;

    private Uri imageUri;
    private String filename; //图片名称
    private Bitmap bm;
    private Uri uri;
    private String localHeader;
//    private String path;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_person;
    }

    @Override
    protected void onInitView() {
        setTitle("个人中心");
        toolbar.setNavigationIcon(R.mipmap.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //加载本地个人头像
        if(!getHeader().equals("")){
            Glide.with(this).load(getHeader()).into(aviter);
        }
    }

    @OnClick(R.id.aviter)
    void aviter(){
        startActivity(BigPhotoActivity.class);
    }

    @OnClick(R.id.layout_aviter)
    void layout_aviter(){
        AppSelectPicsDialog dialog = new AppSelectPicsDialog(this, R.style.translate_dialog);
        dialog.show();
        dialog.setDialogListner(new AppSelectPicsDialog.dialogListenner() {
            @Override
            public void setOnCameraLis(Dialog d, View v) {
                d.dismiss();
                takePicture();
            }

            @Override
            public void setOnGalleryLis(Dialog d, View v) {
                d.dismiss();
                selectPhoto();
            }

            @Override
            public void setOnCancelLis(Dialog d, View v) {
                d.dismiss();
            }
        });
    }

    private String getHeader(){
        String path = SPUtils.get(getApplication(),"header","");
        return path;
    }

    /**
     * 调用系统相册
     */
    private void selectPhoto() {
        File outputImage = getFile();
        //将File对象转换为Uri并启动照相程序
        imageUri = Uri.fromFile(outputImage);
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image");
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    /**
     * 调用系统拍照功能
     */
    private void takePicture() {
        File outputImage = getFile();
        //将File对象转换为Uri并启动照相程序
        imageUri = Uri.fromFile(outputImage);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); //照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //指定图片输出地址
        startActivityForResult(intent, CAMERA_REQUEST_CODE); //启动照相
    }

    @NonNull
    private File getFile() {
        //图片名称 时间命名
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        filename = format.format(date);
        //创建File对象用于存储拍照的图片 SD卡根目录
        //File outputImage = new File(Environment.getExternalStorageDirectory(),"test.jpg");
        //存储至DCIM文件夹
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File outputImage = new File(path, filename + ".jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
            localHeader = outputImage.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputImage;
    }

    /**
     * 将通过相机、图库得到的图片进行裁剪
     *
     * @param uri
     */
    private void startImageZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 480);
        intent.putExtra("outputY", 480);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE://相机
                startImageZoom(imageUri);
                //广播刷新相册
                Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intentBc.setData(imageUri);
                this.sendBroadcast(intentBc);
                break;
            case GALLERY_REQUEST_CODE://图库
                if (data == null || data.getData() == null) {
                    return;
                }
                uri = data.getData();
                startImageZoom(uri);
                break;
            case CROP_REQUEST_CODE:
                if (data == null) {
                    return;
                }
                //图片解析成Bitmap对象
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    if (bitmap == null) {
                        ToastUtil.showToast("请从相册进行选择");
                        return;
                    }
//                    aviter.setImageBitmap(bitmap);
                    //保存头像路径至内存
                    if(localHeader!=null)
                    SPUtils.put(getApplication(),"header",localHeader);

                    Glide.with(this).load(getHeader()).into(aviter);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


}