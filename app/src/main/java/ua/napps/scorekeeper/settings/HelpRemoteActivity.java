package ua.napps.scorekeeper.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.DonateDialog;
import ua.napps.scorekeeper.utils.ViewUtil;

public class HelpRemoteActivity extends AppCompatActivity {

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, HelpRemoteActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_remote);

    }

    private void launchEmailClient() {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("mailto:scorekeeper.feedback@gmail.com"));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"scorekeeper.feedback@gmail.com"});
        String s = getString(R.string.app_name) + ": " + getString(R.string.setting_help_translate);
        i.putExtra(Intent.EXTRA_SUBJECT, s);

        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
