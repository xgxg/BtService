package dong.xg.com.myapplication;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lyx.frame.permission.Permission;
import com.lyx.frame.permission.PermissionManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // 蓝牙服务端socket
    private BluetoothServerSocket mServerSocket;
    // 蓝牙客户端socket
    private BluetoothSocket mSocket;
    private BluetoothAdapter mBluetoothAdapter;
    // 线程类
    private ServerThread mServerThread;
    private ReadThread mReadThread;

    private final int STATUS_CONNECT = 1;
    private final int STATUS_CONNECTING = 2;
    private final int STATUS_ACCEPT = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        permissionManager.addPermission(new Permission() {
            @Override
            public String getPermission() {
                return Manifest.permission.ACCESS_FINE_LOCATION;
            }

            @Override
            public void onApplyResult(boolean succeed) {
                if (succeed) {
                } else {
                    Toast.makeText(MainActivity.this, "没有定位权限无法使用蓝牙功能！", Toast.LENGTH_LONG).show();
                }
            }
        }).apply(MainActivity.this);
    }

    // 开启服务器
    private class ServerThread extends Thread {
        public void run() {
            try {
                // 创建一个蓝牙服务器 参数分别：服务器名称、UUID
                mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("dy",
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

                Message msg = new Message();
                msg.obj = "请稍候，正在等待客户端的连接...";
                msg.what = STATUS_CONNECTING;
                mHandler.sendMessage(msg);
                //服务端接受
                mSocket = mServerSocket.accept();

                msg = new Message();
                msg.obj = "客户端已经连接上！可以发送指令。";
                msg.what = STATUS_CONNECT;
                mHandler.sendMessage(msg);
                // 启动接受数据
                mReadThread = new ReadThread();
                mReadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取数据
     */
    private class ReadThread extends Thread {
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream is = null;
            try {
                is = mSocket.getInputStream();
                while (true) {
                    if ((bytes = is.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        Message msg = new Message();
                        msg.obj = s;
                        msg.what = STATUS_ACCEPT;
                        mHandler.sendMessage(msg);
                        //同样  子线程不能刷新UI
                        //tv_accept.setText(s);
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }


        /**
         * 信息处理
         */
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String info = (String) msg.obj;
                switch (msg.what) {
                    case STATUS_CONNECT:
                        //吐司出 连接状态
                        Toast.makeText(MainActivity.this, info, Toast.LENGTH_LONG).show();
                        break;
                    case STATUS_CONNECTING:
                        //吐司出 连接状态
                        Toast.makeText(MainActivity.this, info, Toast.LENGTH_LONG).show();
                        break;
                    case STATUS_ACCEPT:
                        //吐司出 连接状态
                        Toast.makeText(MainActivity.this, info, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private PermissionManager permissionManager = new PermissionManager(MainActivity.this);

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onPermissionsResult(requestCode, permissions, grantResults);
    }
}
