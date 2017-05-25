package ghww.bottlenosesender;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import ghww.wcl.CommunicationType;
import ghww.wcl.DeviceType;
import ghww.wcl.GrindhouseDeviceFactory;
import ghww.wcl.IGrindhouseDevice;
import ghww.wcl.IGrindhouseListener;

import static android.os.Build.VERSION_CODES.KITKAT;

@TargetApi(KITKAT)
public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int SETTINGS_RESULT = 1;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayList <String> passedValuesCollection = new ArrayList<String>();
    IGrindhouseDevice gdal = GrindhouseDeviceFactory.GetDevice(DeviceType.BOTTLENOSE, CommunicationType.BLUETOOTH);
    String _selectedDeviceID = "";

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private BroadcastReceiver alarmReceiver;
    private BroadcastReceiver SMSReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        gdal.addListener(dataChangeListener);
        setAlarm();
    }

    @Override
    public void onDestroy() {
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
        super.onDestroy();
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
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_RESULT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                setAlarm();

                // Deregister the SMSReceiver
                // The IllegalArgumentException will be thrown if it's not already registered
                try {
                    if (SMSReceiver != null) unregisterReceiver(alarmReceiver);
                } catch(IllegalArgumentException e) {
                    SMSReceiver = null;
                }

                // This BR will react to a new text message
                // It extracts the text and the sender and sends it to the Bottlenose
                SMSReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle bundle = intent.getExtras();
                        String str = "";

                        final SharedPreferences shared = PreferenceManager
                                .getDefaultSharedPreferences(MainActivity.this);

                        if (bundle != null)
                        {

                            Object[] pdus = (Object[]) bundle.get("pdus");
                            for (Object pdu : pdus) {
                                SmsMessage receivedMsg = SmsMessage.createFromPdu((byte[]) pdu);
                                String pref = shared.getString(
                                        getString(R.string.pref_key_sms),
                                        getString(R.string.pref_sms_none));
                                if(!Objects.equals(pref, getString(R.string.pref_sms_none))) {
                                    // If SMS preference is anything but "Nothing"
                                    // Send to the Bottlenose the sender of the text
                                    str += "SMS from " + receivedMsg.getOriginatingAddress();
                                }
                                if(Objects.equals(pref, getString(R.string.pref_sms_all))) {
                                    // If SMS preference is "all" send the text body as well
                                    str += " :" + receivedMsg.getMessageBody();
                                }
                            }
                        gdal.IssueCustomCommand(str);
                        Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG).show();
                    }
                    }
                };

                IntentFilter iFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                registerReceiver(alarmReceiver, iFilter);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setAlarm() {
        // If preference for enabling metronome is checked, set alarm for this
        final SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);

        if(shared.getBoolean(getString(R.string.pref_key_toggle), false)) {
            int refreshTime = Integer.parseInt(
                    shared.getString(getString(R.string.pref_key_refresh_time),
                            getString(R.string.pref_default_refresh_time)));
            long refreshTimeMillis = refreshTime*60*1000;

            // Create PendingIntent and send it every refreshTime minutes
            Intent intent = new Intent("ghww.bottlenosesender.ALARM_SEND_DATA");
            alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            alarmMgr = (AlarmManager)MainActivity.this.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + refreshTimeMillis,
                    refreshTimeMillis, alarmIntent);

            // Deregister previous alarm so the new one can take over
            try {
                if (alarmReceiver != null) unregisterReceiver(alarmReceiver);
            } catch(IllegalArgumentException e) {
                alarmReceiver = null;
            }

            // Create BroadcastReceiver and IntentFilter to send to Bottlenose when alarm goes off
            alarmReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String sentData = "";
                    if(shared.getBoolean(getString(R.string.pref_key_time), false)) {
                        // Get current time and add to sent data in HHMM format
                        Calendar calendar = Calendar.getInstance();
                        int hours = calendar.get(Calendar.HOUR_OF_DAY);
                        int mins = calendar.get(Calendar.MINUTE);
                        sentData += (String.valueOf(hours) + String.valueOf(mins));
                    }
                    if(shared.getBoolean(getString(R.string.pref_key_temp), false)) {
                        // These two need to be declared outside the try/catch
                        // so that they can be closed in the finally block.
                        HttpURLConnection urlConnection = null;
                        BufferedReader reader = null;

                        // Will contain the raw JSON response as a string.
                        String forecastJsonStr = null;

                        String location = shared.getString(getString(R.string.pref_key_location),
                                getString(R.string.pref_default_location));
                        String units = shared.getString(getString(R.string.pref_units_key),
                                getString(R.string.pref_units_metric));

                        // Get current temperature and add to sent data
                        try {
                            // Construct the URL for the OpenWeatherMap query
                            // Possible parameters are avaiable at OWM's forecast API page, at
                            // http://openweathermap.org/API#forecast
                            final String FORECAST_BASE_URL =
                                    "http://api.openweathermap.org/data/2.5/weather?";
                            final String QUERY_PARAM = "q";
                            final String UNITS_PARAM = "units";
                            final String LOCATION_PARAM = "zip";

                            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                    .appendQueryParameter(UNITS_PARAM, units)
                                    .appendQueryParameter(LOCATION_PARAM, location)
                                    .build();

                            URL url = new URL(builtUri.toString());

                            // Create the request to OpenWeatherMap, and open the connection
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.connect();

                            // Read the input stream into a String
                            InputStream inputStream = urlConnection.getInputStream();
                            StringBuffer buffer = new StringBuffer();
                            if (inputStream == null) {
                                // Nothing to do.
                                sentData += "";
                            }
                            reader = new BufferedReader(new InputStreamReader(inputStream));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                                // But it does make debugging a *lot* easier if you print out the completed
                                // buffer for debugging.
                                buffer.append(line + "\n");
                            }

                            if (buffer.length() == 0) {
                                // Stream was empty.  No point in parsing.
                                sentData += "";
                            }
                            forecastJsonStr = buffer.toString();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Error ", e);
                            // If the code didn't successfully get the weather data, there's no point in attemping
                            // to parse it.
                            return;
                        } finally {
                            if (urlConnection != null) {
                                urlConnection.disconnect();
                            }
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (final IOException e) {
                                    Log.e(LOG_TAG, "Error closing stream", e);
                                }
                            }
                        }

                        try {
                            JSONObject forecastJson = new JSONObject(forecastJsonStr);
                            JSONObject weatherObject = forecastJson.getJSONObject("main");
                            double temp = weatherObject.getDouble("temp");
                            sentData += String.valueOf(temp);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.getMessage(), e);
                            e.printStackTrace();
                        }
                        // This will only happen if there was an error getting or parsing the forecast.
                        sentData += "";
                    }

                    gdal.IssueCustomCommand(sentData);
                }
            };

            IntentFilter iFilter = new IntentFilter("ghww.bottlenosesender.ALARM_SEND_DATA");
            registerReceiver(alarmReceiver, iFilter);
        } else {
            if (alarmMgr != null) {
                alarmMgr.cancel(alarmIntent);
            }
        }
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
        SuitsRow(layout);
        CardsRow1(layout);
        CardsRow2(layout);
        CardsRow3(layout);
        CardsRow4(layout);
        SendCardRow(layout);
    }

    public void onButtonClickSendRaw(View v){
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout_content);
        layout.removeAllViews();
        layout.addView(TextToSendRow());
        layout.addView(SendRawRow());
    }

    //build button
    public void buildButtons(int buttonSet[], LinearLayout layout) {
        Button b;
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        for(int buttonName : buttonSet) {
            // TODO: anything but this
            // if we can pass this function a miltidim array with the value and the desired text,
            // I will sleep much better at night
            if(ButtonIDs.values()[buttonName].name() == "HEARTS" ||
               ButtonIDs.values()[buttonName].name() == "CLUBS" ||
               ButtonIDs.values()[buttonName].name() == "SPADES" ||
               ButtonIDs.values()[buttonName].name() == "DIAMONDS" ||
               ButtonIDs.values()[buttonName].name() == "KING" ||
               ButtonIDs.values()[buttonName].name() == "QUEEN" ||
               ButtonIDs.values()[buttonName].name() == "JACK" ||
               ButtonIDs.values()[buttonName].name() == "ACE")
            {
                b = GetButton(this,ButtonIDs.values()[buttonName].name(), ButtonIDs.values()[buttonName].ordinal());
            }else{
                b = GetButton(this, Integer.toString(ButtonIDs.values()[buttonName].ordinal()), ButtonIDs.values()[buttonName].ordinal());
            }
            b.setOnClickListener(suitButtonListener);
            row.addView(b);
        }
        //return row;
        layout.addView(row);
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
        row.addView(GetButton(this, "Start", ButtonIDs.START_TIME.ordinal()));
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

    private void SendCardRow(LinearLayout layout) {
        Button b;
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        b = GetButton(this, "Send", ButtonIDs.SEND_CARD.ordinal());
        b.setOnClickListener(suitButtonListener);
        row.addView(b);

        //return row;
        layout.addView(row);
    }

    private void SuitsRow(LinearLayout layout) {
        int[] suitRowNames = new int[]{
                ButtonIDs.HEARTS.ordinal(),
                ButtonIDs.DIAMONDS.ordinal(),
                ButtonIDs.SPADES.ordinal(),
                ButtonIDs.CLUBS.ordinal()
        };
        buildButtons(suitRowNames, layout);
    }

    private void CardsRow1(LinearLayout layout) {
        int[] cardRowNames = new int[]{
                ButtonIDs.ACE.ordinal(),
                ButtonIDs.TWO.ordinal(),
                ButtonIDs.THREE.ordinal(),
                ButtonIDs.FOUR.ordinal()
        };
        buildButtons(cardRowNames, layout);
    }

    private void CardsRow2(LinearLayout layout) {
        int[] cardRowNames = new int[]{
                ButtonIDs.FIVE.ordinal(),
                ButtonIDs.SIX.ordinal(),
                ButtonIDs.SEVEN.ordinal(),
                ButtonIDs.EIGHT.ordinal()
        };
        buildButtons(cardRowNames, layout);
    }

    private void CardsRow3(LinearLayout layout) {
        int[] cardRowNames = new int[]{
                ButtonIDs.NINE.ordinal(),
                ButtonIDs.TEN.ordinal(),
                ButtonIDs.JACK.ordinal(),
                ButtonIDs.KING.ordinal()
        };
        buildButtons(cardRowNames, layout);
    }

    private void CardsRow4(LinearLayout layout) {
        int[] cardRowNames = new int[]{
                ButtonIDs.QUEEN.ordinal()
        };
        buildButtons(cardRowNames, layout);
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
        //Toast.makeText(getApplicationContext(), "SUIT BUTTON", Toast.LENGTH_LONG).show();
        Button selectedSuit = (Button) v;

        Toast.makeText(getApplicationContext(), selectedSuit.getText(), Toast.LENGTH_LONG).show();
        String passedValue = selectedSuit.getText().toString();
        if(passedValue.length()>2) {
            passedValue = getCorrectedValue(passedValue);
        }
        if(passedValue.length()<2) {
            passedValue = "0"+passedValue;
        }

        if(passedValue != "Send"){
            //Toast.makeText(getApplicationContext(), "Real Value", Toast.LENGTH_LONG).show();
            passedValuesCollection.add(passedValue);
        }else if(passedValue == "Send" ){
            //Toast.makeText(getApplicationContext(), "Sending...", Toast.LENGTH_LONG).show();
            String sendString = "";
            for(String cardValue : passedValuesCollection){
                sendString+=cardValue;
                //
            }
            if(sendString.length() == 4){
                gdal.IssueCustomCommand(sendString);
                Toast.makeText(getApplicationContext(), "SENT! ", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "TOO MANY CHARS! ", Toast.LENGTH_LONG).show();

            }
            passedValuesCollection.clear();
        }
    }

    private String getCorrectedValue(String passedValue) {
        if(passedValue == "CLUBS"){
            return "11";
        }else if(passedValue == "DIAMONDS"){
            return "22";
        }else if(passedValue == "HEARTS"){
            return "33";
        }else if(passedValue == "SPADES"){
            return "44";
        }else if(passedValue == "ACE"){
            return "01";
        }else if(passedValue == "JACK"){
            return "11";
        }else if(passedValue == "QUEEN"){
            return "12";
        }else if(passedValue == "KING"){
            return "13";
        }else{
            return passedValue;
        }

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
            findViewById(R.id.isConnected).setEnabled(gdal.isConnected());
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
            String noDevices = "No Devices Available".toString();
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
                findViewById(R.id.isConnected).setEnabled(true);
            }
            else if(!gdal.isConnected()){
                tb.setChecked(false);
                findViewById(R.id.isConnected).setEnabled(true);
            }


        }

    };

    public void deviceBack(View v) {
        String info = ((TextView) v).getText().toString();
        String address = "";
        LinearLayout content = (LinearLayout) findViewById(R.id.linearlayout_content);
        LinearLayout devices = (LinearLayout) findViewById(R.id.mydevices);

        Button rawData = (Button) findViewById(R.id.button_rawdata);
        Button timeSender = (Button) findViewById(R.id.button_timesender);
        Button cardSender = (Button) findViewById(R.id.button_cardsender);
        ToggleButton isConnected = (ToggleButton) findViewById(R.id.isConnected);

        if (info.length() > 17)
            address = info.substring(info.length() - 17);
        else
            address = info;

        _selectedDeviceID = address;
        content.setVisibility(View.VISIBLE);
        devices.setVisibility(View.GONE);

        rawData.setVisibility(View.VISIBLE);
        timeSender.setVisibility(View.VISIBLE);
        cardSender.setVisibility(View.VISIBLE);
        isConnected.setVisibility(View.VISIBLE);
    }
}
