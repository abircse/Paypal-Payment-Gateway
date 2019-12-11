package com.example.paypalpaymentgateway;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {


    public static final int PAYPAL_REQUEST_CODE = 7171;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId("AZdksQA0QATU6peaArch3QZwWT5bayh83EmkZ1n6yj-9HJxSLPXyyFYl0VGoSBp-7unjT_GidZHUQsyi");

    private Button button;
    private EditText editText;
    private String ammount = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.submitpayment);
        editText = findViewById(R.id.ammountedittext);

        // Start Intent Service
        Intent intent = new Intent(this,PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProcessPaymentNow();


            }
        });

    }

    private void ProcessPaymentNow() {

        ammount = editText.getText().toString();
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(ammount)),
                "USD", "Donate for Beach Eatz", PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
        startActivityForResult(intent,PAYPAL_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentdetails = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentdetails);
                        showDetails(jsonObject.getJSONObject("response"),ammount);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if (resultCode == Activity.RESULT_CANCELED)
                {
                    Toast.makeText(getApplicationContext(), "Cancle", Toast.LENGTH_SHORT).show();

                }
            }
            else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
            {
                Toast.makeText(getApplicationContext(), "iNVALID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDetails(JSONObject response, String ammount) {

        try {

            Toast.makeText(this, "ID = "+response.getString("id"), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "STATE = "+response.getString("state"), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "AMMOUNT = "+ammount, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this,PayPalService.class));
        super.onDestroy();
    }
}
