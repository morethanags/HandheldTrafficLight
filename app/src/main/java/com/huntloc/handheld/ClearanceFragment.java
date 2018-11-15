package com.huntloc.handheld;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class ClearanceFragment extends Fragment {

    private OnClearanceFragmentInteractionListener mListener;
    public static final String ARG_CREDENTIALID = "credentialId";
    private WeakReference<ClearanceOperation> clearanceOperationWeakReference;
    private String credential;

    public ClearanceFragment() {
        // Required empty public constructor
    }

    public static ClearanceFragment newInstance(String credential) {
        ClearanceFragment fragment = new ClearanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CREDENTIALID, credential);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            credential = getArguments().getString(ARG_CREDENTIALID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clearance,
                container, false);

        String credentialId = getArguments().getString(ARG_CREDENTIALID);
        String serverURL = getResources().getString(R.string.service_url)
                + "/Access/GetClearance/" + credentialId;
        Log.d("Clearance URL", serverURL);
        ClearanceOperation clearanceOperation = new ClearanceOperation(this);
        this.clearanceOperationWeakReference = new WeakReference<ClearanceOperation>(
                clearanceOperation);
        clearanceOperation.execute(serverURL);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClearanceFragmentInteractionListener) {
            mListener = (OnClearanceFragmentInteractionListener) context;
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

    public interface OnClearanceFragmentInteractionListener {
        void onClearanceFragmentInteraction();
    }

    private static class ClearanceOperation extends
            AsyncTask<String, String, String> {

        String response;
        String[] values = null;
        ListView clearanceList = null;
        private WeakReference<ClearanceFragment> clearanceFragmentWeakReference;
        HttpURLConnection urlConnection;

        private ClearanceOperation(ClearanceFragment fragment) {
            this.clearanceFragmentWeakReference = new WeakReference<ClearanceFragment>(
                    fragment);

        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                Log.d("Clearance URL", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {

            } finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {
            try {
                clearanceList = (ListView) clearanceFragmentWeakReference
                        .get().getView().findViewById(R.id.list_Clearance);

                JSONObject jsonResponse = new JSONObject(result);
                JSONArray jsonArray = jsonResponse.getJSONArray("values");
                values = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    values[i] = jsonArray.getJSONObject(i)
                            .optString("Name");

                    response += jsonArray.getJSONObject(i)
                            .optString("Name") + "\r\n";
                }

                clearanceFragmentWeakReference.get().getActivity()
                        .runOnUiThread(new Runnable() {
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                        clearanceFragmentWeakReference
                                                .get().getActivity(),
                                        android.R.layout.simple_list_item_1,
                                        android.R.id.text1, values);
                                clearanceList.setAdapter(adapter);

                            }
                        });
            } catch (Exception e) {

            }

        }
    }
}
