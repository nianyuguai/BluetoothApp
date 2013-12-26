package nianyu.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppClient extends Activity {
	// Debugging
    private static final String TAG = "AppClient";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_TOAST = 3;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    
    //variable
    private BluetoothAdapter mBluetoothAdapter = null;
    //定义实现连接的外部设备
    private BluetoothService mDeviceService = null;
    private BluetoothReceiver receiver;
    private ArrayAdapter<String> deviceList;
    private String []deviceAddress = new String[100];
    private int mCnt = 0;
    // Name of the connected device
    private String mConnectedDeviceName = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.e(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.main);
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        deviceList = new ArrayAdapter<String>(this,R.layout.devices_name);
        ListView deviceListView = (ListView)findViewById(R.id.bt_devices);
        deviceListView.setAdapter(deviceList);
        deviceListView.setOnItemClickListener(mDeviceClickListener);
       
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
   
        receiver = new BluetoothReceiver();
        registerReceiver(receiver,filter);
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //初始化一个外部设备的连接服务,与进程绑定
        mDeviceService = new BluetoothService(this, mHandler);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(D) Log.e(TAG, "+++ ON START +++");
		
		if(!mBluetoothAdapter.isEnabled()){
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
		}
		doDiscovery();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(D) Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(D) Log.e(TAG, "-- ON STOP --");
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(D) Log.e(TAG, "-- ON DESTROY --");
		if(mDeviceService != null) mDeviceService.stop();
		unregisterReceiver(receiver);	
	}
	
	   /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
        	mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }
	
	private class BluetoothReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Bundle mBundle = intent.getExtras();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				deviceList.add("设备名称(Name):"+device.getName() + "\n"+
							   "地址(Mac Address):"+device.getAddress() + "\n"+
							   "信号(Rssi):"+String.valueOf(mBundle.get("android.bluetooth.device.extra.RSSI"))+ "dB\n"+
							   "种类(Class):"+String.valueOf(mBundle.get("android.bluetooth.device.extra.CLASS"))+"\n"+
							   "服务(Uuid):"+ device.getUuids()+"\n" +
							   "连接(Connect):"+device.getBondState());
				deviceAddress[mCnt++] =  device.getAddress();
			}else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch(device.getBondState()){
				case BluetoothDevice.BOND_BONDING:
					if (D) Log.d(TAG, "正在配对...");
					break;
				case BluetoothDevice.BOND_BONDED:
					if (D) Log.d(TAG, "完成配对!!!");
					break;
				case BluetoothDevice.BOND_NONE:
					if (D) Log.d(TAG, "未配对");
					break;
				default:
						break;
				}
			}else if(action.equals("android.bluetooth.device.action.PAIRING_REQUEST")){
				//自动匹配广播
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				try {
					BluetoothMethod.setPin(device.getClass(), device,"0000");
					BluetoothMethod.createBond(device.getClass(), device);
					//BluetoothMethod.cancelBondProcess(device.getClass(), device);
					BluetoothMethod.cancelPairingUserInput(device.getClass(), device);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	private OnItemClickListener mDeviceClickListener = new  OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long arg3) {
			// TODO Auto-generated method stub
			//连接前要取消搜索
			mBluetoothAdapter.cancelDiscovery();
			
			if(D) Log.d(TAG, "Mac Address"+deviceAddress[position]);
			//获取对应的外部设备
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress[position]);
			
			//配对
			
			if(device.getBondState()!= BluetoothDevice.BOND_BONDED){
				try {
					BluetoothMethod.setPin(device.getClass(), device,"0000");
					BluetoothMethod.createBond(device.getClass(), device);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				try {
					BluetoothMethod.createBond(device.getClass(), device);
					BluetoothMethod.setPin(device.getClass(), device,"0000");
					BluetoothMethod.createBond(device.getClass(), device);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			mDeviceService.connect(device);
		}
		
	};
	

	
	// The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            
                            //设置连接标志
                            //mConnectFlag = true;
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            
                            break;
                        case BluetoothService.STATE_NONE:
                          
                          //设置连接标志
                          // mConnectFlag = false;
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
	
	private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_client, menu);
		return true;
	}

}
