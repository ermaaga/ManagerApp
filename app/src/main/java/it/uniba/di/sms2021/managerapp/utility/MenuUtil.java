package it.uniba.di.sms2021.managerapp.utility;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import it.uniba.di.sms2021.managerapp.NotificationsActivity;
import it.uniba.di.sms2021.managerapp.R;

public class MenuUtil {
    public static void performMainActions (Context context, int MenuItemId) {
        if (MenuItemId == R.id.action_notifications) {
            context.startActivity(new Intent(context, NotificationsActivity.class));
        } else if (MenuItemId == R.id.action_settings) {
            Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show();
        }
    }
}
