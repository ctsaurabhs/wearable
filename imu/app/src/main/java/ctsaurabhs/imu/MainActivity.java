package ctsaurabhs.imu;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.UUID;
//import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
//import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity {

    Button btnOn, btnOff;
    TextView txtArduino, txtString, txtStringLength, sensorView0, sensorView1;
    Handler bluetoothIn;



    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private float lastX = 0;

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    private static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // get handles to Views defined in our layout file

        final File file = new File(getExternalFilesDir(null), "imuDATA.txt");
        final String parentPath = file.getParent() + File.separator + "imuDATA.txt";

        GraphView graph = (GraphView) findViewById(R.id.graph);

        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(500);
        viewport.setScrollable(true);

        context = getApplicationContext();
        if(isExternalStorageWritable())
        {
            Toast.makeText(getBaseContext(), "YES EXTERN", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getBaseContext(), "No EXTERN", Toast.LENGTH_SHORT).show();
        }


        //Link the buttons and textViews to respective views
        btnOn = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);
        txtString = (TextView) findViewById(R.id.txtString);
        txtStringLength = (TextView) findViewById(R.id.testView1);
        sensorView0 = (TextView) findViewById(R.id.sensorView0);
        sensorView1 = (TextView) findViewById(R.id.sensorView1);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        try {
                            // Very simple code to copy a picture from the application's
                            // resource into the external file.  Note that this code does
                            // no error checking, and assumes the picture is small (does not
                            // try to copy it in chunks).  Note that if external storage is
                            // not currently mounted this will silently fail.

                            FileOutputStream os = new FileOutputStream(parentPath,true);
                            os.write(dataInPrint.getBytes());
                            os.close();
                        } catch (IOException e) {
                            // Unable to create file, likely because external storage is
                            // not currently mounted.
                            Log.w("ExternalStorage", "Error writing ", e);
                        }


                        if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                        {
                            String pitch_0 = null;
                            String pitch_1 = null;

                            String mpuNO = recDataString.substring(7,7);
                            Toast.makeText(getBaseContext(), mpuNO, Toast.LENGTH_SHORT).show();
                            pitch_0 = recDataString.substring(12,endOfLineIndex);
                            Toast.makeText(getBaseContext(), pitch_0, Toast.LENGTH_SHORT).show();

//                            if(0 == Integer.parseInt(mpuNO))
  //                          {
    //                            pitch_0 = recDataString.substring(11,endOfLineIndex);
  //                          } else if(1 == Integer.parseInt(mpuNO))
                            {
      //                          pitch_1 = recDataString.substring(11,endOfLineIndex);
                            }


                            sensorView0.setText(" Pitch 0  = " + pitch_0 + " ");	//update the textviews with sensor values
                            sensorView1.setText(" Pitch 1  = " + pitch_1 + " ");

                            if (pitch_0 != null){
                                try {
                                    // since we know that our string value is an float number we can parse it to a float
                                    final float sensorReading = Float.parseFloat(pitch_0);
                                    series.appendData(new DataPoint(lastX, sensorReading), false, 500);
                                    lastX += 0.2;
                                   // writeToFile(sensor0, context);
/*
                                    try {
                                        // Very simple code to copy a picture from the application's
                                        // resource into the external file.  Note that this code does
                                        // no error checking, and assumes the picture is small (does not
                                        // try to copy it in chunks).  Note that if external storage is
                                        // not currently mounted this will silently fail.

                                        FileOutputStream os = new FileOutputStream(parentPath,true);
                                        os.write(pitch_0.getBytes());
                                        os.close();
                                    } catch (IOException e) {
                                        // Unable to create file, likely because external storage is
                                        // not currently mounted.
                                        Log.w("ExternalStorage", "Error writing ", e);
                                    }
*/
                                    //  series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 10);
                                }
                                catch (NumberFormatException e) { /* oh data was not an integer */ }
                            }
                        }
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();


        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");    // Send "1" via Bluetooth
                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                   // Toast.makeText(getBaseContext(), readMessage, Toast.LENGTH_LONG).show();
                   // Toast.makeText(getBaseContext(), "read msg", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}
