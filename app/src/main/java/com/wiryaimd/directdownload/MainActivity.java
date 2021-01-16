package com.wiryaimd.directdownload;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.rey.material.widget.Button;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.ProgressView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnsearch;
    private EditText edtsearch;
    private CheckBox mp3, mp4, mkv, apk, jpg, png, zip;
    private TextView resultfound, loaddesk;
    private ProgressView loading;
    private ImageView myimg;

    public RecyclerView recyclerView;

    public ArrayList<String> arrtype;

    private String encode = "UTF-8";
    private String notlink1 = "www.google.com/search";
    private String notlink2 = "webcache.googleusercontent.com/";
    private String notlink3 = "translate.google.com/translate";

    private RewardedVideoAd rewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        recyclerView = findViewById(R.id.main_recyclerview);

        btnsearch = findViewById(R.id.main_btnsearch);
        edtsearch = findViewById(R.id.main_edtsearch);
        resultfound = findViewById(R.id.main_resultfound);
        loading = findViewById(R.id.main_loading);
        loaddesk = findViewById(R.id.main_loadingdesk);
        myimg = findViewById(R.id.main_myimg);

        mp3 = findViewById(R.id.main_mp3);
        mp4 = findViewById(R.id.main_mp4);
        mkv = findViewById(R.id.main_mkv);
        apk = findViewById(R.id.main_apk);
        jpg = findViewById(R.id.main_jpg);
        png = findViewById(R.id.main_png);
        zip = findViewById(R.id.main_zip);

        loading.setVisibility(View.GONE);

        btnsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myimg.setVisibility(View.GONE);
                String strsearch = edtsearch.getText().toString();

                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(new LinkAdapter(MainActivity.this, getSupportFragmentManager(), new ArrayList<String>(), new ArrayList<String>(), getLayoutInflater()));
                if (!strsearch.trim().isEmpty()) {
                    loading.setVisibility(View.VISIBLE);
                    arrtype = checktype();

                    StringBuilder fulltype = new StringBuilder();
                    if (arrtype.size() != 0) {
                        for (int i = 0; i < arrtype.size(); i++) {
                            String type = arrtype.get(i);
                            fulltype.append(type);
                            if (arrtype.size() - i != 1) {
                                fulltype.append("|");
                            }
                        }
                    } else {
                        fulltype.append("mp3|mp4|mkv|apk|jpg|png|zip");
                    }

                    loaddesk.setText("Searching for as many results.. please wait");
                    final String mydork = String.format("%s -inurl:(htm|html|php|pls|txt) intitle:index.of “last modified” (%s)", strsearch, fulltype.toString());
                    new MyLoadLink().execute(mydork);

                } else {
                    Toast.makeText(MainActivity.this, "Your input text is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public ArrayList<String> checktype(){
        ArrayList<String> arrtype = new ArrayList<>();
        if (mp3.isChecked()){
            arrtype.add("mp3");
        }
        if (mp4.isChecked()){
            arrtype.add("mp4");
        }
        if (mkv.isChecked()){
            arrtype.add("mkv");
        }
        if (apk.isChecked()){
            arrtype.add("apk");
        }
        if (jpg.isChecked()){
            arrtype.add("jpg");
        }
        if (png.isChecked()){
            arrtype.add("png");
        }
        if (zip.isChecked()){
            arrtype.add("zip");
        }

        return arrtype;
    }

    private class MyLoadLink extends AsyncTask<String, Void, LinkAdapter>{

        @Override
        protected LinkAdapter doInBackground(String... strings) {
            LinkAdapter adapter = null;
            System.out.println("searching...");
            try {
                Document doc = Jsoup.connect("https://www.google.com/search?q=" + URLEncoder.encode(strings[0], encode)).get();
                Elements elements = doc.select("div.r a");

                if (!elements.isEmpty()) {
                    ArrayList<String> listlink = new ArrayList<>();
                    for (Element link : elements) {
                        String mylink = link.attr("abs:href");
                        if (!mylink.contains(notlink1) && !mylink.contains(notlink2)) {
                            if (!mylink.contains(notlink3)) {
                                listlink.add(mylink);
                            }
                        }
                    }

                    ArrayList<String> endlinklist = new ArrayList<>();
                    ArrayList<String> titlelist = new ArrayList<>();
                    mainloop: for (int i = 0; i < listlink.size(); i++){
                        Elements endlink;
                        try {
                            Document docdownload = Jsoup.connect(listlink.get(i)).timeout(6000).get();
                            endlink = docdownload.select("a");
                        }catch (Exception e){
                            continue;
                        }
                        if (!endlink.isEmpty()) {

                            for (int j = 0; j < endlink.size(); j++){
                                Element element = endlink.get(j);
                                String link = element.attr("abs:href");
                                if (endlinklist.size() > 100) {
                                    System.out.println("break 100");
                                    break mainloop;
                                }else{
                                    if (link.length() > 3) {
                                        boolean cektype = false;
                                        cektypeloop: for (int k = 0; k < arrtype.size(); k++){
                                            cektype = link.substring(link.length() - 3, link.length()).equalsIgnoreCase(arrtype.get(k));
                                            if (cektype){
                                                break cektypeloop;
                                            }
                                        }

                                        if (cektype) {
                                            endlinklist.add(element.attr("abs:href"));
                                            titlelist.add(element.text());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    adapter = new LinkAdapter(MainActivity.this, getSupportFragmentManager(), endlinklist, titlelist, getLayoutInflater());

                } else {
                    System.out.println("not found");
                }

                System.out.println("done");

            } catch (IOException e) {
                System.out.println(e);
            }
            return adapter;
        }

        @Override
        protected void onPostExecute(LinkAdapter linkAdapter) {
            if (linkAdapter != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.setAdapter(linkAdapter);
                String resultcount = linkAdapter.getsize() - 1  + " Result found";
                resultfound.setText(resultcount);

                myloadad();
            }else{
                Toast.makeText(MainActivity.this, "Failed to search result, please try again", Toast.LENGTH_SHORT).show();
                myimg.setVisibility(View.VISIBLE);
            }
            loaddesk.setText(" ");
            loading.setVisibility(View.GONE);
        }
    }

    public void myloadad(){
        MobileAds.initialize(MainActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(MainActivity.this);
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if (rewardedVideoAd.isLoaded()){
                    rewardedVideoAd.show();
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                System.out.println("reward: " + rewardItem.getType() + " amount: " + rewardItem.getAmount());
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Toast.makeText(MainActivity.this, "failed to load ad", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardedVideoCompleted() {

            }
        });
        loadreward();
    }

    public void loadreward(){
        rewardedVideoAd.loadAd(getString(R.string.myad1), new AdRequest.Builder().build());
    }
}
