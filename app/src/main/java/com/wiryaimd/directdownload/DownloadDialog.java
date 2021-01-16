package com.wiryaimd.directdownload;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;

public class DownloadDialog {

    public void showDialog(final Context context, final FragmentManager fm, final LayoutInflater inflater, final String filename, final String filelink, final String filetype){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogview = inflater.inflate(R.layout.dialog_download, null, false);
        builder.setView(dialogview);

        final Button downloadnow = dialogview.findViewById(R.id.dialog_downloadnow);
        Button copylink = dialogview.findViewById(R.id.dialog_copylink);
        TextView title = dialogview.findViewById(R.id.dialog_title);
        TextView link = dialogview.findViewById(R.id.dialog_link);

        String mylink = "Link: " + filelink;
        title.setText(filename);
        link.setText(mylink);

        final AlertDialog dialog = builder.create();
        dialog.show();

        downloadnow.setEnabled(true);
        downloadnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        dialogmessage(context, inflater, "Before downloading, you are required to allow STORAGE permission so that the download process can run properly");
                    }
                }

                FragmentDownload cek = (FragmentDownload) fm.findFragmentById(R.id.main_frame);
                if (cek != null && cek.isVisible()) {
                    Toast.makeText(context, "The previous download currently running, you have to cancel the previous download", Toast.LENGTH_SHORT).show();
                }else{
                    FragmentDownload fragmentDownload = new FragmentDownload(context, filename, filelink, filetype);
                    FragmentTransaction ft = fm.beginTransaction().replace(R.id.main_frame, fragmentDownload);
                    ft.commit();
                    downloadnow.setEnabled(false);
                    dialog.dismiss();
                }
            }
        });

        copylink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = "Copied " + filelink;
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);;
                ClipData clipData = ClipData.newPlainText("text", filelink);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showDialogDone(final Context context, LayoutInflater inflater, final String filename, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogview = inflater.inflate(R.layout.dialog_done, null, false);
        builder.setView(dialogview);

        TextView title = dialogview.findViewById(R.id.dialog_title2);
        TextView desk = dialogview.findViewById(R.id.dialog_desk);
        Button open = dialogview.findViewById(R.id.dialog_open);
        Button close = dialogview.findViewById(R.id.dialog_close);

        title.setText(filename);
        desk.setText(msg);

        final AlertDialog dialog = builder.create();
        dialog.show();

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(FragmentDownload.getRootDirPath(context) + "/" + filename);
                System.out.println("dirpath: " + Uri.fromFile(file));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "*/*");
                context.startActivity(intent);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void dialogmessage(Context context, LayoutInflater inflater, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogview = inflater.inflate(R.layout.dialog_message, null, false);
        builder.setView(dialogview);

        TextView message = dialogview.findViewById(R.id.dialog_message);
        Button btn = dialogview.findViewById(R.id.dialog_btnok);

        message.setText(msg);

        final AlertDialog dialog = builder.create();
        dialog.show();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
