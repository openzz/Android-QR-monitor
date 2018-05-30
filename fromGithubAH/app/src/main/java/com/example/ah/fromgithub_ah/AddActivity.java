package com.example.ah.fromgithub_ah;

        import android.app.Activity;
        import android.app.Fragment;
        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.EditText;

        import com.google.android.gms.common.api.CommonStatusCodes;
        import com.google.android.gms.vision.barcode.Barcode;

public class AddActivity extends Activity implements View.OnClickListener {

    String connString;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    EditText connStr;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        findViewById(R.id.read_barcode).setOnClickListener(this);
        connStr = (EditText)findViewById(R.id.input);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
        if (v.getId() == R.id.buttonOk){
            // save and return to previous activity
            if(connStr != null) {
                connString = connStr.getText().toString();
                Intent retIntent = new Intent();
                retIntent.putExtra("ConnectionString", connString);
                setResult(CommonStatusCodes.SUCCESS, retIntent);
                finish();
            }else{
                finish();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    //barcodeValue.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                    connStr.setText(barcode.displayValue);
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                        CommonStatusCodes.getStatusCodeString(resultCode);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        //connStr.setText(connString);
    }
}