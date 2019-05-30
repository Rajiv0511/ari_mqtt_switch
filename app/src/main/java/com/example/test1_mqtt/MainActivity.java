package com.example.test1_mqtt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.OptionalDataException;

public class MainActivity extends AppCompatActivity
{
    static String MQTTHOST = "tcp://arismartswitch.com:1883";
    String topicstr = "ari";
    String msg = "1";
    String mesge = "0";
    int i;
    String s = "";
    String myMsg, success, id, status;
    MqttAndroidClient client;

    public static TextView subText;
    private Switch switch1, switch2, switch3, switch4, switch5, switch6, switch7, switch8;
    private static final int[] idArray = {R.id.switch1, R.id.switch2, R.id.switch3, R.id.switch4,
            R.id.switch5, R.id.switch6, R.id.switch7, R.id.switch8, R.id.switch9, R.id.switch10,
            R.id.switch11, R.id.switch12, R.id.switch13, R.id.switch14, R.id.switch15, R.id.switch16,
            R.id.switch17, R.id.switch18, R.id.switch19, R.id.switch20};
    private Switch[] switches = new Switch[idArray.length];
    String url ="https://api.myjson.com/bins/kdoyr";
    RequestQueue rq;
    MqttConnectOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        subText = (TextView) findViewById(R.id.subText);
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);
        options = new MqttConnectOptions();
        rq = Volley.newRequestQueue(this);
        try
        {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {Toast.makeText(MainActivity.this, "Connected..!", Toast.LENGTH_SHORT).show();
                    setSubscription();}
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {Toast.makeText(MainActivity.this, "Disconnected..!", Toast.LENGTH_SHORT).show();}
            });
        } catch (MqttException e){e.printStackTrace();}
        client.setCallback(new MqttCallback(){
            @Override
            public void connectionLost(Throwable cause)
            {}
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception
            {
                //subText.setText(new String(message.getPayload()));
                sendjsonrquest();
                myMsg = new String(message.getPayload());
                if(topic.equals(topicstr))
                {
                    JSONObject objj = new JSONObject(myMsg);
                    JSONArray arr = objj.getJSONArray("led");

                    String led = arr.getJSONObject(0).getString("status");
                    String[] strArray = led.split("");
                    char[] ch=led.toCharArray();
                    for(int i=0; i <= ch.length; i++)
                    {
                        if(String.valueOf(ch[i]).equals("1"))
                        {
                            switches[i] = (Switch)findViewById(idArray[i]);
                            switches[i].setChecked(true);
                        }
                        else
                        {
                            switches[i] = (Switch)findViewById(idArray[i]);
                            switches[i].setChecked(false);
                        }
                    }
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token)
            {}
        });
    }

    public void sendjsonrquest()
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    success = response.getString("success");
                    id = response.getString("id");
                    status = response.getString("status");
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {}
        });
        rq.add(jsonObjectRequest);
    }

    public void push1(View v) throws JSONException
    {
        for(i=0; i<idArray.length; i++)
        {
            switches[i] = (Switch)findViewById(idArray[i]);
            if (switches[i].isChecked())
            {
                s = s.concat("1");
            }else
            {
                s = s.concat("0");
            }
        }
        JSONObject obj = new JSONObject();
        JSONObject rootObj = new JSONObject();

        obj.put("status", s);
        obj.put("id", "1");

        JSONArray ja = new JSONArray();
        ja.put(obj);

        rootObj.put("success","1");
        rootObj.put("led",ja);

        try
        {
            client.publish(topicstr, rootObj.toString().getBytes(), 0, true);
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
        s="";
    }

    private void setSubscription()
    {
        try
        {
            client.subscribe(topicstr,0);
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

    public void conn(View v)
    {
        try
        {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    Toast.makeText(MainActivity.this, "Connected..!", Toast.LENGTH_SHORT).show();
                    setSubscription();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    Toast.makeText(MainActivity.this, "Disconnected..!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

    public void disconn(View v)
    {
        try
        {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener()
            {
                @Override
                public void onSuccess(IMqttToken asyncActionToken)
                {
                    Toast.makeText(MainActivity.this, "Disconnected..!", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception)
                {
                    Toast.makeText(MainActivity.this, "Connected..!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }
    }

}