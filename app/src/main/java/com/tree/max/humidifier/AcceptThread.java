package com.tree.max.humidifier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by max on 17-5-20.
 */

public class AcceptThread extends Thread {
    private PrintStream output;
    private BufferedInputStream bufferedInputStream;
    private Socket socket;

    Bundle bundle;
    LocalBroadcastManager localBroadcastManager;
    Intent intent = new Intent("IVE_LOCAL_BROAD_RECECHANGED");
    String patter = "(\\d+)";
    Pattern r = Pattern.compile(patter);
    Matcher matcher;
    int relay = 0;
    String relay_state = "";
    int aim_humidifier = 0;
    int humidifier_setting = 0;


    public AcceptThread(Context context) {

        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        bundle = new Bundle();
    }

    public AcceptThread(String s, int i) {
        relay = i;
        relay_state =s ;
    }

    public AcceptThread(int i) {
        aim_humidifier = i;
        humidifier_setting = 1;

    }

    @Override
    public void run() {
        initClientSocket();
        String receive;


        while (true)
        {
            byte[] data = receiveData();
            if (data.length>1)
            {
                receive = new String(data);
                matcher =r.matcher(receive);
                if (matcher.find())
                    receive = matcher.group(1);
                else
                    receive = "-1";
                if (receive.startsWith("0"))
                    receive = receive.substring(1);

                bundle = new Bundle();
                bundle.putString("humidifier",receive);
                intent.putExtras(bundle);
                localBroadcastManager.sendBroadcast(intent);
            }
        }


    }
    public void initClientSocket(){
        try{
            socket = new Socket("192.168.4.1",5000);
            output = new PrintStream(socket.getOutputStream(),true,"gbk");
        } catch (UnknownHostException e) {
            Log.e("init","host wrong");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("init","IO");
            e.printStackTrace();
        }
        if (relay==1)
        {
            sendMessage();
            relay = 0;
        }
        if (humidifier_setting == 1)
        {
            output.print(aim_humidifier);
            humidifier_setting = 0;
        }
    }
    private void sendMessage()
    {
        output.print(relay_state);
    }

    public byte[] receiveData(){
        if (socket == null || socket.isClosed()){
            try{
                socket = new Socket("192.168.4.1",5000);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] buffer =new byte[254];
        int length;
        if (socket.isConnected())
        {
            try{
                bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                length = bufferedInputStream.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            buffer = new byte[1];
        }
        return buffer;
    }
}
