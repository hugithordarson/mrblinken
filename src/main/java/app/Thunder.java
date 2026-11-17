package app;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thunder sound. Plays a random WAV from the thunder directory, never the same one twice in a row.
 *
 * Directory: system property thunder.dir or environment variable THUNDER_DIR. Default: ~/Music/MrBlinken/thunder
 *
 * If there are no sounds, lightning is silent (logged once).
 */

public class Thunder {

	private static final Logger logger = LoggerFactory.getLogger( Thunder.class );

	private static File _lastPlayed;
	private static boolean _warned;

	public static void main( String[] args ) throws InterruptedException {
		play();
		Thread.sleep( 10000 );
	}

	public static void play() {
		final File file = pick();

		if( file == null ) {
			return;
		}

		Thread.ofVirtual().start( () -> {
			try {
				final AudioInputStream in = AudioSystem.getAudioInputStream( new BufferedInputStream( new FileInputStream( file ) ) );
				final Clip clip = AudioSystem.getClip();
				clip.addLineListener( e -> {
					if( e.getType() == LineEvent.Type.STOP ) {
						clip.close();
					}
				} );
				clip.open( in );
				clip.start();
			}
			catch( Exception e ) {
				logger.warn( "Failed to play thunder from {}: {}", file, e.getMessage() );
			}
		} );
	}

	private static synchronized File pick() {
		final File dir = directory();
		final File[] files = dir.listFiles( f -> f.isFile() && f.getName().toLowerCase().endsWith( ".wav" ) );

		if( files == null || files.length == 0 ) {
			if( !_warned ) {
				logger.info( "No thunder sounds (.wav) in {} (set thunder.dir / THUNDER_DIR). Lightning will be silent.", dir );
				_warned = true;
			}
			return null;
		}

		File[] candidates = files;

		if( files.length > 1 && _lastPlayed != null ) {
			candidates = Arrays.stream( files ).filter( f -> !f.equals( _lastPlayed ) ).toArray( File[]::new );
		}

		_lastPlayed = candidates[ThreadLocalRandom.current().nextInt( candidates.length )];
		return _lastPlayed;
	}

	private static File directory() {
		String path = System.getProperty( "thunder.dir" );

		if( path == null || path.isBlank() ) {
			path = System.getenv( "THUNDER_DIR" );
		}

		if( path == null || path.isBlank() ) {
			path = System.getProperty( "user.home" ) + "/Music/MrBlinken/thunder";
		}

		return new File( path );
	}
}
