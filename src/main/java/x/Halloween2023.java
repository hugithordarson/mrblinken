package x;

import java.util.Timer;
import java.util.TimerTask;

import main.shelly.ShellyPlayground;
import main.tradfri.TradfriClient;
import main.tradfri.TradfriPlayground;
import nl.stijngroenen.tradfri.device.Light;
import nl.stijngroenen.tradfri.util.ColourRGB;

public class Halloween2023 {

	public static void main( String[] args ) {
		Timer t = new Timer();
		TimerTask smu = new TimerTask() {

			@Override
			public void run() {
				System.out.println( "ble" );
				performLightning();
			}
		};
		t.schedule( smu, 5000, 5000 );

		Timer t2 = new Timer();
		TimerTask smu2 = new TimerTask() {

			@Override
			public void run() {
				System.out.println( "ble" );
				TradfriPlayground.triggerSound( null );
			}
		};
		t2.schedule( smu2, 10000, 10000 );
	}

	private static void performLightning() {
		for( Light light2 : TradfriClient.defaultClient().lights() ) {
			ColourRGB smu = new ColourRGB( 255, 255, 255 );
			light2.setColour( smu );
		}

		for( Light light : TradfriClient.defaultClient().lights() ) {
			new Thread( () -> {
				TradfriPlayground.triggerLightning( light );
				ShellyPlayground.generateLightningBolt();
			} ).run();
		}
	}
}