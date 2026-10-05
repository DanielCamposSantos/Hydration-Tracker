package io.github.danielcampossantos.hydrationtracker;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.danielcampossantos.hydrationtracker.model.Intake;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFERENCES_NAME = "hydration_tracker";
    private static final String KEY_INTAKE_LOGS = "intake_logs";
    private static final String KEY_LAST_ACCESS_DAY = "last_access_day";
    private static final String KEY_GOAL = "goal";
    private static final int DEFAULT_GOAL = 2000;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private SharedPreferences sharedPreferences;

    private EditText inputDailyGoal;

    private MaterialCardView goalIconContainer;
    private ImageView goalIcon;

    private MaterialButton buttonSaveGoal;
    private MaterialButton buttonCancelGoal;

    private TextView textGoalSaved;

    private BottomNavigationView bottomNavigation;

    private final Gson gson = new Gson();
    private List<Intake> intakeLogs = new ArrayList<>();
    private int totalIntake;
    private int goal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createSharedPreferences();

        findAllViews();

        setupClickListeners();

        setupBottomNavigation();


    }

    @Override
    protected void onResume() {
        super.onResume();

        checkDailyReset();
        updateAllFields();
        bottomNavigation.setSelectedItemId(R.id.navSettings);
    }

    private void createSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
    }

    private void findAllViews() {
        findGoalViews();
        findButtonViews();
        findBottomNavigationView();
    }

    private void findGoalViews() {
        inputDailyGoal = findViewById(R.id.inputDailyGoal);
        goalIconContainer = findViewById(R.id.goalIconContainer);
        goalIcon = findViewById(R.id.goalIcon);
        textGoalSaved = findViewById(R.id.textGoalSaved);
    }

    private void findButtonViews() {
        buttonSaveGoal = findViewById(R.id.buttonSaveGoal);
        buttonCancelGoal = findViewById(R.id.buttonCancelGoal);
    }

    private void findBottomNavigationView() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupClickListeners() {
        buttonSaveGoal.setOnClickListener(v -> saveGoal());
        buttonCancelGoal.setOnClickListener(v -> finish());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navWater) {
                finish();
                return true;
            }

            return itemId == R.id.navSettings;
        });
    }

    private void checkDailyReset() {
        String today = getToday();
        String lastAccessDay = sharedPreferences.getString(KEY_LAST_ACCESS_DAY, today);

        if (!today.equals(lastAccessDay)) {
            resetDailyData();
        }

        sharedPreferences.edit()
                .putString(KEY_LAST_ACCESS_DAY, today)
                .apply();
    }

    private String getToday() {
        return dateFormatter.format(new Date());
    }

    private void resetDailyData() {
        sharedPreferences.edit()
                .remove(KEY_INTAKE_LOGS)
                .apply();
    }

    private void updateAllFields() {
        updateGoalValue();
        updateLogs();
        updateAllColors(totalIntake, goal);
    }

    private void updateGoalValue() {
        String goalValue = sharedPreferences.getString(
                KEY_GOAL,
                String.valueOf(DEFAULT_GOAL)
        );

        try {
            goal = Integer.parseInt(goalValue);
        } catch (NumberFormatException exception) {
            goal = DEFAULT_GOAL;
        }

        inputDailyGoal.setText(String.valueOf(goal));
    }

    private void saveGoal() {
        String goalText = getGoalText();
        Integer newGoal = parseGoal(goalText);

        if (isInvalidGoal(newGoal)) {
            return;
        }

        goal = newGoal;

        saveGoalPreference();
        showGoalSavedFeedback();
        updateAllColors(totalIntake, goal);
    }

    private String getGoalText() {
        return inputDailyGoal.getText().toString().trim();
    }

    private Integer parseGoal(String goalText) {
        try {
            return Integer.parseInt(goalText);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isInvalidGoal(Integer goal) {
        return goal == null || goal <= 0;
    }

    private void saveGoalPreference() {
        sharedPreferences.edit()
                .putString(KEY_GOAL, String.valueOf(goal))
                .apply();
    }

    private void showGoalSavedFeedback() {
        textGoalSaved.setVisibility(View.VISIBLE);
    }

    private void updateLogs() {
        convertJsonToLogs();
        updateTotalVolume();
    }

    private void convertJsonToLogs() {
        String logs = sharedPreferences.getString(KEY_INTAKE_LOGS, "[]");

        Type listOfMyClassObject = new TypeToken<ArrayList<Intake>>() {
        }.getType();

        List<Intake> convertedLogs = gson.fromJson(logs, listOfMyClassObject);

        intakeLogs = convertedLogs != null
                ? convertedLogs
                : new ArrayList<>();
    }

    private void updateTotalVolume() {
        totalIntake = intakeLogs.stream()
                .mapToInt(Intake::getVolume)
                .sum();
    }

    private void updateAllColors(int totalIntake, int goal) {
        boolean exceeded = totalIntake > goal;

        int primaryColor = ContextCompat.getColor(this, exceeded
                ? R.color.primary_exceeded
                : R.color.primary_normal);

        int containerColor = ContextCompat.getColor(this, exceeded
                ? R.color.primary_container_exceeded
                : R.color.primary_container_normal);

        updateGoalColors(primaryColor, containerColor);
        updateMenuColors(primaryColor);


    }

    private void updateGoalColors(int primaryColor, int containerColor) {
        goalIcon.setImageTintList(ColorStateList.valueOf(primaryColor));
        goalIconContainer.setCardBackgroundColor(containerColor);

        buttonSaveGoal.setBackgroundTintList(ColorStateList.valueOf(primaryColor));

        textGoalSaved.setTextColor(primaryColor);
    }

    private void updateMenuColors(int primaryColor) {
        int inactiveColor = ContextCompat.getColor(
                this,
                R.color.navigation_inactive
        );

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };

        int[] colors = new int[]{primaryColor, inactiveColor};

        ColorStateList navigationColors = new ColorStateList(states, colors);

        bottomNavigation.setItemIconTintList(navigationColors);
        bottomNavigation.setItemTextColor(navigationColors);
    }
}