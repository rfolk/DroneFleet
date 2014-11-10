package guru.clevercoder.dronefleet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by russell on 11/9/14.
 */
public class SettingsScreen extends Activity implements View.OnClickListener
{
    private int numberSimDrones;
    private String ipSim;
    Button saveSettings;

    @Override
    public void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.settings_screen );
        numberSimDrones = 0;
        ipSim = "";
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
                intent = new Intent( this, MainScreen.class );
                startActivity( intent );
                finish();
            break;
        }
    }

}
