package ua.napps.scorekeeper.app;

import static ua.napps.scorekeeper.settings.LocalSettings.THEME_DARK;
import static ua.napps.scorekeeper.settings.LocalSettings.THEME_LIGHT;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.platform.MaterialFadeThrough;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersFragment;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.dice.OnDiceFragmentInteractionListener;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.utils.RateMyAppDialog;
import ua.napps.scorekeeper.utils.ViewUtil;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnDiceFragmentInteractionListener {

    private static final String TAG_DICES_FRAGMENT = "DICES_FRAGMENT";
    private static final String TAG_COUNTERS_FRAGMENT = "COUNTERS_FRAGMENT";
    private static final String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    private static final String STATE_CURRENT_DICE_ROLL = "STATE_CURRENT_DICE_ROLL";
    private static final String[] TAGS = new String[]{TAG_COUNTERS_FRAGMENT, TAG_DICES_FRAGMENT, TAG_SETTINGS_FRAGMENT};
    private RateMyAppDialog rateMyAppDialog;
    private String currentFragmentTag;
    private FragmentManager manager;
    private int currentDiceRoll;
    private int previousDiceRoll;
    private boolean isKeepScreenOn;
    private BottomNavigationView bottomNavigationBar;

    // Time intervals
    private static final long DOUBLE_PRESS_INTERVAL = 300; // Max time in ms for double click
    private static final long LONG_PRESS_THRESHOLD = 500;  // Min time in ms for long press

    // Handler and flags
    private Handler handler;
    private long lastKeyDownTime = 0;
    private boolean isDoubleClick = false;
    private boolean isSingleClickPending = false;
    private boolean isLongPress = false;

    // Runnable actions
    private Runnable singleClickRunnable;
    private Runnable longPressRunnable;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // If already long pressed, ignore further actions
            if (isLongPress) return true;

            lastKeyDownTime = System.currentTimeMillis();
            startLongPressDetection(keyCode);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Cancel long press detection
            handler.removeCallbacks(longPressRunnable);

            if (isLongPress) {
                // Reset long press state and exit early if long press was detected
                isLongPress = false;
                return true;
            }

            long keyUpTime = System.currentTimeMillis();
            long pressDuration = keyUpTime - lastKeyDownTime;

            // If it's a short press, check for double click or single click
            if (pressDuration < LONG_PRESS_THRESHOLD) {
                handleClick(keyCode);
            }

            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void handleClick(int keyCode) {
        if (isSingleClickPending) {
            // Double click detected
            handler.removeCallbacks(singleClickRunnable); // Cancel pending single click
            isSingleClickPending = false;
            isDoubleClick = true;

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                onVolumeDownDoubleClick();
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                onVolumeUpDoubleClick();
            }
        } else {
            // Start waiting for a possible double click
            isSingleClickPending = true;
            isDoubleClick = false;

            singleClickRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isSingleClickPending && !isDoubleClick) {
                        // Single click confirmed
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            onVolumeDownSingleClick();
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            onVolumeUpSingleClick();
                        }
                    }
                    isSingleClickPending = false; // Reset flag after action
                }
            };
            handler.postDelayed(singleClickRunnable, DOUBLE_PRESS_INTERVAL); // Wait to confirm single click
        }
    }

    private void startLongPressDetection(int keyCode) {
        // Long press detection logic
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                isLongPress = true;
                isSingleClickPending = false; // Cancel pending single click
                handler.removeCallbacks(singleClickRunnable); // Cancel single click
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    onVolumeDownLongPress();
                } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    onVolumeUpLongPress();
                }
            }
        };
        handler.postDelayed(longPressRunnable, LONG_PRESS_THRESHOLD); // Schedule long press detection
    }

    private void onVolumeDownSingleClick() {
        // Add logic for Volume Down single click
        CountersFragment counterFragment = getCounterFragment();
        if (counterFragment != null) {
            counterFragment.increaseCounter(1);
        }
    }

    private void onVolumeDownDoubleClick() {
        // Add logic for Volume Down double click
        CountersFragment counterFragment = getCounterFragment();
        if (counterFragment != null) {
            counterFragment.decreaseCounter(1);
        }
    }

    private void onVolumeUpSingleClick() {
        // Add logic for Volume Up single click
        CountersFragment counterFragment = getCounterFragment();
        if (counterFragment != null) {
            counterFragment.increaseCounter(0);
        }
    }

    private void onVolumeUpDoubleClick() {
        // Add logic for Volume Up double click
        CountersFragment counterFragment = getCounterFragment();
        if (counterFragment != null) {
            counterFragment.decreaseCounter(0);
        }
    }

    private void onVolumeDownLongPress() {
        // Add logic for Volume Down long press
        CountersFragment counterFragment = getCounterFragment();
        if (counterFragment != null) {
            counterFragment.resetAllCounters();
        }
    }

    private void onVolumeUpLongPress() {
        // Add logic for Volume Up long press
        CountersFragment counterFragment = getCounterFragment();
        if (counterFragment != null) {
            counterFragment.resetAllCounters();
        }
    }


    private CountersFragment getCounterFragment() {
        Fragment f = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
        if (f instanceof CountersFragment) {
            return ((CountersFragment) f);
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
        if (savedInstanceState != null) {
            currentDiceRoll = savedInstanceState.getInt(STATE_CURRENT_DICE_ROLL);
        }

        applyAppTheme();
        boolean nightModeActive = ViewUtil.isNightModeActive(this);
        ViewUtil.setLightMode(this, !nightModeActive);
        ViewUtil.setNavBarColor(this, !nightModeActive);
        setContentView(R.layout.activity_main);
        rateMyAppDialog = new RateMyAppDialog(this);
        manager = getSupportFragmentManager();

        bottomNavigationBar = findViewById(R.id.bottom_navigation);
        bottomNavigationBar.setSelectedItemId(R.id.counters);

        onRotationEnableFullScreenMode();

        bottomNavigationBar.setOnItemSelectedListener(item -> {
            // Setting orientation as Portrait to avoid messing up the UI any other tab.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            switch (item.getItemId()) {
                case R.id.counters:
                    switchFragment(TAGS[0]);
                    getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.primaryBackground));
                    ViewUtil.setLightMode(this, !nightModeActive);

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    break;
                case R.id.dices:
                    switchFragment(TAGS[1]);
                    hideDiceBadge();
                    getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.primaryBackground));
                    ViewUtil.setLightMode(this, !nightModeActive);
                    break;
                case R.id.more:
                    switchFragment(TAGS[2]);
                    hideDiceBadge();
                    getWindow().setStatusBarColor(Color.parseColor("#455a64"));
                    ViewUtil.setLightMode(this, false);
                    break;
            }

            return true;
        });

        bottomNavigationBar.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.counters) {
                Fragment f = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
                if (f instanceof CountersFragment) {
                    ((CountersFragment) f).scrollToTop();
                }
            }
        });


        switchFragment(TAGS[0]);

        applyKeepScreenOnIfNeeded();

        //Handle the case when back button is pressed. Show warning message
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNavigationBar.getSelectedItemId() == R.id.counters) {
                    finish();
                } else {
                    bottomNavigationBar.setSelectedItemId(R.id.counters);
                }
            }
        });
    }

    private void onRotationEnableFullScreenMode() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Hiding the large Bottom Bar
            bottomNavigationBar.setVisibility(View.GONE);

            // Make the app full screen in landscape mode
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        } else {
            // Showing everything back - first the large Bottom Bar
            bottomNavigationBar.setVisibility(View.VISIBLE);

            // Restore the app to its default state in portrait mode
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private void applyAppTheme() {
        int theme = LocalSettings.getDefaultTheme();
        final int nightMode;
        switch (theme) {
            case THEME_DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case THEME_LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private void hideDiceBadge() {
        BadgeDrawable badge = bottomNavigationBar.getBadge(R.id.dices);
        if (badge != null) {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_DICE_ROLL, currentDiceRoll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rateMyAppDialog.showIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        rateMyAppDialog.onStart();
        App.getTinyDB().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.getTinyDB().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void updateCurrentRoll(int number) {
        previousDiceRoll = currentDiceRoll;
        currentDiceRoll = number;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (LocalSettings.KEEP_SCREEN_ON.equals(key)) {
            isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
            applyKeepScreenOnIfNeeded();
        } else if (LocalSettings.APP_THEME_MODE.equals(key)) {
            bottomNavigationBar.setSelectedItemId(R.id.counters);
            applyAppTheme();
            getDelegate().applyDayNight();
        }
    }

    private void switchFragment(String tag) {
        if (tag.equals(currentFragmentTag)) return;
        Fragment fragment = getFragmentByTag(tag);

        fragment.setEnterTransition(createTransition());

        manager.beginTransaction()
                .replace(R.id.container, fragment, tag)
                .commit();
        currentFragmentTag = tag;
    }

    private Fragment getFragmentByTag(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) return fragment;

        switch (tag) {
            case TAG_COUNTERS_FRAGMENT:
                return CountersFragment.newInstance();
            case TAG_DICES_FRAGMENT:
                return DicesFragment.newInstance(currentDiceRoll, previousDiceRoll);
            case TAG_SETTINGS_FRAGMENT:
                return SettingsFragment.newInstance();
        }
        return null;
    }

    private void applyKeepScreenOnIfNeeded() {
        if (isKeepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private MaterialFadeThrough createTransition() {
        MaterialFadeThrough fadeThrough = new MaterialFadeThrough();

        // Add targets for this transition to explicitly run transitions only on these views. Without
        // targeting, a MaterialFadeThrough would be run for every view in the Fragment's layout.
        fadeThrough.addTarget(R.id.counters_fragment);
        fadeThrough.addTarget(R.id.dices_fragment);
        fadeThrough.addTarget(R.id.settings_fragment);

        return fadeThrough;
    }
}