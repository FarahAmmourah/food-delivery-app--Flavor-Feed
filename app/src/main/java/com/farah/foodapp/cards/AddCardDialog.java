package com.farah.foodapp.cards;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.farah.foodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddCardDialog extends DialogFragment {

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;

    private AddCardListener listener;

    public interface AddCardListener {
        void onPaymentSuccess(String last4, String expiry, String holderName);
    }

    public AddCardDialog(AddCardListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);
        builder.setMessage("Processing payment...")
                .setCancelable(false);
        String publishableKey = getString(R.string.stripe_publishable_key);

        // Initialize Stripe with publishable key
        PaymentConfiguration.init(requireContext(), publishableKey);

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Call backend to create payment intent
        createPaymentIntentOnBackend();

        return builder.create();
    }

    private void createPaymentIntentOnBackend() {
        new Thread(() -> {// better than to open in UI thread for security
            try {
                // Prepare JSON data to send to backend
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("amount", 1000);

                // Connect to backend URL
                URL url = new URL(getString(R.string.backend_url) + "/api/payments/create-payment-intent/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Send JSON body
                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                // Get response from backend
                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                // Parse backend response
                JSONObject jsonResponse = new JSONObject(response.toString());

                if (responseCode == HttpURLConnection.HTTP_OK && jsonResponse.optBoolean("success", false)) {
                    paymentIntentClientSecret = jsonResponse.getString("client_secret");
                    requireActivity().runOnUiThread(this::presentPaymentSheet);
                } else {
                    // Handle error from backend
                    String errorMessage = jsonResponse.optString("message", "Unknown error");
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Backend error: " + errorMessage, Toast.LENGTH_LONG).show()
                    );
                    dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    dismiss();
                });
            }
        }).start();
    }

    // Show the Stripe PaymentSheet to user
    private void presentPaymentSheet() {
        PaymentSheet.Configuration config = new PaymentSheet.Configuration("Your Merchant Name");
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, config);
    }

    // Handle result after user interacts with PaymentSheet
    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Completed) {
            Toast.makeText(requireContext(), "Payment successful!", Toast.LENGTH_SHORT).show();
            fetchCardDetails(paymentIntentClientSecret); // Get card info from backend
        } else if (result instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(requireContext(), "Payment canceled.", Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Failed) {
            String error = ((PaymentSheetResult.Failed) result).getError().getMessage();
            Toast.makeText(requireContext(), "Payment failed: " + error, Toast.LENGTH_LONG).show();
        }
        dismiss();
    }

    private void fetchCardDetails(String clientSecret) {
        new Thread(() -> {
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("client_secret", clientSecret);
                String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "guest";

                jsonBody.put("user_id", userId);

                URL url = new URL(getString(R.string.backend_url) + "/api/payments/payment-method-details/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode == HttpURLConnection.HTTP_OK)
                        ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                if (responseCode == HttpURLConnection.HTTP_OK && !jsonResponse.has("error")) {
                    String last4 = jsonResponse.optString("last4", "");
                    String expiry = jsonResponse.optString("expiry", "");
                    String holderName = jsonResponse.optString("holder_name", "");

                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (listener != null) {
                                listener.onPaymentSuccess(last4, expiry, holderName);
                            }
                            dismissAllowingStateLoss();
                        });
                    }

                } else {
                    String errorMessage = jsonResponse.optString("error", "Unknown error");
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to get card details: " + errorMessage, Toast.LENGTH_LONG).show();
                            dismissAllowingStateLoss();
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        dismissAllowingStateLoss();
                    });
                }
            }
        }).start();
    }

}
