package com.huntloc.handheld;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JournalFragment extends Fragment {

    public static final String ARG_RESPONSE = "response";
    public static final String PREFS_NAME = "HandheldPrefsFile";

    TextView textView_Credential, textView_Name;
    TextView textView_DriverLicenseDate, textView_DriverLicenseType, textView_DefenseDrivingDate;
    ImageView portrait;
    ImageView imageView_DriverLicenseDate, imageView_DriverLicenseType, imageView_DefenseDrivingDate;
    String credentialId;
    Button buttonEntrance, buttonExit;
    private String response;
    private WeakReference<JournalOperation> journalOperationWeakReference;
    private OnJournalFragmentInteractionListener mListener;
    JournalOperation journalOperation = null;
    public JournalFragment() {
        // Required empty public constructor
    }

    public static JournalFragment newInstance(String response) {
        JournalFragment fragment = new JournalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESPONSE, response);
        fragment.setArguments(args);

        return fragment;
    }

    private Date parseString(String date) {
        String value = date.replaceFirst("\\D+([^\\)]+).+", "$1");
        String[] timeComponents = value.split("[\\-\\+]");
        long time = Long.parseLong(timeComponents[0]);
           /* int timeZoneOffset = Integer.valueOf(timeComponents[1]) * 36000;
	        if(value.indexOf("-") > 0){
	            timeZoneOffset *= -1;
	        }
	        time += timeZoneOffset;*/
        //return new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            response = getArguments().getString(ARG_RESPONSE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_journal_cel,
                container, false);
        textView_Credential = (TextView) view.findViewById(R.id.textView_Credential);
        textView_Name = (TextView) view.findViewById(R.id.textView_Name);
        textView_DriverLicenseDate = (TextView) view
                .findViewById(R.id.textView_DriverLicenseDate);
        textView_DriverLicenseType = (TextView) view
                .findViewById(R.id.textView_DriverLicenseType);
        textView_DefenseDrivingDate = (TextView) view
                .findViewById(R.id.textView_DefenseDrivingDate);
        imageView_DriverLicenseDate = (ImageView) view
                .findViewById(R.id.imageView_DriverLicenseDate);
        imageView_DriverLicenseType = (ImageView) view
                .findViewById(R.id.imageView_DriverLicenseType);
        imageView_DefenseDrivingDate = (ImageView) view
                .findViewById(R.id.imageView_DefenseDrivingDate);
        portrait = (ImageView) view
                .findViewById(R.id.imageView_Portrait);
        journalOperation = new JournalOperation(this);
        this.journalOperationWeakReference = new WeakReference<JournalOperation>(
                journalOperation);
        buttonEntrance = (Button) view
                .findViewById(R.id.button_Entrance);
        buttonEntrance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String log = JournalFragment.this.getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("logEntry_id", "EntryMainGate");
                String serverURL = getResources().getString(
                        R.string.service_url)
                        + "/Journal/PostLog/"
                        + credentialId
                        + "/"
                        + log;
                Log.d("buttonEntrance", serverURL);
                journalOperation.execute(serverURL);
            }
        });
        buttonExit = (Button) view.findViewById(R.id.button_Exit);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String log = JournalFragment.this.getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0).getString("logExit_id", "ExitMainGate");
                String serverURL = getResources().getString(
                        R.string.service_url)
                        + "/Journal/PostLog/"
                        + credentialId
                        + "/"
                        + log;
                Log.d("buttonExit", serverURL);
                journalOperation.execute(serverURL);
            }
        });

        String response = getArguments().getString(ARG_RESPONSE);
        byte[] byteArray;
        Bitmap bitmap;
        try {
            String outputData, outputData1 = "";
            JSONObject jsonResponse = new JSONObject(response);
            credentialId = jsonResponse.optString("CardID");
            textView_Credential.setText("Credencial: " + jsonResponse.optString("CardID"));
            textView_Name.setText("Nombre: " + jsonResponse.optString("Name"));

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.HOUR_OF_DAY, 0);
            Calendar monthAhead = Calendar.getInstance();
            monthAhead.add(Calendar.MONTH, 1);
            monthAhead.add(Calendar.DATE, 1);
            monthAhead.set(Calendar.HOUR, 0);
            monthAhead.set(Calendar.MINUTE, 0);
            monthAhead.set(Calendar.SECOND, 0);
            monthAhead.set(Calendar.HOUR_OF_DAY, 0);

            SimpleDateFormat newDateFormat = new SimpleDateFormat("d MMMM yyyy");

            if (!jsonResponse.isNull("DriverLicenseDate")) {
                Date DriverLicenseDate = parseString(jsonResponse.optString("DriverLicenseDate"));
                textView_DriverLicenseDate.setText("Licencia de conducir: " + newDateFormat.format(DriverLicenseDate) + " ");

                Calendar DriverLicenseCalendar = Calendar.getInstance();
                DriverLicenseCalendar.setTime(DriverLicenseDate);
                DriverLicenseCalendar.add(Calendar.DATE, 1);

                if (DriverLicenseCalendar.getTime().before(today.getTime())) {
                    imageView_DriverLicenseDate.setImageResource(R.drawable.ic_report);
                    imageView_DriverLicenseDate.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.error));
                } else {
                    if (DriverLicenseCalendar.getTime().before(monthAhead.getTime())) {// a un mes
                        imageView_DriverLicenseDate.setImageResource(R.drawable.ic_warning);
                        imageView_DriverLicenseDate.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.warning));
                    } else {
                        imageView_DriverLicenseDate.setImageResource(R.drawable.ic_verified);
                        imageView_DriverLicenseDate.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.check));
                    }
                }
                if (!jsonResponse.isNull("DriverLicenseType")) {
                    textView_DriverLicenseType.setText("Tipo de licencia: " + jsonResponse.optString("DriverLicenseType"));
                    imageView_DriverLicenseType.setImageResource(R.drawable.ic_info);
                    imageView_DriverLicenseType.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.warning));
                }
            }


            if (!jsonResponse.isNull("DefenseDrivingDate")) {

                Date DefenseDrivingDate = parseString(jsonResponse.optString("DefenseDrivingDate"));
                textView_DefenseDrivingDate.setText("Manejo defensivo: " + newDateFormat.format(DefenseDrivingDate) + " ");

                Calendar DefenseDrivingCalendar = Calendar.getInstance();
                DefenseDrivingCalendar.setTime(DefenseDrivingDate);
                DefenseDrivingCalendar.add(Calendar.DATE, 1);

                if (DefenseDrivingCalendar.getTime().before(Calendar.getInstance().getTime())) {
                    imageView_DefenseDrivingDate.setImageResource(R.drawable.ic_report);
                    imageView_DefenseDrivingDate.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.error));
                } else {
                    if (DefenseDrivingCalendar.getTime().before(monthAhead.getTime())) {// a un mes
                        imageView_DefenseDrivingDate.setImageResource(R.drawable.ic_warning);
                        imageView_DefenseDrivingDate.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.warning));
                    } else {
                        imageView_DefenseDrivingDate.setImageResource(R.drawable.ic_verified);
                        imageView_DefenseDrivingDate.setColorFilter(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.check));
                    }
                }
            }

            byteArray = Base64
                    .decode(jsonResponse.optString("Portrait"), 0);
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                    byteArray.length);
            portrait.setImageBitmap(bitmap);

        } catch (Exception ex) {
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
       super.onAttach(context);
        if (context instanceof OnJournalFragmentInteractionListener) {
            mListener = (OnJournalFragmentInteractionListener) context;
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

    public interface OnJournalFragmentInteractionListener {
        void onJournalFragmentInteraction();
    }

    private static class JournalOperation extends
            AsyncTask<String, String, String> {


        private WeakReference<JournalFragment> journalFragmentWeakReference;
        HttpURLConnection urlConnection;
        private JournalOperation(JournalFragment fragment) {
            this.journalFragmentWeakReference = new WeakReference<JournalFragment>(
                    fragment);
        }

        @SuppressWarnings("unchecked")
        protected String doInBackground(String... args) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(args[0]);
                Log.d("Journal URL", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {

            try {
                JSONObject jsonResponse = new JSONObject(result);

                String log = jsonResponse.optString("log")
                        .contains("Entry") ? "Entrada" : "Salida";
                String response = jsonResponse.optString("records") + " " + log
                        + " Registrada";

                Toast.makeText(
                        journalFragmentWeakReference.get()
                                .getActivity(), response, Toast.LENGTH_LONG)
                        .show();

                NavUtils.navigateUpFromSameTask(journalFragmentWeakReference
                        .get().getActivity());

					/*
					 * Intent intent = new
					 * Intent(journalSectionFragmentWeakReference
					 * .get().getActivity(), MainActivity.class);
					 * journalSectionFragmentWeakReference
					 * .get().getActivity().startActivity(intent);
					 */
            } catch (JSONException e) {
            }

        }
    }
}
