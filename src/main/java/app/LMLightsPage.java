package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.shelly.RGBW;
import app.shelly.Shelly;
import ng.appserver.NGActionResults;
import ng.appserver.NGContext;
import ng.appserver.templating.NGComponent;

public class LMLightsPage extends NGComponent {

	private static final Logger logger = LoggerFactory.getLogger( LMLightsPage.class );

	public int red;
	public int green;
	public int blue;
	public int brightness;
	public int white;

	public int minSeconds;
	public int maxSeconds;

	public String errorMessage;

	public LMLightsPage( NGContext context ) {
		super( context );
		minSeconds = lightning().minSecondsBetweenStrikes();
		maxSeconds = lightning().maxSecondsBetweenStrikes();

		try {
			final RGBW status = shelly().status();
			red = status.red();
			green = status.green();
			blue = status.blue();
			brightness = status.brightness();
			white = status.white();
		}
		catch( RuntimeException e ) {
			logger.warn( "Couldn't read Shelly status", e );
			errorMessage = "Næ ekki sambandi við Shelly (%s)".formatted( shelly().host() );
		}
	}

	private static Shelly shelly() {
		return Shelly.defaultShelly();
	}

	private static Lightning lightning() {
		return Lightning.defaultLightning();
	}

	public NGActionResults colorChanged() {
		shelly().set( new RGBW( true, red, green, blue, brightness, white ) );
		return null;
	}

	/**
	 * Applies to the next scheduled strike, so it can be changed while a storm is running
	 */
	public NGActionResults intervalChanged() {
		lightning().setInterval( minSeconds, maxSeconds );
		minSeconds = lightning().minSecondsBetweenStrikes();
		maxSeconds = lightning().maxSecondsBetweenStrikes();
		return null;
	}

	public NGActionResults strike() {
		lightning().strikeNow();
		return null;
	}

	public NGActionResults toggleStorm() {
		if( lightning().isStormRunning() ) {
			lightning().stopStorm();
		}
		else {
			lightning().startStorm();
		}

		return null;
	}

	public String stormLabel() {
		return lightning().isStormRunning() ? "🎃 Stoppa storminn" : "🎃 Byrja storminn";
	}
}
