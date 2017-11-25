package lk.mlbcoders.transportterminalandroid;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by user on 11/24/2017.
 */

public class LoginActivity extends AppCompatActivity {


    @BindView(R.id.txtUsernameLayout)
    TextInputLayout txtUsernameLayout;
    @BindView(R.id.txtPasswordLayout)
    TextInputLayout txtPasswordLayout;
    @BindView(R.id.txtServerURLLayout)
    TextInputLayout txtServerURLLayout;
    @BindView(R.id.login_progress)
    ProgressBar login_progress;

    String txtUsername, txtPassword;

    SharedPreferences pref;
    Dialog reset;

    String serverURL;
    String serverU;
    Boolean loginStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

    }

    @OnClick(R.id.btnSignIn)
    public void btnSignInClicked(View button) {
        login_progress.setVisibility(View.VISIBLE);

        serverURL = txtServerURLLayout.getEditText().getText().toString();
        serverURL = "http://" + serverURL + ":3000/api/";
        if (!serverURL.trim().equals("")) {
            SharedPreferences.Editor edit = pref.edit();
            edit.putString("server_url", serverURL);
            edit.commit();
        }
        Log.d("TRANSTERM", "URLs : " + serverURL + " pref : " + pref.getString("server_url", "192.168.8.101"));

        txtUsername = txtUsernameLayout.getEditText().getText().toString();

        if (txtUsername.trim().equals("")) {
//            txtUsernameLayout.setError(getString(R.string.error_field_required));
        } else {
            txtPassword = txtPasswordLayout.getEditText().getText().toString();
            if (txtPassword.trim().equals("")) {
//                txtPasswordLayout.setError(getString(R.string.error_field_required));
            } else {

                Log.d("TRANSTERM", "Username : " + txtUsername + " Password : " + txtPassword);

                NetworkHelper networkHelper = new NetworkHelper();

                final String json = "{\"username\": \"" + txtUsername + "\", \"password\":\"" + txtPassword + "\"}";
                serverU = serverURL + "driver/authenticate";
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
                            loginStatus = jsonObject.getBoolean("login_status");
                            Log.d("TRANSTERM", "LoginStatus " + loginStatus);
                            if (loginStatus) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("Object",responseStr);
                                intent.putExtra("ServerURL",serverURL);
                                startActivity(intent);
                            }
                        } catch (Exception e) {

                        }
                    }
                });


            }
        }
    }
}
