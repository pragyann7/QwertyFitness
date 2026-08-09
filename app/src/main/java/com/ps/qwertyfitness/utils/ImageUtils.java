package com.ps.qwertyfitness.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class ImageUtils {

    public static String saveImageToInternalStorage(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;

            String fileName = "progress_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteImage(String path) {
        if (path == null) return false;
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static void loadThumbnail(android.widget.ImageView imageView, String path) {
        loadImage(imageView, path, 300, 300);
    }

    public static void loadFullPhoto(android.widget.ImageView imageView, String path) {
        // Use a reasonable max size for full screen to avoid OOM
        loadImage(imageView, path, 1080, 1920);
    }

    private static void loadImage(android.widget.ImageView imageView, String path, int w, int h) {
        if (path == null) {
            imageView.setImageBitmap(null);
            return;
        }

        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            new Thread(() -> {
                Bitmap bitmap = decodeSampledBitmapFromFile(path, w, h);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    imageView.setImageBitmap(bitmap);
                });
            }).start();
        });
    }

    private static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}