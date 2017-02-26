package com.example.android.BluetoothChat;

import android.annotation.SuppressLint;
//import android.R;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.WindowManager;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

@SuppressLint("HandlerLeak")
public class BluetoothChat extends Activity {
	// Debugging
	private static final String TAG = "BluetoothGraph";
	private static final boolean D = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

//	// Layout Views
//	private ListView mConversationView;
//	private EditText mOutEditText;
//	private Button mSendButton;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothChatService mChatService = null;

	LinearLayout mainLL = null;
	LinearLayout[] subLL = null;
    GraphView[] graphView = null;
    Button button1 = null;
    private Menu menu;
    int preMode;
    
    StringTokenizer s_Token = null;
    private readThread mReadThread;
    private writeThread mWriteThread;    
    SaveData saveData = null;
    String readMessage = null;
    int k;
    int itemLen = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		
		mainLL = new LinearLayout(this);
        mainLL.setOrientation(LinearLayout.VERTICAL);
        mainLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F));
        mainLL.setBackgroundColor(Color.parseColor("#ffffff"));       

        setContentView(mainLL);

        // Configure layouts, custom views, and button
        subLL = new LinearLayout[3];
        graphView = new GraphView[6];
        for (int i = 0; i < 3; i++) {
            subLL[i] = new LinearLayout(this);
            subLL[i].setOrientation(LinearLayout.HORIZONTAL);
            subLL[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0F));
            graphView[2 * i] = new GraphView(this, "A" + Character.toString ((char) (i + 88)));
            graphView[2 * i + 1] = new GraphView(this, "G" + Character.toString ((char) (i + 88)));
            graphView[2 * i].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            graphView[2 * i + 1].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            subLL[i].addView(graphView[2 * i]);
            subLL[i].addView(graphView[2 * i + 1]);
            mainLL.addView(subLL[i]);
        }
        itemLen = graphView[0].list1.maxItem - 1;
        preMode = 0;
        
        button1 = new Button(this);
        button1.setTextSize(10);
        button1.setText("시  작");
        mainLL.addView(button1);
        
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {            	
	            MenuItem MenuItem = menu.findItem(R.id.start);
	            
	            if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
	                if (mReadThread == null) {
	                    button1.setText("일시정지");
	                    mReadThread = new readThread(false);
	                    mWriteThread = new writeThread(false);
	                    mReadThread.start();
	                    mWriteThread.start();
	                    MenuItem.setTitle("Graph pause");
	                } else if(button1.getText().equals("일시정지")) {
	                	button1.setText("시  작");
	                	mReadThread.pauseThread(true);
	                	mWriteThread.pauseThread(true);
	                	MenuItem.setTitle("Graph start");
	                } else {
	                	button1.setText("일시정지");
	                	mReadThread.pauseThread(false);
	                	mWriteThread.pauseThread(false);
	                	MenuItem.setTitle("Graph pause");
	                }
            	} else {
            		Toast.makeText(getApplicationContext(), "Please connect to device", Toast.LENGTH_LONG).show();
            	}
            }
        });

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
//		Argument is filename.
		saveData = new SaveData("Hello");
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}		
	}

	private void setupChat() {
		Log.e(TAG, "setupChat()");
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);

		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");		
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if(mReadThread != null)
			mReadThread.shutdown();		
		if(mWriteThread != null)
			mWriteThread.shutdown();
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							mConnectedDeviceName));
					break;
				case BluetoothChatService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
				/* 메세지를 전송하는 상태*/			
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				readMessage = new String(readBuf);
				
//				Accept bluetooth data.
				if(mReadThread != null)
					mReadThread.emptyThread(false);
				
//				Store .txt file.
				if(mWriteThread != null)
					mWriteThread.emptyThread(false);
				
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	class readThread extends Thread {
        private boolean isEmpty = true;
        private boolean isPause;
        public boolean running = true;
        int[][] items = null;
        List<Integer>[] max = new ArrayList[6];

        public readThread(boolean isPause) {
            this.isPause = isPause;
            items = new int[12][5];
            
            for (int i = 0; i < 12; i++) {
                items[i] = new int[5];
            }
            for(int i = 0; i < 6; i++) {
            	max[i] = new ArrayList<Integer>();
            }            
        }
        
        public void pauseThread(boolean isPause) {
            this.isPause = isPause;
        }
        public void emptyThread(boolean isEmpty) {
        	this.isEmpty = isEmpty;
        }
        
        public void shutdown() {
        	running = false;
			emptyThread(true);
			pauseThread(true);	
        }

        public void run() {
            try {
            	while(running) {
            		while(isPause || isEmpty);
            		isEmpty = true;            		
            		            		
            		s_Token = new StringTokenizer(readMessage, "abcdef/ ");
	
	                if (s_Token.countTokens() != 61) return;
	
	                //                      자료 수 버림.
	                s_Token.nextToken();
	                for (int i = 0; s_Token.hasMoreTokens(); i++) {
	                    items[i / 5][i % 5] = Integer.valueOf(s_Token.nextToken());
	
	                    if(max[(i / 5) % 6].size() == (graphView[0].list1.maxItem) * 2) {
	                    	max[(i / 5) % 6].remove(0);
	                    }
	                    max[(i / 5) % 6].add(Math.abs(items[i / 5][i % 5]));
	                }
	
	                graphView[0].level = Collections.max(max[0]) * 3 / 2;
	                graphView[1].level = Collections.max(max[3]) * 3 / 2;
	                graphView[2].level = Collections.max(max[1]) * 3 / 2;
	                graphView[3].level = Collections.max(max[4]) * 3 / 2;
	                graphView[4].level = Collections.max(max[2]) * 3 / 2;
	                graphView[5].level = Collections.max(max[5]) * 3 / 2;
	
	                for (k = 0; k < 5; k++) {
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {	                        	
	                            graphView[0].addPoint(items[0][k], items[6][k]);
	                            graphView[1].addPoint(items[3][k], items[9][k]);
	                            graphView[2].addPoint(items[1][k], items[7][k]);
	                            graphView[3].addPoint(items[4][k], items[10][k]);
	                            graphView[4].addPoint(items[2][k], items[8][k]);
	                            graphView[5].addPoint(items[5][k], items[11][k]);
	                        }
	                    });
	                    Thread.sleep(195);
	                }
	                
            	}                
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
	}
	
	class writeThread extends Thread {
        private boolean isEmpty = true;
        private boolean isPause;
        public boolean running = true;

        public writeThread(boolean isPause) {
            this.isPause = isPause;
        }
        
        public void pauseThread(boolean isPause) {
            this.isPause = isPause;
        }
        public void emptyThread(boolean isEmpty) {
        	this.isEmpty = isEmpty;
        }        
        public void shutdown() {
        	running = false;
			emptyThread(true);
			pauseThread(true);	
        }
        
        public void run() {
            while(running) {
            	while(isPause || isEmpty);
            	isEmpty = true;
            		
            	saveData.write(readMessage);
            }
        }
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupChat();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		case R.id.start:
			MenuItem MenuItem = menu.findItem(R.id.start);
			
			if (mChatService.getState() == BluetoothChatService.STATE_CONNECTED) {
				if (mReadThread == null) {
	                button1.setText("일시정지");
	                mReadThread = new readThread(false);
	                mWriteThread = new writeThread(false);
	                mReadThread.start();
	                mWriteThread.start();
	                MenuItem.setTitle("Graph pause");
	            } else if(button1.getText().equals("일시정지")) {
	            	button1.setText("시  작");
	            	mReadThread.pauseThread(true);
	            	mWriteThread.pauseThread(true);
	            	MenuItem.setTitle("Graph start");
	            } else {
	            	button1.setText("일시정지");
	            	mReadThread.pauseThread(false);
	            	mWriteThread.pauseThread(false);
	            	MenuItem.setTitle("Graph pause");
	            }
			} else {
        		Toast.makeText(getApplicationContext(), "Please connect to device", Toast.LENGTH_LONG).show();
        	}
		}
		return false;
	}

}
