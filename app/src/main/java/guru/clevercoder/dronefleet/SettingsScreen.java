package guru.clevercoder.dronefleet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by russell on 11/9/14.
 */
public class SettingsScreen extends Activity implements View.OnClickListener
{
    private int numberSimDrones;
    private String drone1;
    private String drone2;
    private String ipSim;
    Button saveSettings;

    @Override
    public void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.settings_screen );
        SharedPreferences e = PreferenceManager.getDefaultSharedPreferences(this);
        numberSimDrones = e.getInt("numberSimDrones",2);
        drone1 = e.getString("drone1_ip","192.168.43.4");
        drone2 = e.getString("drone2_ip","192.168.43.5");

        ((EditText) findViewById(R.id.numDrones)).setText(""+numberSimDrones);
        ((EditText) findViewById(R.id.ip_drone1)).setText(""+drone1);
        ((EditText) findViewById(R.id.ip_drone2)).setText(""+drone2);

        ipSim = "192.168.43.42";
        saveSettings = ( Button ) findViewById( R.id.btn_saveSettings );
        saveSettings.setOnClickListener( this );
    }

    @Override
    public void onClick ( View view )
    {
        Intent intent;
        switch ( view.getId() )
        {
            case R.id.btn_saveSettings:
                ipSim = ( ( EditText ) findViewById( R.id.ip_simulator ) ).getText().toString();
                numberSimDrones = Integer.parseInt(((EditText) findViewById(R.id.numDrones)).getText().toString());
                drone1 = ((EditText) findViewById(R.id.ip_drone1)).getText().toString();
                drone2 = ((EditText) findViewById(R.id.ip_drone2)).getText().toString();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("numberSimDrones", numberSimDrones);
                editor.putString("drone1_ip",drone1);
                editor.putString("drone2_ip",drone2);
                editor.commit();

                intent = new Intent( this, MainScreen.class );
                startActivity( intent );
                finish();
            break;
        }
    }

    public int getNumberSimulatedDrones ()
    {
        return numberSimDrones;
    }

    public String getSimulatorIP ()
    {
        return ipSim;
    }

}
