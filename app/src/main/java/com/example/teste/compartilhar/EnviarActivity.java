package com.example.teste.compartilhar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.InetAddresses;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teste.MainActivity;
import com.example.teste.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class EnviarActivity extends AppCompatActivity {

    //private static Bitmap bitmap;
    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView read_msg_box;
    EditText writeMsg;
    TextView connectionStatus;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    Bitmap bitmap, testeBitmap;
    byte[] testeByte;
    ImageView imageView;
    String btm;

    byte[] imagembyte;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviarinterface);
        //this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

        Intent intent = getIntent();
        bitmap = intent.getParcelableExtra("BitmapImage");

        ImageView imageView = findViewById( R.id.verImagem );
        imageView.setImageBitmap(bitmap);


        initialWork();
        exqListener();

        Button btHome = findViewById(R.id.home);
        btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentHome = new Intent( EnviarActivity.this, MainActivity.class );
                startActivity( intentHome );
            }
        });
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what) {
                case MESSAGE_READ:

                    byte[] readBuff = (byte[]) msg.obj;

                    Log.v("img,", ""+readBuff.length);

                    //String tempMsg = new String(readBuff, 0, msg.arg1);
                    //read_msg_box.setText( tempMsg );
                    ImageView imageViewReceber = findViewById(R.id.verImagemColega);
                    imageViewReceber.setImageBitmap(decodeBase64(readBuff));
                    break;
            }
            return true;
        }
    });

    private void exqListener() {
        /* btnOnOff.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View view) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("ON");
                } else {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("OFF");
                }

        });}*/

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(EnviarActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(EnviarActivity.this, "Bucando...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(EnviarActivity.this, "Falha na busca", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                if (ActivityCompat.checkSelfPermission(EnviarActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(), "Not Connected " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } );
        btnSend.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String msg = writeMsg.getText().toString();
                //String msg;
                imagembyte = encodeTobase64( bitmap);
                sendReceive.write( imagembyte );


            }
        } );
    }

    private void initialWork() {
        btnOnOff = (Button) findViewById( R.id.onOff );
        btnDiscover = (Button) findViewById( R.id.discover );
        btnSend = (Button) findViewById( R.id.sendButton );
        listView = (ListView) findViewById( R.id.peerListView );
        read_msg_box = (TextView) findViewById( R.id.readMsg );
        writeMsg = (EditText) findViewById( R.id.writeMsg );
        connectionStatus = (TextView) findViewById( R.id.connectionStatus );
        imageView = findViewById( R.id.verImagem );

        wifiManager = (WifiManager) getApplicationContext().getSystemService( Context.WIFI_SERVICE );

        mManager = (WifiP2pManager) getSystemService( Context.WIFI_P2P_SERVICE );
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction( WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION );
        mIntentFilter.addAction( WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION );
        mIntentFilter.addAction( WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION );
        mIntentFilter.addAction( WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION );

        //Teste de codificação e decodificação do bitmap antes de enviar
        //imagembyte = encodeTobase64(bitmap);
        //imageView.setImageBitmap(decodeBase64(imagembyte));


    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals( peers )){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter( adapter );
            }

            if (peers.size() == 0){
                Toast.makeText( EnviarActivity.this, "Nenhum aparelho localozado", Toast.LENGTH_SHORT ).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                connectionStatus.setText( "Host" );
                Toast.makeText( getApplicationContext(), "Host", Toast.LENGTH_SHORT ).show();
                serverClass = new ServerClass();
                serverClass.start();
            } else if (wifiP2pInfo.groupFormed){
                connectionStatus.setText( "Client" );
                Toast.makeText( getApplicationContext(), "Client", Toast.LENGTH_SHORT ).show();
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver( mReceiver, mIntentFilter );
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver( mReceiver );
    }

    private static class BitmapUtils {
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive( socket );
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();
            }

        }

        @Override
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;

            while (socket !=  null){

                try {
                    inputStream.reset();
                    bytes = inputStream.read(buffer);
                    if (bytes > 0){
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes){
            try {
                outputStream.write( bytes );
            } catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;
        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket(  );
        }
        @Override
        public void run() {
            try {
                socket.connect( new InetSocketAddress( hostAdd, 8888 ), 500 );
                sendReceive =  new SendReceive( socket );
                sendReceive.start();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //bitmap para string
    public static byte[] encodeTobase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    //String para bitmap
    public Bitmap decodeBase64(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
