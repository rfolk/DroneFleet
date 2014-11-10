package guru.clevercoder.dronefleet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by russell on 11/9/14.
 */
public class GoScreen extends Activity implements View.OnClickListener
{
    private Button startLive;
    private Button startSim;

    @Override
    public void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.go_screen );
        startLive = ( Button ) findViewById( R.id.btn_real );
        startLive.setOnClickListener( this );
        startSim = ( Button ) findViewById( R.id.btn_simulator );
        startSim.setOnClickListener( this );
    }

    @Override
    public void onClick ( View view )
    {
        Intent intent;
        switch ( view.getId() )
        {
            case R.id.btn_real:
                intent = new Intent( this, MapScreen.class );
                startActivity( intent );
                finish();
            break;
            case R.id.btn_simulator:
                intent = new Intent( this, MapScreen.class );
                startActivity( intent );
                finish();
            break;
        }
    }
}
