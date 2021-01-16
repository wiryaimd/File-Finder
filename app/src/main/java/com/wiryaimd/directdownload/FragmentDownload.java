package com.wiryaimd.directdownload;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;

import java.io.File;
import java.util.Locale;

public class FragmentDownload extends Fragment {

    private static final int PERMISSION_STORAGE_CODE = 1000;
    private int downloadid;
    private TextView stat, filename;
    private Button btnpause, btncancel;
    private ProgressBar loading;

    private String title, link, type;
    private Context context;

    public FragmentDownload(Context context, String title, String link, String type){
        this.title = title;
        this.link = link;
        this.type = type;
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_downloadstat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        stat = view.findViewById(R.id.downloadstat_size);
        btnpause = view.findViewById(R.id.downloadstat_pause);
        btncancel = view.findViewById(R.id.downloadstat_cancel);
        loading = view.findViewById(R.id.downloadstat_progress);
        filename = view.findViewById(R.id.downloadstat_filename);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] perm = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(perm, PERMISSION_STORAGE_CODE);
            }else{
                startdownload();
            }
        }else{
            startdownload();
        }

        btnpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnpause.getText().toString().equalsIgnoreCase("pause")) {
                    PRDownloader.pause(downloadid);
                } else {
                    PRDownloader.resume(downloadid);
                }
            }
        });

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PRDownloader.cancel(downloadid);
            }
        });

        Toast.makeText(getContext(), "Start Downloading..", Toast.LENGTH_SHORT).show();

    }

    public void startdownload(){
        final String mytitle = title.replaceAll("\\s+", "_");
        final String dirpath = getRootDirPath(context);
        final String fulltitle = "_DD_" + mytitle + type;
        filename.setText(title);

        downloadid = PRDownloader.download(link, dirpath, fulltitle)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        loading.setIndeterminate(false);
                        btnpause.setText("Pause");
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        btnpause.setText("Resume");
                        Toast.makeText(getContext(), "Paused", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.main_frame)).commit();
                        Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        long percent = progress.currentBytes * 100 / progress.totalBytes;
                        loading.setProgress((int) percent);
                        stat.setText(getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        DownloadDialog dialog = new DownloadDialog();
                        String msg = "You can open the file in " + dirpath;
                        dialog.showDialogDone(getContext(), getLayoutInflater(), fulltitle, msg);
                        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.main_frame)).commit();
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Error error) {
                        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.main_frame)).commit();
                        Toast.makeText(getContext(), "Error while downloading.. please try again", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public static String getRootDirPath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File file = ContextCompat.getExternalFilesDirs(context.getApplicationContext(),
                    null)[0];
            String dirpath = file.getAbsolutePath();
            for (int i = 0; i < dirpath.length(); i++){
                if (dirpath.toLowerCase().substring(i, i + 7).contains("android")){
                    dirpath = dirpath.substring(0, i) + "Download";
                    System.out.println("root dirpath: " + dirpath);
                    break;
                }
            }
            return dirpath;
        } else {
            String dirpath = context.getApplicationContext().getFilesDir().getAbsolutePath();
            for (int i = 0; i < dirpath.length(); i++){
                if (dirpath.toLowerCase().substring(i, i + 7).contains("android")){
                    dirpath = dirpath.substring(0, i) + "Download";
                    System.out.println("root dirpath: " + dirpath);
                    break;
                }
            }
            return dirpath;
        }
    }

    public static String getProgressDisplayLine(long currentBytes, long totalBytes) {
        return getBytesToMBString(currentBytes) + "/" + getBytesToMBString(totalBytes);
    }

    private static String getBytesToMBString(long bytes){
        return String.format(Locale.ENGLISH, "%.2fMb", bytes / (1024.00 * 1024.00));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startdownload();
            }else{
                Toast.makeText(context, "Permission denied, you have to allow the permission storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
