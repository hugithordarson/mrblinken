package app;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.shelly.RGBW;
import app.shelly.Shelly;

/**
 * Halloween lightning storm on the Shelly LED strip.
 *
 * A storm fires a lightning strike (a burst of flashes + optional thunder) at random intervals until stopped.
 * After each strike, the strip is restored to the state it was in before the strike.
 *
 * Can also be run standalone: java app.Lightning [minSeconds] [maxSeconds]
 */

public class Lightning {

	private static final Logger logger = LoggerFactory.getLogger( Lightning.class );

	private static final Lightning DEFAULT = new Lightning( Shelly.defaultShelly() );

	private final Shelly _shelly;
	private final ScheduledExecutorService _scheduler = Executors.newSingleThreadScheduledExecutor( r -> {
		final Thread t = new Thread( r, "lightning" );
		t.setDaemon( true );
		return t;
	} );

	private ScheduledFuture<?> _nextStrike;
	private double _originalTransitionDuration = -1;

	private int _minSecondsBetweenStrikes = 10;
	private int _maxSecondsBetweenStrikes = 45;

	public Lightning( final Shelly shelly ) {
		_shelly = shelly;
	}

	public static Lightning defaultLightning() {
		return DEFAULT;
	}

	public static void main( String[] args ) throws InterruptedException {
		final Lightning lightning = defaultLightning();

		if( args.length == 2 ) {
			lightning.setInterval( Integer.parseInt( args[0] ), Integer.parseInt( args[1] ) );
		}

		Runtime.getRuntime().addShutdownHook( new Thread( () -> {
			try {
				lightning.stopStorm().get( 5, TimeUnit.SECONDS );
			}
			catch( Exception e ) {
				logger.warn( "Clean shutdown failed: {}", e.getMessage() );
			}
		} ) );
		lightning.startStorm();
		Thread.currentThread().join();
	}

	public synchronized void setInterval( final int minSeconds, final int maxSeconds ) {
		_minSecondsBetweenStrikes = Math.max( 1, minSeconds );
		_maxSecondsBetweenStrikes = Math.max( _minSecondsBetweenStrikes, maxSeconds );
	}

	public synchronized int minSecondsBetweenStrikes() {
		return _minSecondsBetweenStrikes;
	}

	public synchronized int maxSecondsBetweenStrikes() {
		return _maxSecondsBetweenStrikes;
	}

	public synchronized boolean isStormRunning() {
		return _nextStrike != null;
	}

	public synchronized void startStorm() {
		if( isStormRunning() ) {
			return;
		}

		logger.info( "Storm starting on {} (strike every {}-{}s)", _shelly.host(), _minSecondsBetweenStrikes, _maxSecondsBetweenStrikes );
		scheduleNextStrike( 1 );
	}

	/**
	 * @return A future that completes once the light and device settings have been restored
	 */
	public synchronized Future<?> stopStorm() {
		if( _nextStrike != null ) {
			_nextStrike.cancel( false );
			_nextStrike = null;
			logger.info( "Storm stopped" );
		}

		// Wait for a strike in progress to finish (it restores the light), then put back the device's fade setting
		return _scheduler.submit( this::restoreTransitionDuration );
	}

	/**
	 * Perform a single strike right away (in the background)
	 */
	public void strikeNow() {
		_scheduler.execute( () -> {
			strike();

			if( !isStormRunning() ) {
				restoreTransitionDuration();
			}
		} );
	}

	private synchronized void scheduleNextStrike( final long delaySeconds ) {
		_nextStrike = _scheduler.schedule( () -> {
			strike();

			synchronized( this ) {
				if( _nextStrike != null ) {
					scheduleNextStrike( random( _minSecondsBetweenStrikes, _maxSecondsBetweenStrikes ) );
				}
			}
		}, delaySeconds, TimeUnit.SECONDS );
	}

	/**
	 * Runs on the scheduler thread only, so strikes never overlap
	 */
	private void strike() {
		try {
			ensureInstantTransitions();

			final RGBW before = _shelly.status();
			final int flashes = random( 2, 6 );

			logger.info( "⚡ Strike with {} flashes", flashes );

			// Thunder slightly after the flash, like the real thing (roughly)
			_scheduler.schedule( Thunder::play, random( 0, 400 ), TimeUnit.MILLISECONDS );

			for( int i = 0; i < flashes; i++ ) {
				// Bluish white, varying intensity. The first flash is the big one.
				final int brightness = i == 0 ? 100 : random( 30, 100 );
				final int blue = random( 180, 255 );
				_shelly.set( new RGBW( true, random( 150, 220 ), random( 170, 230 ), blue, brightness, random( 150, 255 ) ) );
				sleep( random( 20, 90 ) );

				_shelly.off();
				sleep( random( 30, 250 ) );
			}

			_shelly.set( before );
		}
		catch( RuntimeException e ) {
			logger.warn( "Strike failed: {}", e.getMessage() );
		}
	}

	/**
	 * The device's configured fade would turn flashes into slow glows (and RGBW.Set won't accept a per-call transition below 0.5s), so we set it to 0 while the storm runs
	 */
	private void ensureInstantTransitions() {
		if( _originalTransitionDuration < 0 ) {
			_originalTransitionDuration = _shelly.transitionDuration();
			_shelly.setTransitionDuration( 0 );
		}
	}

	private void restoreTransitionDuration() {
		if( _originalTransitionDuration >= 0 ) {
			try {
				_shelly.setTransitionDuration( _originalTransitionDuration );
				_originalTransitionDuration = -1;
			}
			catch( RuntimeException e ) {
				logger.warn( "Failed to restore transition duration: {}", e.getMessage() );
			}
		}
	}

	private static int random( final int min, final int max ) {
		return ThreadLocalRandom.current().nextInt( min, max + 1 );
	}

	private static void sleep( final long ms ) {
		try {
			Thread.sleep( ms );
		}
		catch( InterruptedException e ) {
			Thread.currentThread().interrupt();
		}
	}
}
