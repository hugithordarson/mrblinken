package app;

import main.Audio;
import main.Util;
import main.shelly.ShellyClient;
import main.shelly.ShellyPlayground;
import main.shelly.ShellyStatus;
import main.tradfri.TradfriClient;
import main.tradfri.TradfriPlayground;
import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;
import nl.stijngroenen.tradfri.device.Light;

public class LMLightsPage extends NGComponent {

	public int redShelly;
	public int greenShelly;
	public int blueShelly;
	public int whiteShelly;

	public LMLightsPage( NGContext context ) {
		super( context );
		ShellyStatus status = ShellyClient.defaultClient().readStatus();
		//		ShellyStatus status = new ShellyStatus( 10, 20, 30, 40 );
		redShelly = status.red();
		greenShelly = status.green();
		blueShelly = status.blue();
		whiteShelly = status.white();
	}

	public NGActionResults shellyChanged() {
		ShellyClient.defaultClient().setColor( redShelly, greenShelly, blueShelly, whiteShelly );
		//		System.out.println( redShelly + " : " + greenShelly + " : " + blueShelly + " : " + whiteShelly );
		return null;
	}

	public NGActionResults halloween() {
		new Thread( () -> {
			Halloween2023.main( null );
		} ).start();

		return null;
	}

	public NGActionResults lightningShelly() {
		Audio.playThunderclap();
		ShellyPlayground.generateLightningBolt();
		ShellyClient.defaultClient().setColor( redShelly, greenShelly, blueShelly, whiteShelly );
		return null;
	}

	public NGActionResults lightningShellyTimed() throws InterruptedException {
		int numberOfLightnings = 1000;

		while( numberOfLightnings > 0 ) {
			int wait = Util.generateRandom( 30 * 1000, 120 * 1000 );
			System.out.println( "Doing lightning %s, sleeping for %sms ".formatted( numberOfLightnings, wait ) );
			Thread.sleep( wait );
			Audio.playThunderclap();
			ShellyPlayground.generateLightningBolt();
			numberOfLightnings--;
		}

		ShellyClient.defaultClient().setColor( redShelly, greenShelly, blueShelly, whiteShelly );
		return null;
	}

	public NGActionResults lightningIkea() {
		for( Light light : TradfriClient.defaultClient().lights() ) {
			new Thread( () -> {
				TradfriPlayground.triggerLightningAndSound( light );
			} ).run();
		}

		return null;
	}

	public NGActionResults resetIkea() {
		for( Light light : TradfriClient.defaultClient().lights() ) {
			new Thread( () -> {
				TradfriPlayground.resetLightToWarmWhite( light );
			} ).run();
		}
		return null;
	}
}