package hu.petrik.peoplerestclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private EditText nameInput;
    private EditText emailInput;
    private EditText ageInput;
    private Button submitButton;
    private TextView peopleTextView;
    private String base_url = "https://retoolapi.dev/0xaaMh/people";

    private class RequestTask extends AsyncTask<Void, Void, Response> {
        private String requestUrl;
        private String requestMethod;
        private String requestBody;

        public RequestTask(String requestUrl) {
            this.requestUrl = requestUrl;
            this.requestMethod = "GET";
        }

        public RequestTask(String requestUrl, String requestMethod, String requestBody) {
            this.requestUrl = requestUrl;
            this.requestMethod = requestMethod;
            this.requestBody = requestBody;
        }

        public RequestTask(String requestUrl, String requestMethod) {
            this.requestUrl = requestUrl;
            this.requestMethod = requestMethod;
        }

        @Override
        protected Response doInBackground(Void... voids) {
            Response response = null;
            try {
                switch (requestMethod) {
                    case "GET":
                        response = RequestHandler.get(requestUrl);

                        break;
                    case "POST":
                        response = RequestHandler.post(requestUrl, requestBody);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            switch (requestMethod) {
                case "GET":
                    String people = response.getContent();
                    peopleTextView.setText(people);
                    break;
                case "POST":
                    if (response.getResponseCode() == 201) {
                        RequestTask task = new RequestTask(base_url);
                        task.execute();
                    }
                    break;
            }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        submitButton.setOnClickListener(view -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String age = ageInput.getText().toString().trim();
            //TODO: validate
            String json = String.format("{\"name\": \"%s\", \"email\": \"%s\", \"age\": \"%s\"}", name, email, age);
            RequestTask task = new RequestTask(base_url, "POST", json);
            task.execute();
        });
        RequestTask task = new RequestTask(base_url);
        task.execute();
    }

    private void init() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        ageInput = findViewById(R.id.ageInput);
        submitButton = findViewById(R.id.submitButton);
        peopleTextView = findViewById(R.id.textPeople);
        peopleTextView.setMovementMethod(new ScrollingMovementMethod());
    }
}