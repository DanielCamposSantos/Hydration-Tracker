package io.github.danielcampossantos.hydrationtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.danielcampossantos.hydrationtracker.model.Intake;

public class MainActivity extends AppCompatActivity {

    public static final String INTAKE_LOGS = "intake_logs";
    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private SharedPreferences sharedPreferences;

    private EditText inputVolume;
    private LinearLayout logsContainer;
    private LinearLayout totalSummaryContainer;

    private TextView textEmptyLog;
    private MaterialButton goalButton;
    private TextView textLastRecord;
    private TextView textLastRecordTime;
    private TextView textTotalIntake;
    private TextView textDailyGoal;

    private Button button150;
    private Button button250;
    private Button button350;
    private Button button500;
    private Button buttonAddIntake;

    private BottomNavigationView bottomNavigation;

    private final Gson gson = new Gson();
    private List<Intake> intakeLogs = new ArrayList<>();
    private int totalIntake;
    private int goal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createSharedPreference();

        findAllViews();

        setQuickButtonsClickListeners(button150, button250, button350, button500);


        buttonAddIntake.setOnClickListener(v -> addIntake());


        setupBottomNavigation();


    }

    @Override
    protected void onResume() {
        super.onResume();

        setDailyCleaner();
        updateAllFields();
        bottomNavigation.setSelectedItemId(R.id.navWater);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navWater);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navWater) {
                return true;
            }

            if (itemId == R.id.navSettings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void setDailyCleaner() {
        String today = getToday();
        String lastAccessDay = sharedPreferences.getString("last_access_day", today);

        if (!today.equals(lastAccessDay)) {
            resetDailyData();
        }

        sharedPreferences.edit()
                .putString("last_access_day", today)
                .apply();
    }

    private String getToday() {
        return dateFormater.format(new Date());
    }

    private void resetDailyData() {
        sharedPreferences.edit()
                .remove(INTAKE_LOGS)
                .apply();

        intakeLogs.clear();
        totalIntake = 0;
    }

    private void createSharedPreference() {
        sharedPreferences = getSharedPreferences("hydration_tracker", MODE_PRIVATE);
    }

    private void findAllViews() {
        findDataViews();
        findAddingIntakeViews();
        findLogsViews();
        findBottomNavegationView();
    }

    private void findDataViews() {
        textLastRecord = findViewById(R.id.textLastRecord);
        textLastRecordTime = findViewById(R.id.textLastRecordTime);
        textTotalIntake = findViewById(R.id.textTotalIntake);
        textDailyGoal = findViewById(R.id.textDailyGoal);
        totalSummaryContainer = findViewById(R.id.totalSummaryContainer);
        goalButton = findViewById(R.id.buttonGoal);
    }

    private void findAddingIntakeViews() {
        button150 = findViewById(R.id.button150);
        button250 = findViewById(R.id.button250);
        button350 = findViewById(R.id.button350);
        button500 = findViewById(R.id.button500);
        inputVolume = findViewById(R.id.inputVolume);
        buttonAddIntake = findViewById(R.id.buttonAddIntake);
    }

    private void findLogsViews() {
        logsContainer = findViewById(R.id.logsContainer);
        textEmptyLog = findViewById(R.id.textEmptyLog);
    }

    private void findBottomNavegationView() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void updateAllFields() {
        updateLogs();
    }


    private void updateTextValues() {
        if (!intakeLogs.isEmpty()) {
            textLastRecord.setText(String.valueOf(getLatestIntake().getVolume()));
            textLastRecordTime.setText(formatDateToValidString(getLatestIntake()));
        }

        textTotalIntake.setText(String.valueOf(totalIntake));

        updateGoalValue();
    }

    private void updateGoalValue() {
        String goalValue = sharedPreferences.getString("goal", "2000");
        goal = Integer.parseInt(goalValue);
        textDailyGoal.setText(goalValue);
        this.goalButton.setText(goalValue);
    }

    private void updateAllColors(int totalIntake, int goal) {
        boolean exceeded = totalIntake > goal;

        int primaryColor = ContextCompat.getColor(this, exceeded
                ? R.color.primary_exceeded
                : R.color.primary_normal);

        int containerColor = ContextCompat.getColor(this, exceeded
                ? R.color.primary_container_exceeded
                : R.color.primary_container_normal);

        int summaryContainerColor = ContextCompat.getColor(this, exceeded
                ? R.color.primary_container_exceeded
                : R.color.summary_container_normal);

        updateInputColors(primaryColor, containerColor);
        updateTextColors(primaryColor, containerColor, summaryContainerColor);
        updateLogColors(primaryColor);
        updateMenuColors(primaryColor);


    }

    private void updateInputColors(int primaryColor, int containerColor) {
        buttonAddIntake.setBackgroundTintList(ColorStateList.valueOf(primaryColor));

        button150.setBackgroundTintList(ColorStateList.valueOf(containerColor));
        button250.setBackgroundTintList(ColorStateList.valueOf(containerColor));
        button350.setBackgroundTintList(ColorStateList.valueOf(containerColor));
        button500.setBackgroundTintList(ColorStateList.valueOf(containerColor));
    }

    private void updateTextColors(int primaryColor, int containerColor, int summaryContainerColor) {
        goalButton.setIconTint(ColorStateList.valueOf(primaryColor));
        goalButton.setBackgroundTintList(ColorStateList.valueOf(containerColor));

        totalSummaryContainer.setBackgroundTintList(ColorStateList.valueOf(summaryContainerColor));

        updateTextViewsColor(primaryColor);
    }

    private void updateTextViewsColor(int primaryColor) {
        for (int i = 0; i < totalSummaryContainer.getChildCount(); i++) {
            View view = totalSummaryContainer.getChildAt(i);

            if (view instanceof TextView) {
                ((TextView) view).setTextColor(primaryColor);
            }
        }
    }

    private void updateLogColors(int primaryColor) {
        for (int i = 0; i < logsContainer.getChildCount(); i++) {
            View row = logsContainer.getChildAt(i);

            TextView textVolume = row.findViewById(R.id.textIntakeVolume);

            if (textVolume != null) {
                textVolume.setTextColor(primaryColor);
            }

            updateLogIcons(row, primaryColor);
        }
    }

    private void updateLogIcons(View view, int primaryColor) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageTintList(ColorStateList.valueOf(primaryColor));
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                updateLogIcons(viewGroup.getChildAt(i), primaryColor);
            }
        }
    }

    private void updateMenuColors(int primaryColor) {
        int inactiveColor = ContextCompat.getColor(this, R.color.navigation_inactive);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };

        int[] colors = new int[]{primaryColor, inactiveColor};

        ColorStateList navigationColors = new ColorStateList(states, colors);

        bottomNavigation.setItemIconTintList(navigationColors);
        bottomNavigation.setItemTextColor(navigationColors);
    }

    private void addIntake() {
        String volumeText = getVolumeText();

        if (isInvalidVolume(volumeText)) {
            Toast.makeText(this, "Enter a valid volume", Toast.LENGTH_SHORT).show();
            return;
        }

        int volume = parseVolume(volumeText);

        addIntakeToLogs(volume);
        saveIntakeLogs();
        loadLogs();
    }

    private String getVolumeText() {
        return inputVolume.getText().toString().trim();
    }

    private boolean isInvalidVolume(String volumeText) {
        if (volumeText.isEmpty()) {
            return true;
        }

        return parseVolume(volumeText) <= 0;
    }

    private int parseVolume(String volumeText) {
        return Integer.parseInt(volumeText);
    }

    private void addIntakeToLogs(int volume) {
        Intake intake = new Intake(volume, System.currentTimeMillis());
        intakeLogs.add(intake);
    }

    private void saveIntakeLogs() {
        String json = gson.toJson(intakeLogs);

        sharedPreferences.edit()
                .putString(INTAKE_LOGS, json)
                .apply();
    }

    private void updateLogs() {
        convertJsonToLogs();
        loadLogs();
    }

    private void convertJsonToLogs() {
        String logs = sharedPreferences.getString(INTAKE_LOGS, "[]");

        Type listOfMyClassObject = new TypeToken<ArrayList<Intake>>() {
        }.getType();

        intakeLogs = gson.fromJson(logs, listOfMyClassObject);
    }

    private void loadLogs() {
        logsContainer.removeAllViews();

        setTextEmptyLogVisibility();
        useReversedListIntoLogs();

        updateTotalVolume();
        updateTextValues();
        updateAllColors(totalIntake, goal);


    }

    private void useReversedListIntoLogs() {
        for (int i = intakeLogs.size() - 1; i >= 0; i--) {
            Intake intake = intakeLogs.get(i);
            View row = getLayoutInflater().inflate(R.layout.item_intake, logsContainer, false);
            TextView textTime = row.findViewById(R.id.textIntakeTime);
            TextView textVolume = row.findViewById(R.id.textIntakeVolume);
            textVolume.setText(getString(R.string.added_volume_format, intake.getVolume()));
            String time = formatDateToValidString(intake);
            textTime.setText(time);
            logsContainer.addView(row);
        }
    }

    private void updateTotalVolume() {
        totalIntake = intakeLogs.stream()
                .map(Intake::getVolume)
                .reduce(0, Integer::sum);
    }

    private void setTextEmptyLogVisibility() {
        if (intakeLogs.isEmpty()) {
            logsContainer.setVisibility(View.GONE);
            textEmptyLog.setVisibility(View.VISIBLE);
            return;
        }

        logsContainer.setVisibility(View.VISIBLE);
        textEmptyLog.setVisibility(View.GONE);
    }

    private void setQuickButtonsClickListeners(Button button150, Button button250, Button button350,
                                               Button button500) {
        button150.setOnClickListener(v -> setVolumeToAdd(150));
        button250.setOnClickListener(v -> setVolumeToAdd(250));
        button350.setOnClickListener(v -> setVolumeToAdd(350));
        button500.setOnClickListener(v -> setVolumeToAdd(500));
    }

    private void setVolumeToAdd(int volume) {
        inputVolume.setText(String.valueOf(volume));
    }

    private Intake getLatestIntake() {
        return intakeLogs.get(intakeLogs.size() - 1);
    }

    private String formatDateToValidString(Intake intake) {
        return timeFormatter.format(new Date(intake.getTimestamp()));
    }
}