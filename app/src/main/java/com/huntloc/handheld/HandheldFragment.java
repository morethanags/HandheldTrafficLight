package com.huntloc.handheld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class HandheldFragment extends Fragment {

    private OnHandheldFragmentInteractionListener mListener;
    private EditText mCredentialId;
    private Button buttonCkeck;
    public static final String PERSONNEL_MESSAGE = "com.huntloc.handheld.PERSONNEL";
    public HandheldFragment() {
        // Required empty public constructor
    }

    public static HandheldFragment newInstance() {
        HandheldFragment fragment = new HandheldFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_handheld, container, false);

        mCredentialId = (EditText) view
                .findViewById(R.id.editText_CredentialId);
        mCredentialId.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if (mCredentialId.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(),
                                "Tap a Badge or Enter Credential ID",
                                Toast.LENGTH_LONG).show();
                    } else {
                        sendRequest();
                    }
                    return true;
                }
                return false;
            }
        });

        buttonCkeck = (Button) view.findViewById(R.id.button_Register);
        buttonCkeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCredentialId.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(),
                            "Tap a Badge or Enter Credential ID",
                            Toast.LENGTH_LONG).show();
                } else {
                    sendRequest();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnHandheldFragmentInteractionListener) {
            mListener = (OnHandheldFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnJournalFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnHandheldFragmentInteractionListener {
        void onHandheldFragmentInteraction();
    }
    public void setCredentialId(String id) {
        mCredentialId.setText(id);
        if (!id.isEmpty()) {
            String serverURL = getResources().getString(
                    R.string.service_url)
                    + "/SwapBadge/"
                    + id
                    + "/"
                    + UUID.randomUUID().toString();
            new QueryBadgeTask(this).execute(serverURL);
            Log.d("URL Badge", serverURL);
        }
    }
    public void clearCredentialId() {
        if(mCredentialId!=null){
            mCredentialId.setText("");
        }
    }
    private void sendRequest() {
        String serverURL = getResources().getString(R.string.service_url)
                + "/PersonnelService/" + mCredentialId.getText().toString()
                + "/" + getResources().getString(R.string.area_id) + "/"
                + UUID.randomUUID().toString();
        Log.d("URL Personnel", serverURL);
        new QueryPersonnelTask(this).execute(serverURL);
    }
    private void sendRequest(String credentialId) {
        String serverURL = getResources().getString(R.string.service_url)
                + "/PersonnelService/" + credentialId + "/"
                + getResources().getString(R.string.area_id) + "/"
                + UUID.randomUUID().toString();
        Log.d("URL Personnel", serverURL);
        new QueryPersonnelTask(this).execute(serverURL);
    }

    private class QueryBadgeTask extends AsyncTask<String, String, String> {
        String printedCode = "";
        HttpURLConnection urlConnection;
        private WeakReference<HandheldFragment> handheldFragmentWeakReference;

        private QueryBadgeTask(HandheldFragment fragment) {
            this.handheldFragmentWeakReference = new WeakReference<HandheldFragment>(
                    fragment);
        }

        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                handheldFragmentWeakReference.get().getActivity()
                        .runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                                alertDialogBuilder.setTitle("Handheld");
                                alertDialogBuilder.setMessage("Red WiFi no Disponible");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                alertDialogBuilder.create().show();
                            }
                        });
            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {
            try {
                if (!result.equals("")) {
                    JSONObject jsonResponse = new JSONObject(result);
                    printedCode = jsonResponse.optString("PrintedCode");
                    if (printedCode.equals("null")) {
                        handheldFragmentWeakReference.get().getActivity()
                                .runOnUiThread(new Runnable() {
                                    public void run() {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                                        alertDialogBuilder.setTitle("Handheld");
                                        alertDialogBuilder.setMessage("Credencial no Válida");
                                        alertDialogBuilder.setCancelable(false);
                                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                        alertDialogBuilder.create().show();
                                    }
                                });
                        return;
                    } else {
                        handheldFragmentWeakReference.get().getActivity()
                                .runOnUiThread(new Runnable() {
                                    public void run() {
                                        EditText outputText = (EditText) handheldFragmentWeakReference.get()
                                                .getView().findViewById(R.id.editText_CredentialId);
                                        outputText.setText(printedCode);
                                        Log.d("Printed Code",printedCode);
                                        sendRequest(printedCode);
                                    }
                                });
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

    private class QueryPersonnelTask extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;
        String result = "";
        private WeakReference<HandheldFragment> handheldFragmentWeakReference;

        private QueryPersonnelTask(HandheldFragment fragment) {
            this.handheldFragmentWeakReference = new WeakReference<HandheldFragment>(fragment);
        }

        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                handheldFragmentWeakReference.get().getActivity()
                        .runOnUiThread(new Runnable() {
                            public void run() {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                                alertDialogBuilder.setTitle("Handheld");
                                alertDialogBuilder.setMessage("Red WiFi no Disponible");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                                alertDialogBuilder.create().show();
                            }
                        });
            }
            finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        private Date parseString(String date) {
            String value = date.replaceFirst("\\D+([^\\)]+).+", "$1");
            String[] timeComponents = value.split("[\\-\\+]");
            long time = Long.parseLong(timeComponents[0]);
		        /*int timeZoneOffset = Integer.valueOf(timeComponents[1]) * 36000;
		        if(value.indexOf("-") > 0){
		            timeZoneOffset *= -1;
		        }
		        time += timeZoneOffset;*/
            //return new Date(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            //calendar.set(Calendar.HOUR, 0);
            //calendar.set(Calendar.MINUTE, 0);
            //calendar.set(Calendar.SECOND, 0);
            //calendar.set(Calendar.HOUR_OF_DAY, 0);
            return calendar.getTime();
        }

        protected void onPostExecute(String result) {
            try {
                boolean ready = true;
                if (!result.equals("")) {
                    this.result = result;
                    JSONObject jsonResponse = new JSONObject(result);
                    if(jsonResponse.isNull("CardID")) {
                        ready = false;
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                        alertDialogBuilder.setTitle("Handheld");
                        alertDialogBuilder.setMessage("Credencial No Tiene Acceso");
                        alertDialogBuilder.setCancelable(false);
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                        alertDialogBuilder.create().show();
                    }

                    if (!jsonResponse.isNull("ExpirationDate")){
                        Date ExpirationDate = parseString(jsonResponse.optString("ExpirationDate"));

                        SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMMM yyyy h:mm a");
                        SimpleDateFormat newDateFormat1 = new SimpleDateFormat("d MMMM yyyy");
                        Calendar today = Calendar.getInstance();
                        //today.set(Calendar.HOUR, 0);
                        //today.set(Calendar.MINUTE, 0);
                        //today.set(Calendar.SECOND, 0);
                        //today.set(Calendar.HOUR_OF_DAY, 0);
                        Log.d("Today", newDateFormat.format(today.getTime()));
                        Log.d("Expiration Date", newDateFormat.format(ExpirationDate));
                        if (ExpirationDate.before(today.getTime())) {//comparamos fechas y hora exactas
                            ready = false;
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                            alertDialogBuilder.setTitle("Handheld");
                            alertDialogBuilder.setMessage("Credencial vencida\n"+
                                    newDateFormat.format(ExpirationDate));
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                            alertDialogBuilder.create().show();
                        }
                        else {
                            if (!jsonResponse.isNull("CAMODate")) {
                                Date CAMODate = parseString(jsonResponse.optString("CAMODate"));

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(CAMODate);
                                calendar.add(Calendar.DATE, 1);
                                calendar.set(Calendar.HOUR, 0);
                                calendar.set(Calendar.MINUTE, 0);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.HOUR_OF_DAY, 0);

                                Log.d("CAMO Date", newDateFormat1.format(CAMODate));

                                if (calendar.getTime().before(today.getTime())) {

                                    ready = false;
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(handheldFragmentWeakReference.get().getActivity());
                                    alertDialogBuilder.setTitle("Handheld");
                                    alertDialogBuilder.setMessage("Certificado de aptitud médico ocupacional vencido\n"+
                                            newDateFormat1.format(CAMODate));
                                    alertDialogBuilder.setCancelable(false);
                                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                                    alertDialogBuilder.create().show();
                                }
                            }
                        }
                    }

                }
                else {
                    ready = false;
                }

                if (ready) {
                    handheldFragmentWeakReference.get().getActivity()
                            .runOnUiThread(new Runnable() {
                                public void run() {
                                    Intent intent = new Intent(
                                            handheldFragmentWeakReference
                                                    .get().getActivity(),
                                            PersonnelActivity.class);
                                    intent.putExtra(PERSONNEL_MESSAGE, QueryPersonnelTask.this.result);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(intent);
                                    handheldFragmentWeakReference.get().clearCredentialId();
                                }
                            });
                }
                else {
                    handheldFragmentWeakReference.get().clearCredentialId();
                    return;
                }
            }
            catch (JSONException ex) {
                Log.d("JSONException", ex.getMessage());
            }
        }
    }
}
