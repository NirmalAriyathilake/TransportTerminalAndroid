package lk.mlbcoders.transportterminalandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DestinationSelectionActivity extends AppCompatActivity {


    SharedPreferences pref;

    @BindView(R.id.txt_title)
    TextView txt_title;

    @BindView(R.id.txt_balance_value)
    TextView txt_balance_value;

//    @BindView(R.id.txt_start_location_value)
//    TextView txt_start_location_value;

    @BindView(R.id.route_list)
    ListView route_list;

    HashMap<String,String> hashMap;
    String selectedValue = "";
    String balance;
    String name;
    String mainJson;
    String ServerURL;
    ArrayList<String> stringArray;

    String customer_id;
    NetworkHelper networkHelper;
    JSONObject object;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_selection);

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

        Log.d("TRANSTERM", "Main pref : " + pref.getString("server_url", "192.168.8.101"));

        ButterKnife.bind(this);

         customer_id = getIntent().getExtras().getString("Customer");
        mainJson = getIntent().getExtras().getString("MainObject");
        ServerURL = getIntent().getExtras().getString("ServerURL");
        Log.d("TRANSTERM", "Cu id : " + customer_id);
        Log.d("TRANSTERM", "MainJSON : " + mainJson);
        networkHelper = new NetworkHelper();

        final String json = "{\"id\": \"" + customer_id + "\"}";
        String serverU = ServerURL + "terminal/passenger";
        Log.d("TRANSTERM", "Server URL : " + serverU);

        networkHelper.post(serverU, json, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d("TRANSTERM", "Json String : " + responseStr);
                try {
                    JSONObject jsonObject = new JSONObject(responseStr);
                    name = jsonObject.getString("passenger_name");
                    balance = jsonObject.getString("passenger_card");
                    txt_title.setText("WELCOME\n" + name);
                    txt_balance_value.setText(balance);

                    Log.d("TRANSTERM","NAME : " + name + "  BAlance : " + balance);


                    JSONObject objectParent = new JSONObject(mainJson);
                    object = new JSONObject(objectParent.getString("bus_details"));

                    ArrayList<JSONObject> routeArray = new ArrayList<>();
                    stringArray = new ArrayList<>();
                    hashMap = new HashMap<>();
                    JSONArray jsonarray = object.getJSONArray("route");
                    if (jsonarray != null) {
                        for (int i = 0; i < jsonarray.length(); i++) {
                            routeArray.add(jsonarray.getJSONObject(i));
                            stringArray.add(jsonarray.getJSONObject(i).getString("location"));
                            hashMap.put(jsonarray.getJSONObject(i).getString("location"),jsonarray.getJSONObject(i).getString("price"));
                            Log.d("TRANSTERM", "D Route " + i + " : " + jsonarray.getJSONObject(i).getString("location"));
                        }
                    }

                    ArrayAdapter<String> itemsAdapter =
                            new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArray);
                    route_list.setAdapter(itemsAdapter);

//                    txt_start_location_value.setText(stringArray.get(1));
//                    ((TextView)route_list.getItemAtPosition(0)).setEnabled(false);
//                    ((TextView)route_list.getItemAtPosition(0)).setBackgroundColor(getResources().getColor(R.color.colorWhite2));
//                    ((TextView)route_list.getItemAtPosition(0)).setBackgroundColor(getResources().getColor(R.color.colorBlack));
//                    ((TextView)route_list.getItemAtPosition(1)).setEnabled(false);
//                    ((TextView)route_list.getItemAtPosition(1)).setBackgroundColor(getResources().getColor(R.color.colorWhite2));
//                    ((TextView)route_list.getItemAtPosition(1)).setBackgroundColor(getResources().getColor(R.color.colorBlack));

                    route_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            // When clicked, show a toast with the TextView text
                            Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                                    Toast.LENGTH_SHORT).show();
                            ((TextView)view).setBackgroundColor(getResources().getColor(R.color.colorWhite));
                            ((TextView)view).setTextColor(getResources().getColor(R.color.colorBlack));
                            selectedValue = ((TextView) view).getText().toString();
                            Log.d("TRANSTERM","SElected : "  + selectedValue);
                        }
                    });
                } catch (Exception e) {

                }
            }
        });


    }

    @OnClick(R.id.btn_selected)
    public void btnSelected(View button){
        if(selectedValue.equals("")){
           try{
               stringArray = new ArrayList<>();
               hashMap = new HashMap<>();
               JSONArray jsonarray = object.getJSONArray("route");
               if (jsonarray != null) {
                   for (int i = 0; i < jsonarray.length(); i++) {
                       stringArray.add(jsonarray.getJSONObject(i).getString("location"));
                       hashMap.put(jsonarray.getJSONObject(i).getString("location"),jsonarray.getJSONObject(i).getString("price"));
                       Log.d("TRANSTERM", "DR Route " + i + " : " + jsonarray.getJSONObject(i).getString("location"));
                   }
               }

               ArrayAdapter<String> itemsAdapter =
                       new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, stringArray);
               route_list.setAdapter(itemsAdapter);
           }catch (Exception e){

           }
        }
        final String cost = hashMap.get(selectedValue);
        if(Double.parseDouble(balance) > Double.parseDouble(cost)){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DestinationSelectionActivity.this);

            // set title

            alertDialogBuilder.setTitle("Payment Confirmed");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Dear " + name + "\nYour payment is verified. Your balance is " + String.valueOf(Double.parseDouble(balance) - Double.parseDouble(cost)) )
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            final String json = "{\"card_id\": \"" + customer_id + "\", \"amount\":\"" + Double.parseDouble(cost) + "\"}";
                            String serverU = ServerURL + "terminal/cards/balance";
                            Log.d("TRANSTERM", "Server URL : " + serverU);

                            networkHelper.post(serverU, json, new Callback() {
                                @Override
                                public void onFailure(Request request, IOException e) {
                                }

                                @Override
                                public void onResponse(Response response) throws IOException {
//                                    String responseStr = response.body().string();
//                                    Log.d("TRANSTERM", "Json String : " + responseStr);

                                }
                            });

                            dialog.dismiss();
                            Intent intent = new Intent(DestinationSelectionActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("Object",mainJson);
                            intent.putExtra("ServerURL",ServerURL);
                            startActivity(intent);
                        }
                    });
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();



            // show it
            alertDialog.show();

        }else{
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DestinationSelectionActivity.this);

            // set title

            alertDialogBuilder.setTitle("Payment Denied");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Dear " + name + "\nYour balance is insufficient.")
                    .setCancelable(false)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.dismiss();
                            Intent intent = new Intent(DestinationSelectionActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("Object",mainJson);
                            intent.putExtra("ServerURL",ServerURL);
                            startActivity(intent);
                        }
                    });
            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();



            // show it
            alertDialog.show();

        }
    }
}
