package com.example.avoscloud_demo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import ar.com.daidalos.afiledialog.FileChooserDialog;
import com.avos.avoscloud.*;
import junit.framework.Assert;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: zhuzeng
 * Date: 12/16/13
 * Time: 4:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileDemoActivity extends DemoBaseActivity {

    private String fileUrl = null;
    private String objectId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.demo_base);
        setupAdapter();
    }

    private void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private byte[] readFile(File file) {
        RandomAccessFile rf = null;
        byte[] data = null;
        try {
            rf = new RandomAccessFile(file, "r");
            int length = (int)rf.length();
            if (length >= 5 * 1024 * 1024) {
                return null;
            }
            data = new byte[length];
            rf.readFully(data);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeQuietly(rf);
        }
        return data;
    }

    private void uploadFileImpl(File file, final String string) {
        byte[] data = readFile(file);
        if (data == null) {
            Toast.makeText(this, "File is too big to upload.", Toast.LENGTH_LONG).show();
            return;
        }
        final AVFile avFile = new AVFile(file.getName(), data);
        avFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                FileDemoActivity.this.showMessage(string, e, false);
                if (e == null) {
                    fileUrl = avFile.getUrl();
                    objectId = avFile.getObjectId();
                }
                setProgressBarIndeterminateVisibility(false);
            }
            }, new ProgressCallback() {
                @Override
                public void done(Integer percentDone) {
                    LogUtil.log.d("uploading: " + percentDone);
                }
        });
    }

    public void testFileUpload(final String string) throws AVException {
        FileChooserDialog dialog = new FileChooserDialog(this);
        dialog.show();
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                FileDemoActivity.this.uploadFileImpl(file, string);
            }
            public void onFileSelected(Dialog source, File folder, String name) {
            }
        });
    }

    // create an object and query it.
    public void testFileDownload(final String string) throws AVException {
        if (isBlankString(fileUrl)) {
            showMessage("Please upload file at first.", null, false);
            return;
        }
        AVFile avFile = new AVFile("my_download_file", fileUrl);
        avFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, AVException e) {
                showMessage(string, e, false);
            }
        });
    }

    // update an object
    public void testFileDelete(final String string) throws Exception {
        if (isBlankString(objectId)) {
            showMessage("Please upload file at first.", null, false);
            return;
        }
        AVFile.withObjectIdInBackground(objectId, new GetFileCallback<AVFile>() {
            @Override
            public void done(AVFile avFile, AVException e) {
                avFile.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(AVException e) {
                        showMessage(string, e, false);
                    }
                });
            };
        });
    }
}
