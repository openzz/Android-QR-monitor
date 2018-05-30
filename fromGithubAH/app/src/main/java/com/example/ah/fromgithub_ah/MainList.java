package com.example.ah.fromgithub_ah;


import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.ListView;


/**
 * Created by AH on 4/25/2018.
 */

public class MainList extends ListFragment implements OnClickListener {

    private static final int SRTING_CAPTURE = 12;
    Map <String, String> devices = new HashMap<String, String>();

    public Map parseConnStr(String connstr) throws URISyntaxException {
            Map<String, String> deviceInfo = new HashMap<String, String>();
            String[] parts = connstr.split(";");
            deviceInfo.put("Host", parts[0].substring(parts[0].indexOf("=") + 1));
            deviceInfo.put("DeviceID", parts[1].substring(parts[1].indexOf("=") + 1));
            deviceInfo.put("SASkey", parts[2].substring(parts[2].indexOf("=") + 1));
            deviceInfo.put("ConnectionString", connstr);
            return deviceInfo;
    }

    public void showList(){
        List<String> deviceList = new ArrayList<String>(devices.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, deviceList);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment, container, false);
        Button button = (Button)v.findViewById(R.id.buttonAdd);
        button.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
       showList();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonAdd) {
            Intent intent = new Intent(getActivity(), AddActivity.class);
            startActivityForResult(intent, SRTING_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        try {
            if (requestCode == SRTING_CAPTURE) {
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    try {
                        Map<String, String> deviceInfo = parseConnStr(data.getStringExtra("ConnectionString"));
                        devices.put(deviceInfo.get("DeviceID"), deviceInfo.get("ConnectionString"));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            System.out.println("No incoming connection data.");
        }
        showList();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String clickedDetail = (String)l.getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), ListItem.class);
        intent.putExtra("ConnectionString", devices.get(clickedDetail));
        startActivity(intent);
    }
}