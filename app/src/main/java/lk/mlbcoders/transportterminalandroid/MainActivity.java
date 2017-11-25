package lk.mlbcoders.transportterminalandroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    @BindView(R.id.txt_welcome_title)
    TextView txt_welcome_title;
    @BindView(R.id.txt_bus_no_value)
    TextView txt_bus_no_value;
    @BindView(R.id.txt_bus_route_value)
    TextView txt_bus_route_value;
    @BindView(R.id.txt_start_value)
    TextView txt_start_value;
    @BindView(R.id.txt_destination_value)
    TextView txt_destination_value;

    private ZXingScannerView mScannerView;

    SharedPreferences pref;
    String jsonString;
    String ServerURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.custom_title_bar);

        ButterKnife.bind(this);


        jsonString = getIntent().getExtras().getString("Object");
        ServerURL = getIntent().getExtras().getString("ServerURL");

        Log.d("TRANSTERM", "Passed obj : " + jsonString);
        if(jsonString!=null) {
            try {
                JSONObject objectParent = new JSONObject(jsonString);
                JSONObject object = new JSONObject(objectParent.getString("bus_details"));
                String title = objectParent.getString("agency");
                Log.d("TRANSTERM", "Title : " + title);
                txt_welcome_title.setText("WELCOME\nTO\n" + title);
                txt_bus_no_value.setText(object.getString("bus_number"));
                txt_bus_route_value.setText(object.getString("route_number"));
                txt_start_value.setText(object.getString("start_location"));
                txt_destination_value.setText(object.getString("end_location"));

                ArrayList<JSONObject> routeArray = new ArrayList<>();
                JSONArray jsonarray = object.getJSONArray("route");
                if (jsonarray != null) {
                    for (int i = 0; i < jsonarray.length(); i++) {
                        routeArray.add(jsonarray.getJSONObject(i));
                        Log.d("TRANSTERM", "Route " + i + " : " + jsonarray.getJSONObject(i).getString("location"));
                    }
                }

            } catch (Exception e) {

            }

        }
        //QR Scanner


    }

    @Override
    public void handleResult(Result result) {
        Log.d("TRANSTERM", "QR : " + result.getText());
        Log.d("TRANSTERM", "QR Format : " + result.getBarcodeFormat().toString());

        String customer_id = result.getText();
        setContentView(R.layout.activity_main);
        mScannerView.stopCamera();
        Intent intent = new Intent(getApplicationContext(), DestinationSelectionActivity.class);
        intent.putExtra("Customer",customer_id);
        intent.putExtra("MainObject",jsonString);
        intent.putExtra("ServerURL",ServerURL);
        startActivity(intent);
    }


    @OnClick(R.id.btn_scan_qr)
    public void scanQR(View v) {
        if(mScannerView==null) {
            mScannerView = new ZXingScannerView(this);
        }else{
            mScannerView.resumeCameraPreview(this);
        }
        setContentView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}
