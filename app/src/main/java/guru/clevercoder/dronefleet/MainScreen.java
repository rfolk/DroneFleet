package guru.clevercoder.dronefleet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by russell on 11/9/14.
 */
public class MainScreen extends Activity implements View.OnClickListener
{
    Button flightPath;
    Button settings;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main_screen );
        flightPath = ( Button ) findViewById( R.id.btn_flightPath );
        flightPath.setOnClickListener( this );
        settings = ( Button ) findViewById( R.id.btn_settings );
        settings.setOnClickListener( this );
    }

    @Override
    public void onClick ( View view )
    {
        Intent intent;
        switch ( view.getId() )
        {
            case R.id.btn_flightPath:
                intent = new Intent( this, MapScreen.class );
                startActivity( intent );
                finish();
            break;
            case R.id.btn_settings:
                intent = new Intent( this, SettingsScreen.class );
                startActivity( intent );
                finish();
            break;
        }
    }
}
