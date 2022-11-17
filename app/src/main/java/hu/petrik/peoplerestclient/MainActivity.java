package hu.petrik.peoplerestclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText nameInput;
    private EditText emailInput;
    private EditText ageInput;
    private Button submitButton;
    private Button updateButton;
    private Button cancelButton;
    private Button createButton;
    private LinearLayout personFrom;
    private ListView peopleListView;
    private String base_url = "https://retoolapi.dev/0xaaMh/people";
    private int updateId;
    private ProgressBar progressBar;

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
                    case "PUT":
                        response = RequestHandler.put(requestUrl, requestBody);
                        break;
                    case "DELETE":
                        response = RequestHandler.delete(requestUrl);
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
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);
            if (response == null) {
                Toast.makeText(MainActivity.this, R.string.unable_to_connect, Toast.LENGTH_SHORT).show();
                return;
            }
            if (response.getResponseCode() >= 400) {
                Toast.makeText(MainActivity.this, response.getContent(), Toast.LENGTH_SHORT).show();
                return;
            }
            switch (requestMethod) {
                case "GET":
                    String content = response.getContent();
                    Gson converter = new Gson();
                    List<Person> people = Arrays.asList(converter.fromJson(content, Person[].class));
                    PeopleAdapter adapter = new PeopleAdapter(people);
                    peopleListView.setAdapter(adapter);
                    break;
                default:
                    if (response.getResponseCode() >= 201 && response.getResponseCode() < 300) {
                        cancelForm();
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
        addListeners();

    }

    private void addListeners() {
        submitButton.setOnClickListener(view -> {
            try {
                String json = createJsonFromFormdata();
                RequestTask task = new RequestTask(base_url, "POST", json);
                task.execute();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        updateButton.setOnClickListener(view -> {
            try {
                String json = createJsonFromFormdata();
                String url = base_url + "/" + updateId;
                RequestTask task = new RequestTask(base_url, "PUT", json);
                task.execute();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        cancelButton.setOnClickListener(view -> {
            cancelForm();
        });

        createButton.setOnClickListener(view -> {
            personFrom.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
            updateButton.setVisibility(View.GONE);
            createButton.setVisibility(View.GONE);
        });
    }

    private String createJsonFromFormdata() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String ageText = ageInput.getText().toString().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (ageText.isEmpty()) {
            throw new IllegalArgumentException("Age is required");
        }
        int age = Integer.parseInt(ageText);
        Person person = new Person(0, name, email, age);
        Gson converter = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return converter.toJson(person);
    }

    private void cancelForm() {
        nameInput.setText("");
        updateId = 0;
        emailInput.setText("");
        ageInput.setText("");
        personFrom.setVisibility(View.GONE);
        createButton.setVisibility(View.VISIBLE);
    }

    private void init() {
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        ageInput = findViewById(R.id.ageInput);
        submitButton = findViewById(R.id.submitButton);
        createButton = findViewById(R.id.createButton);
        updateButton = findViewById(R.id.updateButton);
        cancelButton = findViewById(R.id.cancelButton);
        personFrom = findViewById(R.id.personForm);
        peopleListView = findViewById(R.id.peopleListView);
        progressBar = findViewById(R.id.progressBar);
        // TextView peopleTextView = findViewById(R.id.textPeople);
        // peopleTextView.setMovementMethod(new ScrollingMovementMethod());
    }

    private class PeopleAdapter extends ArrayAdapter<Person> {
        private List<Person> people;

        public PeopleAdapter(List<Person> objects) {
            super(MainActivity.this, R.layout.person_list_item, objects);
            people = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.person_list_item, null);
            Person actualPerson = people.get(position);
            TextView display = view.findViewById(R.id.display);
            TextView update = view.findViewById(R.id.update);
            TextView delete = view.findViewById(R.id.delete);
            display.setText(actualPerson.toString());
            update.setOnClickListener(v -> {
                updateId = actualPerson.getId();
                nameInput.setText(actualPerson.getName());
                emailInput.setText(actualPerson.getEmail());
                ageInput.setText(String.valueOf(actualPerson.getAge()));

                personFrom.setVisibility(View.VISIBLE);
                submitButton.setVisibility(View.GONE);
                updateButton.setVisibility(View.VISIBLE);
                createButton.setVisibility(View.GONE);
            });
            delete.setOnClickListener(v -> {
                String url = base_url + "/" + actualPerson.getId();
                RequestTask task = new RequestTask(url, "DELETE");
                task.execute();
            });
            return view;
        }
    }
}