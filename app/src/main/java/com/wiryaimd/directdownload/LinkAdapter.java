package com.wiryaimd.directdownload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.MyHolder> {

    private Context context;
    private ArrayList<String> link, title;

    private LayoutInflater inflater;
    private FragmentManager fm;

    public LinkAdapter(Context context, FragmentManager fm, ArrayList<String> link, ArrayList<String> title, LayoutInflater inflater){
        this.context = context;
        this.fm = fm;
        this.link = link;
        this.title = title;
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public LinkAdapter.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkAdapter.MyHolder holder, int position) {
        String mytitle = title.get(position);
        String mylink = link.get(position);

        holder.title.setText(mytitle);
        holder.link.setText(mylink);

        String filetype = "." + mylink.substring(mylink.length() - 3, mylink.length());
        holder.type.setText(filetype);
        holder.clickdialog(mytitle, mylink, filetype);

    }

    @Override
    public int getItemCount() {
        return link.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        public TextView title, link, type;
        public ImageView imgdownload;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.result_title);
            link = itemView.findViewById(R.id.result_link);
            type = itemView.findViewById(R.id.result_type);
            imgdownload = itemView.findViewById(R.id.result_download);
        }

        public void clickdialog(final String title, final String link, final String filetype){
            imgdownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadDialog downloadDialog = new DownloadDialog();
                    downloadDialog.showDialog(context, fm, inflater, title, link, filetype);
                }
            });
        }

    }

    public int getsize(){
        return link.size();
    }
}
