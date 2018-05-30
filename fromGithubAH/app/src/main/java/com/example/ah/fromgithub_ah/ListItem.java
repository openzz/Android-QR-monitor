package com.example.ah.fromgithub_ah;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by AH on 5/2/2018.
 */

public class ListItem extends AppCompatActivity {


    double light;
    double voltage;
    String connString;

    public static String responce = "Nothing";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_item);

        Intent intent = getIntent();
        connString = intent.getStringExtra("ConnectionString");
        TextView host = (TextView)findViewById(R.id.host);
        TextView device = (TextView)findViewById(R.id.device);
        Map<String, String> deviceInfo = parseConnStr(connString);
        host.setText(deviceInfo.get("Host"));
        device.setText(deviceInfo.get("DeviceID"));
    }

    public Map parseConnStr(String connstr){
        Map<String, String> deviceInfo = new HashMap<String, String>();
        String[] parts = connstr.split(";");
        deviceInfo.put("Host", parts[0].substring(parts[0].indexOf("=")+1));
        deviceInfo.put("DeviceID", parts[1].substring(parts[1].indexOf("=")+1));
        deviceInfo.put("SASkey", parts[2].substring(parts[2].indexOf("=")+1));
        deviceInfo.put("ConnectionString", connstr);

        return deviceInfo;
    }

    public void btnReceiveOnClick(View v) throws URISyntaxException, IOException
    {
        System.out.println("********************************************************************************************************************************************");
        System.out.println("Receiving:");
        Button button = (Button) v;

        // Comment/uncomment from lines below to use HTTPS or MQTT protocol
        IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
        //IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

        DeviceClient client = new DeviceClient(connString, protocol);

        if (protocol == IotHubClientProtocol.MQTT)
        {
            MessageCallbackMqtt callback = new MessageCallbackMqtt();
            Counter counter = new Counter(0);
            client.setMessageCallback(callback, counter);
        } else
        {
            MessageCallback callback = new MessageCallback();
            Counter counter = new Counter(0);
            client.setMessageCallback(callback, counter);
        }

        try
        {
            client.open();
        } catch (Exception e2)
        {
            System.out.println("Exception while opening IoTHub connection: " + e2.toString());
        }

        try
        {
            Thread.sleep(1000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        client.closeNow();

        try {
            JSONObject incomingJson = new JSONObject(responce);
            light = incomingJson.getDouble("light");
            voltage = incomingJson.getDouble("voltage");

            TextView textLight = (TextView)findViewById(R.id.light);
            textLight.setText(Double.toString(light));

            TextView textVolt = (TextView)findViewById(R.id.volt);
            textVolt.setText(Double.toString(voltage));

        }catch (JSONException je){
            System.out.println("Unable to parse json.");
            TextView textLight = (TextView)findViewById(R.id.light);
            textLight.setText("N/A");
            TextView textVolt = (TextView)findViewById(R.id.volt);
            textVolt.setText("N/A");
        }
    }



    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            responce = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
            Counter counter = (Counter) context;
            System.out.println(
                    "[from MessageCallbackMqtt] Received message " + counter.toString()
                            + " with content: " + responce);

            counter.increment();
            return IotHubMessageResult.COMPLETE;
        }
    }

    static class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            Integer i = (Integer) context;
            System.out.println("[from EventCallback]  IoT Hub responded to message " + i.toString()
                    + " with status " + status.name());
        }
    }

    static class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

            int switchVal = counter.get() % 3;
            IotHubMessageResult res;
            switch (switchVal)
            {
                case 0:
                    res = IotHubMessageResult.COMPLETE;
                    break;
                case 1:
                    res = IotHubMessageResult.ABANDON;
                    break;
                case 2:
                    res = IotHubMessageResult.REJECT;
                    break;
                default:
                    // should never happen.
                    throw new IllegalStateException("Invalid message result specified.");
            }

            System.out.println("Responding to message " + counter.toString() + " with " + res.name());
            counter.increment();
            return res;
        }
    }

    /**
     * Used as a counter in the message callback.
     */
    static class Counter
    {
        int num;
        Counter(int num) {
            this.num = num;
        }
        int get() {
            return this.num;
        }
        void increment() {
            this.num++;
        }

        @Override
        public String toString() {
            return Integer.toString(this.num);
        }
    }

}
