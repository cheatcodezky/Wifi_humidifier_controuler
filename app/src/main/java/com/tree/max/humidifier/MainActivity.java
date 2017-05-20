package com.tree.max.humidifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.idtk.smallchart.chart.LineChart;
import com.idtk.smallchart.data.LineData;
import com.idtk.smallchart.interfaces.iData.ILineData;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final int HUMIDIFIER = 1;//用于handler判断

    //绘图曲线图
    LineChart lineChart;
    LineData lineData;
    ArrayList<ILineData> dataList;
    ArrayList<PointF> linePointList ;
    PointF pointF;
    //坐标
    int[] x = new int[12];
    int[] y = new int[12];

    //本地广播 控制UI
    LocalReceiver localReceiver;

    //监听WIFI状态
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;

    IntentFilter mIntentFilter;

    //布局控件
    Button connectButton;
    Button autoButton;
    Button handButton;
    TextView current_humidifier;
    TextView aim_humidifier;
    EditText input_humidifier ;
    ToggleButton stateButton ;

    //通讯线程
    AcceptThread acceptThread;

    //接受到的信息
    static  String number = "" ;

    //判断是否已连接
    private boolean isConnecting= false;

    //显示数据 ,更新曲线图
    final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case HUMIDIFIER:
                    current_humidifier.setText("当前湿度:"+number);
                    lineChart.invalidate();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        acceptThread = new AcceptThread(MainActivity.this);

        init();//初始化


        MainOnClickListener mainOnClickListener = new MainOnClickListener();
        connectButton.setOnClickListener(mainOnClickListener);
        autoButton.setOnClickListener(mainOnClickListener);
        handButton.setOnClickListener(mainOnClickListener);



        for (int i = 1;i<=9;i++)
        {
            y[i-1] = y[i];
        }
        y[0] = 100;
        y[1] = 0;
        linePointList.clear();
        for (int i = 0 ; i<=10;i++)
        {
            linePointList.add(new PointF(x[i],y[i]));
        }

        lineData.setValue(linePointList);
        dataList.clear();
        dataList.add(lineData);
        lineData.setColor(Color.CYAN);
        lineData.setPaintWidth(1);
        lineData.setTextSize(4);

        lineChart.isAnimated = false;
        lineChart.setDataList(dataList);


    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);//注册广播

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("IVE_LOCAL_BROAD_RECECHANGED");//本地UI线程广播
        localReceiver= new LocalReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
    }
    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localReceiver);
    }

    public void init()
    {
        lineChart = (LineChart)findViewById(R.id.times_data_view);
        lineData = new LineData();
        linePointList = new ArrayList<>();
        dataList = new ArrayList<>();

        for (int j= 0; j<12;j++)
        {
            x[j] = (j-2);
        }
        x[1] = 0;
        x[0] = 0;
        x[2] = 0;
        for (int j =2;j<12;j++)
        {
            x[j] +=0.2*(j-1);
        }
        for (int j =3 ;j<12;j++)
        {
            x[j] += (j-2)*0.1;
        }


        connectButton = (Button)findViewById(R.id.connectButton);
        autoButton = (Button)findViewById(R.id.autoButton);
        handButton = (Button)findViewById(R.id.handButton);

        //监听wifi状态
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        aim_humidifier = (TextView)findViewById(R.id.aim_humidifier);
        current_humidifier = (TextView)findViewById(R.id.current_humidifier);

        stateButton = (ToggleButton)findViewById(R.id.stateButton);

        input_humidifier = new EditText(this);

    }


    class MainOnClickListener implements View.OnClickListener
    {

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.connectButton:
                    if (isConnecting)
                    {
                        isConnecting = false;
                        if (acceptThread.isAlive())
                            acceptThread.interrupt();
                        Log.e("Main","connectButton is touched");
                        connectButton.setText("连接仪器");
                    }
                    else
                    {
                        isConnecting = true;
                        connectButton.setText("断开连接");
                        if (acceptThread.isAlive())
                            acceptThread.interrupt();
                        acceptThread.start();
                    }
                    break;
                case R.id.autoButton:
                    aim_humidifier.setText("设置湿度:60");
                    if (acceptThread.isAlive())
                        acceptThread.interrupt();
                    acceptThread = new AcceptThread(60);
                    acceptThread.start();
                    break;
                case R.id.handButton:
                    new AlertDialog.Builder(MainActivity.this).setTitle("设置湿度")
                            .setView(input_humidifier = new EditText(MainActivity.this)).setPositiveButton("确定",onClickListener)
                            .setNegativeButton("取消",null)
                            .show();
                    break;
                case R.id.stateButton:
                    if (stateButton.isChecked())
                    {
                        if (acceptThread.isAlive())
                        {
                            acceptThread.interrupt();
                        }
                        acceptThread = new AcceptThread("@CH1",1);
                        acceptThread.start();
                    }
                    else
                    {
                        if (acceptThread.isAlive())
                        {
                            acceptThread.interrupt();
                        }
                        acceptThread = new AcceptThread("@CH0",1);
                        acceptThread.start();
                    }
                    break;

            }
        }
    }

    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            String input =input_humidifier.getText().toString();
            aim_humidifier.setText("设置湿度:"+input);
            if (acceptThread.isAlive())
                acceptThread.interrupt();
            acceptThread = new AcceptThread(Integer.valueOf(input));
            acceptThread.start();
        }
    };
    private  class  LocalReceiver extends BroadcastReceiver{
        Bundle bundle;


        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction())
            {
                case "IVE_LOCAL_BROAD_RECECHANGED":
                    Message msg = new Message();
                    bundle = intent.getExtras();
                    number = bundle.getString("humidifier");
                    msg.what = HUMIDIFIER;

                    for (int i = 3;i<=10;i++)
                    {
                        y[i-1] = y[i];
                    }

                    y[10] = Integer.valueOf(number);
                    linePointList.clear();

                    for (int i = 0 ; i<11;i++)
                    {
                        pointF = new PointF(x[i],y[i]);
                        linePointList.add(pointF);
                    }

                    handler.sendMessage(msg);
                    break;


            }
        }
    }

}
