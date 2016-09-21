package io.trigger.forge.android.modules.share;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import io.trigger.forge.android.core.ForgeActivity;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeIntentResultHandler;
import io.trigger.forge.android.core.ForgeParam;
import io.trigger.forge.android.core.ForgeTask;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class API {

    public static void item(final ForgeTask task,
                            @ForgeParam("content") final JsonObject content,
                            @ForgeParam("exclusions") final JsonArray exclusions) {

        // parse parameters
        String text = (content.has("text") ? content.get("text").getAsString() : null);
        String url = (content.has("url") ? content.get("url").getAsString() : null);
        if (url != null) {
            text = (text == null || text.length() == 0)
                    ? url
                    : text + " " + url;
        }
        final String image = content.has("image") ? content.get("image").getAsString() : null;
        String subject = content.has("subject") ? content.get("subject").getAsString() : null;

        // create share intent
        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        if (text != null && text.length() > 0) {
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }
        if (subject != null && subject.length() >0) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (image != null && image.length() > 0) {
            intent.setType("image/*");
        }

        // create intent result handler
        final ForgeIntentResultHandler handler = new ForgeIntentResultHandler() {
            @Override
            public void result(int requestCode, int resultCode, Intent data) {
                if (resultCode == RESULT_OK || resultCode == RESULT_CANCELED) {
                    task.success();
                } else {
                    task.error("Unknown error sharing content", "UNEXPECTED_FAILURE", null);
                }
            }
        };

        // start intent
        if (image == null || image.length() == 0) {
            intent.setType("text/plain");
            ForgeApp.intentWithHandler(intent, handler);

        } else {
            ForgeApp.getActivity().requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, new ForgeActivity.EventAccessBlock() {
                @Override
                public void run(boolean granted) {
                    if (!granted) {
                        task.error("Permission denied. User didn't grant access to storage.", "EXPECTED_FAILURE", null);
                        return;
                    }
                    task.performAsync(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String path = cacheImage(image);
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
                                ForgeApp.intentWithHandler(intent, handler);
                            } catch (Exception e) {
                                task.error(e.getLocalizedMessage(), "UNEXPECTED_FAILURE", null);
                            }
                        }
                    });
                }
            });
        }
    }


    private static String cacheImage(String image) throws IOException {
        String path = Environment.getExternalStorageDirectory().getPath() +
                "/" +
                image.substring(image.lastIndexOf("/") + 1, image.length());

        URL url = new URL(image);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(path);
        byte[] b = new byte[2048];
        int length;
        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }
        is.close();
        os.close();

        return path;
    }
}

