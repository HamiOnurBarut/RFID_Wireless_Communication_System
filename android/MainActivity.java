package com.example.tahir.wishop;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;//unique id for each notification that we build
    WebView webview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview = (WebView) findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);//when you click notification it removes notification


        client();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        con2ser();
    }

    public void client(){
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        DatagramSocket socket = new DatagramSocket(9000);
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);
                        String sentence = new String(receivePacket.getData());
                        socket.close();
                        System.out.println("RECEIVED: " + sentence);
                        notification();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void notification(){

        //Build notification
        notification.setSmallIcon(R.drawable.logo);
        notification.setTicker("WiShop");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("WiShop");
        notification.setContentText("Welcome to WiShop");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingintent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());

    }

    public void con2ser(){

        String serverIP = "http://139.179.32.22:7676/";
        webview.loadUrl(serverIP);

        final ProgressDialog progress = ProgressDialog.show(this, "Server", "Loading....", true);
        progress.show();
        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Toast.makeText(getApplicationContext(), "Sayfa yüklendi", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), "Bir hata oluştu", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });
    }






}
