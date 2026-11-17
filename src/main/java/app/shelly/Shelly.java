package app.shelly;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal client for a Gen2 Shelly RGBW controller (e.g. Shelly Plus RGBW PM), using the JSON-RPC API.
 *
 * The device is addressed by hostname (by default its mDNS name), which is resolved once and cached,
 * so per-request lookups don't mess up the timing of lightning flashes. The address is re-resolved if a request fails.
 *
 * Configure with system property or environment variable SHELLY_HOST (property: shelly.host).
 */

public class Shelly {

	private static final String DEFAULT_HOST = "shellyplusrgbwpm-b0a732414c60.local";

	private static final Shelly DEFAULT = new Shelly( configuredHost() );

	private final String _host;
	private final HttpClient _http = HttpClient.newBuilder().connectTimeout( Duration.ofSeconds( 3 ) ).build();
	private volatile String _resolvedAddress;
	private int _requestId;

	public Shelly( final String host ) {
		_host = host;
	}

	public static Shelly defaultShelly() {
		return DEFAULT;
	}

	private static String configuredHost() {
		final String prop = System.getProperty( "shelly.host" );

		if( prop != null && !prop.isBlank() ) {
			return prop;
		}

		final String env = System.getenv( "SHELLY_HOST" );

		if( env != null && !env.isBlank() ) {
			return env;
		}

		return DEFAULT_HOST;
	}

	public String host() {
		return _host;
	}

	/**
	 * @return The current state of the light
	 */
	public RGBW status() {
		final String json = rpc( "RGBW.GetStatus", "{\"id\":0}" );
		final int[] rgb = intArray( json, "rgb" );
		return new RGBW( bool( json, "output" ), rgb[0], rgb[1], rgb[2], integer( json, "brightness" ), integer( json, "white" ) );
	}

	/**
	 * Set the light's state. Uses whatever transition duration is configured on the device (see setTransitionDuration())
	 */
	public void set( final RGBW s ) {
		final String params = "{\"id\":0,\"on\":%s,\"rgb\":[%s,%s,%s],\"brightness\":%s,\"white\":%s}"
				.formatted( s.on(), clamp( s.red(), 255 ), clamp( s.green(), 255 ), clamp( s.blue(), 255 ), clamp( s.brightness(), 100 ), clamp( s.white(), 255 ) );
		rpc( "RGBW.Set", params );
	}

	public void off() {
		rpc( "RGBW.Set", "{\"id\":0,\"on\":false}" );
	}

	/**
	 * @return The device's configured fade time (seconds) applied to every change
	 */
	public double transitionDuration() {
		final String json = rpc( "RGBW.GetConfig", "{\"id\":0}" );
		final Matcher m = Pattern.compile( "\"transition_duration\"\\s*:\\s*([0-9.]+)" ).matcher( json );
		return m.find() ? Double.parseDouble( m.group( 1 ) ) : 3.0;
	}

	/**
	 * Sets the device's fade time. Note that RGBW.Set only accepts per-call transitions of >= 0.5s, so for instant changes (lightning) this has to be set to 0 in the config.
	 */
	public void setTransitionDuration( final double seconds ) {
		rpc( "RGBW.SetConfig", "{\"id\":0,\"config\":{\"transition_duration\":%s}}".formatted( seconds ) );
	}

	private String rpc( final String method, final String params ) {
		final String body;

		synchronized( this ) {
			body = "{\"id\":%s,\"method\":\"%s\",\"params\":%s}".formatted( ++_requestId, method, params );
		}

		try {
			return send( body );
		}
		catch( IOException e ) {
			// Address may have changed (e.g. device re-joined the network). Re-resolve and retry once.
			_resolvedAddress = null;
			try {
				return send( body );
			}
			catch( IOException | InterruptedException e2 ) {
				throw new ShellyException( "Failed to call %s on %s".formatted( method, _host ), e2 );
			}
		}
		catch( InterruptedException e ) {
			Thread.currentThread().interrupt();
			throw new ShellyException( "Interrupted calling " + method, e );
		}
	}

	private String send( final String body ) throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest
				.newBuilder( URI.create( "http://" + address() + "/rpc" ) )
				.timeout( Duration.ofSeconds( 3 ) )
				.header( "content-type", "application/json" )
				.POST( BodyPublishers.ofString( body ) )
				.build();

		final String response = _http.send( request, BodyHandlers.ofString() ).body();

		if( response.contains( "\"error\"" ) ) {
			throw new ShellyException( "Shelly returned an error for " + body + " : " + response, null );
		}

		return response;
	}

	private String address() throws IOException {
		if( _resolvedAddress == null ) {
			_resolvedAddress = InetAddress.getByName( _host ).getHostAddress();
		}

		return _resolvedAddress;
	}

	private static int clamp( final int value, final int max ) {
		return Math.max( 0, Math.min( max, value ) );
	}

	private static int integer( final String json, final String key ) {
		final Matcher m = Pattern.compile( "\"" + key + "\"\\s*:\\s*(-?\\d+)" ).matcher( json );
		return m.find() ? Integer.parseInt( m.group( 1 ) ) : 0;
	}

	private static boolean bool( final String json, final String key ) {
		final Matcher m = Pattern.compile( "\"" + key + "\"\\s*:\\s*(true|false)" ).matcher( json );
		return m.find() && Boolean.parseBoolean( m.group( 1 ) );
	}

	private static int[] intArray( final String json, final String key ) {
		final Matcher m = Pattern.compile( "\"" + key + "\"\\s*:\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]" ).matcher( json );

		if( !m.find() ) {
			return new int[] { 0, 0, 0 };
		}

		return new int[] { Integer.parseInt( m.group( 1 ) ), Integer.parseInt( m.group( 2 ) ), Integer.parseInt( m.group( 3 ) ) };
	}

	public static class ShellyException extends RuntimeException {
		public ShellyException( String message, Throwable cause ) {
			super( message, cause );
		}
	}
}
