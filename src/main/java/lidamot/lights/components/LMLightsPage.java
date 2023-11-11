package lidamot.lights.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;
import main.Audio;
import main.shelly.ShellyClient;
import main.shelly.ShellyPlayground;
import main.shelly.ShellyStatus;
import main.tradfri.TradfriClient;
import main.tradfri.TradfriPlayground;
import nl.stijngroenen.tradfri.device.Light;

public class LMLightsPage extends ERXComponent {

	public int redShelly;
	public int greenShelly;
	public int blueShelly;
	public int whiteShelly;

	public LMLightsPage( WOContext context ) {
		super( context );
		ShellyStatus status = ShellyClient.defaultClient().readStatus();
		redShelly = status.red;
		greenShelly = status.green;
		blueShelly = status.blue;
		whiteShelly = status.white;
	}

	public WOActionResults shellyChanged() {
		ShellyClient.defaultClient().setColor( redShelly, greenShelly, blueShelly, whiteShelly );
		return null;
	}

	public WOActionResults ikeaChanged() {
		ShellyClient.defaultClient().setColor( redShelly, greenShelly, blueShelly, whiteShelly );
		return null;
	}

	public WOActionResults lightningShelly() {
		Audio.playThunderclap();
		ShellyPlayground.generateLightningBolt();
		ShellyClient.defaultClient().setColor( redShelly, greenShelly, blueShelly, whiteShelly );
		return null;
	}

	public WOActionResults lightningIkea() {
		for( Light light : TradfriClient.defaultClient().lights() ) {
			new Thread( () -> {
				TradfriPlayground.triggerLightningAndSound( light );
			} ).run();
		}

		return null;
	}

	public WOActionResults resetIkea() {
		for( Light light : TradfriClient.defaultClient().lights() ) {
			new Thread( () -> {
				TradfriPlayground.resetLightToWarmWhite( light );
			} ).run();
		}
		return null;
	}
}