package io.trigger.forge.android.modules.share;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import io.trigger.forge.android.core.ForgeActivity;
import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeIntentResultHandler;
import io.trigger.forge.android.core.ForgeLog;
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
        if (subject != null && subject.length() > 0) {
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
                                Context context = ForgeApp.getActivity().getApplicationContext();
                                File temp = cacheImage(image);
                                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".ForgeFileProvider", temp);
                                ForgeLog.d("granting read permissions for uri: " + uri.toString());
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                // see https://code.google.com/p/android/issues/detail?id=76683
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                                    grantReadPermissionForUri(intent, uri);
                                }
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


    private static File cacheImage(String url) throws IOException {
        Context context = ForgeApp.getActivity().getApplicationContext();
        // TODO - Revert once Google has fixed: https://issuetracker.google.com/issues/37125252
        //final File outputDir = context.getCacheDir();
        File outputDir = ContextCompat.getExternalFilesDirs(context, null)[0];


        final String filename = url.substring(url.lastIndexOf("/") + 1, url.length());
        final java.io.File tempFile = new java.io.File(outputDir, filename);
        tempFile.createNewFile();

        URL parsedUrl = new URL(url);
        InputStream input = parsedUrl.openStream();
        try {
            OutputStream output = new FileOutputStream(tempFile);
            try {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
                    output.write(buffer, 0, bytesRead);
                }
                return tempFile;
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

    private static void grantReadPermissionForUri(final Intent intent, final Uri uri) {
        // grant read permissions for apps receiving the intent
        Context context = ForgeApp.getActivity().getApplicationContext();
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }
}

