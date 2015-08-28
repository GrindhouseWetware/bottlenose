package ghww.bottlenosesender;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Queue;
import java.util.Set;

import ghww.wcl.CommunicationType;
import ghww.wcl.DeviceType;
import ghww.wcl.GrindhouseDeviceFactory;
import ghww.wcl.IGrindhouseDevice;
import ghww.wcl.IGrindhouseListener;


public class MainActivity extends ActionBarActivity {
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    IGrindhouseDevice gdal = GrindhouseDeviceFactory.GetDevice(DeviceType.BOTTLENOSE, CommunicationType.BLUETOOTH);
    String _selectedDeviceID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        gdal.addListener(dataChangeListener);
    }

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

    public void onButtonClickSendTime(View v) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout_content);
        layout.removeAllViews();
        layout.addView(TimeRow());
        layout.addView(StartRow());
    }

    public void onButtonClickSendCards(View v){
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout_content);
        layout.removeAllViews();
        layout.addView(SuitsRow());
        layout.addView(CardsRow1());
        layout.addView(CardsRow2());
        layout.addView(CardsRow3());
        layout.addView(CardsRow4());

        layout.addView(SendCardRow());
    }

    public void onButtonClickSendRaw(View v){
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout_content);
        layout.removeAllViews();
        layout.addView(TextToSendRow());
        layout.addView(SendRawRow());
    }

    //PRIVATE

    private View TimeRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this, "Speed 1", ButtonIDs.TIME_SPEED1.ordinal()));
        row.addView(GetButton(this, "Speed 2", ButtonIDs.TIME_SPEED2.ordinal()));
        row.addView(GetButton(this, "Speed 3", ButtonIDs.TIME_SPEED3.ordinal()));
        return row;
    }

    private View StartRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this,"Start",ButtonIDs.START_TIME.ordinal()));
        return row;
    }

    private View TextToSendRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        EditText txtTag = new EditText(this);
        txtTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        txtTag.setText("Start");
        txtTag.setId(ButtonIDs.TEXT_ELEMENT.ordinal());
        row.addView(txtTag);
        return row;
    }

    private View SendRawRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this, "Send", ButtonIDs.SEND_RAW.ordinal()));
        return row;
    }

    private View SendCardRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this,"Send",ButtonIDs.SEND_CARD.ordinal()));
        return row;
    }

    private View SuitsRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this,"Diamonds",ButtonIDs.DIAMONDS.ordinal()));
        row.addView(GetButton(this, "Hearts", ButtonIDs.HEARTS.ordinal()));
        row.addView(GetButton(this, "Clubs", ButtonIDs.CLUBS.ordinal()));
        row.addView(GetButton(this, "Spades", ButtonIDs.SPADES.ordinal()));
        return row;
    }

    private View CardsRow1() {
        Button b;
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        b = GetButton(this, "A", ButtonIDs.ACE.ordinal());
        b.setOnClickListener(cardButtonListener);
        row.addView(b);
        b = GetButton(this, "2", ButtonIDs.TWO.ordinal());
        b.setOnClickListener(cardButtonListener);
        row.addView(b);
        b = GetButton(this, "3", ButtonIDs.THREE.ordinal());
        b.setOnClickListener(cardButtonListener);
        row.addView(b);
        b = GetButton(this, "4", ButtonIDs.FOUR.ordinal());
        b.setOnClickListener(cardButtonListener);
        row.addView(b);
        return row;
    }

    private View CardsRow2() {
        Button b;
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this, "5", ButtonIDs.FIVE.ordinal()));
        row.addView(GetButton(this, "6", ButtonIDs.SIX.ordinal()));
        row.addView(GetButton(this, "7", ButtonIDs.SEVEN.ordinal()));
        row.addView(GetButton(this, "8", ButtonIDs.EIGHT.ordinal()));
        return row;
    }

    private View CardsRow3() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this, "9", ButtonIDs.NINE.ordinal()));
        row.addView(GetButton(this, "10",ButtonIDs.TEN.ordinal()));
        row.addView(GetButton(this, "J", ButtonIDs.JACK.ordinal()));
        row.addView(GetButton(this, "Q", ButtonIDs.QUEEN.ordinal()));
        return row;
    }

    private View CardsRow4() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(GetButton(this, "K", ButtonIDs.KING.ordinal()));
        return row;
    }

    private Button GetButton(Activity mainActivity, String text, int id) {
        Button btnTag = new Button(mainActivity);
        btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btnTag.setText(text);
        btnTag.setId(id);
        return btnTag;
    }

    public enum ButtonIDs{
        DEFAULT,
        ACE,
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        DIAMONDS,
        HEARTS,
        CLUBS,
        SPADES,
        TIME_SPEED1,
        TIME_SPEED2,
        TIME_SPEED3,
        START_TIME,
        TEXT_ELEMENT,
        SEND_RAW,
        SEND_CARD
    }

    //card button
    public void onCardButton(View v) {
        Toast.makeText(getApplicationContext(),"CARD BUTTON", Toast.LENGTH_LONG).show();
        gdal.IssueCustomCommand("CARD BUTTON");
    }
    View.OnClickListener cardButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onCardButton(view);
        }
    };
    //suit buttom
    public void onSuitButton(View v) {
        Toast.makeText(getApplicationContext(), "SUIT BUTTON", Toast.LENGTH_LONG);
    }
    View.OnClickListener suitButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onSuitButton(view);
        }
    };
    //time start
    public void onTimeStartButton(View v) {
        Toast.makeText(getApplicationContext(),"TIME START", Toast.LENGTH_LONG);
    }
    View.OnClickListener timeStartButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onTimeStartButton(view);
        }
    };
    //send suit
    public void onSendSuitButton(View v) {
        Toast.makeText(getApplicationContext(),"SEND SUIT", Toast.LENGTH_LONG);
    }
    View.OnClickListener sendSuitButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onSendSuitButton(view);
        }
    };
    //send raw
    public void onSendRawButton(View v) {
        Toast.makeText(getApplicationContext(),"SEND RAW", Toast.LENGTH_LONG);
    }
    View.OnClickListener sendRawButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onSendRawButton(view);
        }
    };

    public void onConnectionToggle(View v){
        LinearLayout content = (LinearLayout)findViewById(R.id.linearlayout_content);
        LinearLayout devices = (LinearLayout)findViewById(R.id.mydevices);
        if(((ToggleButton)v).isChecked()){
            loadAdapters(null);
            if(mNewDevicesArrayAdapter.getCount() > 0 || mPairedDevicesArrayAdapter.getCount() > 0) {
                content.setVisibility(View.GONE);
                findViewById(R.id.button_cardsender).setVisibility(View.GONE);
                findViewById(R.id.button_rawdata).setVisibility(View.GONE);
                findViewById(R.id.button_timesender).setVisibility(View.GONE);
                v.setVisibility(View.GONE);
                devices.setVisibility(View.VISIBLE);
            }
        }
        else{
            gdal.DisconnectDevice();
        }

    }
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = "";
            LinearLayout content = (LinearLayout)findViewById(R.id.linearlayout_content);
            LinearLayout devices = (LinearLayout)findViewById(R.id.mydevices);

            if(info.length() > 17)
                address = info.substring(info.length() - 17);
            else
                address = info;

            _selectedDeviceID = address;
            content.setVisibility(View.VISIBLE);
            findViewById(R.id.button_cardsender).setVisibility(View.VISIBLE);
            findViewById(R.id.button_rawdata).setVisibility(View.VISIBLE);
            findViewById(R.id.button_timesender).setVisibility(View.VISIBLE);
            devices.setVisibility(View.GONE);
            gdal.ConnectToDevice("", address);
            findViewById(R.id.isConnected).setVisibility(View.VISIBLE);
            ((ToggleButton)findViewById(R.id.isConnected)).setChecked(gdal.isConnected());
            ((ToggleButton)findViewById(R.id.isConnected)).setEnabled(gdal.isConnected());
            mNewDevicesArrayAdapter.clear();
            mPairedDevicesArrayAdapter.clear();

        }
    };
    private void loadAdapters(BluetoothAdapter btAdapter){

        // Find and set up the ListView for paired devices
        if(btAdapter == null) btAdapter = BluetoothAdapter.getDefaultAdapter();
        ListView pairedListView = (ListView) findViewById(R.id.listView);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.listView2);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        Set<BluetoothDevice> pairedDevices;
        // Get a set of currently paired devices
        try {
            pairedDevices = btAdapter.getBondedDevices();
        }
        catch (Exception e){

            pairedDevices = null;
        }
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices != null && pairedDevices.size() > 0) {
            //findViewById(R.id.mydevices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "No Devices Bitch".toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
        ListView lv = (ListView)findViewById(R.id.listView);
        lv.setAdapter(mPairedDevicesArrayAdapter);
        btAdapter.startDiscovery();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                //setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "No Devices";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
            ListView lv = (ListView)findViewById(R.id.listView2);
            lv.setAdapter(mNewDevicesArrayAdapter);
        }
    };


    private final IGrindhouseListener dataChangeListener = new IGrindhouseListener(){
        @Override
        public void dataChanged(Queue<String> msgQueue) {
            ToggleButton tb = (ToggleButton)findViewById(R.id.isConnected);
            if(gdal.isConnected()) {
                tb.setChecked(true);
                ((ToggleButton)findViewById(R.id.isConnected)).setEnabled(true);
            }
            else if(!gdal.isConnected()){
                tb.setChecked(false);
                ((ToggleButton)findViewById(R.id.isConnected)).setEnabled(true);
            }


        }

    };
}
