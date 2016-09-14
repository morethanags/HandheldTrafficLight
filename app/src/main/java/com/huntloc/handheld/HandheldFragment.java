package com.huntloc.handheld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class HandheldFragment extends Fragment {

    private OnHandheldFragmentInteractionListener mListener;
    private EditText mCredentialId;
    private Button buttonCkeck;
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
                        //sendRequest();
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
                    //sendRequest();
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
                    + " must implement OnFragmentInteractionListener");
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
        //if (!mCredentialId.getText().toString().isEmpty()) {
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
                                        //sendRequest(printedCode);
                                    }
                                });
                    }
                }
            } catch (Exception ex) {

            }
        }
    }

}
