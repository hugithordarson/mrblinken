package app;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import main.Audio;
import main.shelly.ShellyPlayground;

public class Halloween2023 {

	private static final Random random = new Random();
	private static final Timer timer = new Timer();

	public static void main( String[] args ) {
		scheduleNextStorm();
	}

	private static void scheduleNextStorm() {
		long delay = 5000 + random.nextInt( 15000 );

		System.out.println( "delay: " + delay );

		timer.schedule( new TimerTask() {
			@Override
			public void run() {
				System.out.println( "Lightning & Thunder" );
				performLightning();
				Audio.playThunderclap();
				scheduleNextStorm();
			}
		}, delay );
	}

	private static void performLightning() {
		ShellyPlayground.generateLightningBolt();
	}

	//	public static void main( String[] args ) {
	//		Timer t = new Timer();
	//		TimerTask smu = new TimerTask() {
	//
	//			@Override
	//			public void run() {
	//				System.out.println( "Lightning" );
	//				performLightning();
	//				Audio.playThunderclap();
	//			}
	//		};
	//		t.schedule( smu, 0, 15000 );
	//
	//		Timer t2 = new Timer();
	//		TimerTask smu2 = new TimerTask() {
	//
	//			@Override
	//			public void run() {
	//				System.out.println( "Audio" );
	//				Audio.playThunderclap();
	//			}
	//		};
	//		t2.schedule( smu2, 0, 32000 );
	//	}
	//
	//	public static void main( String[] args ) {
	//		Timer t = new Timer();
	//		TimerTask smu = new TimerTask() {
	//
	//			@Override
	//			public void run() {
	//				System.out.println( "Lightning" );
	//				performLightning();
	//			}
	//		};
	//		t.schedule( smu, 5000, 5000 );
	//
	//		Timer t2 = new Timer();
	//		TimerTask smu2 = new TimerTask() {
	//
	//			@Override
	//			public void run() {
	//				System.out.println( "Audio" );
	//				Audio.playThunderclap();
	//			}
	//		};
	//		t2.schedule( smu2, 10000, 10000 );
	//	}
	//	private static void performLightning() {
	//		for( Light light2 : TradfriClient.defaultClient().lights() ) {
	//			ColourRGB smu = new ColourRGB( 255, 255, 255 );
	//			light2.setColour( smu );
	//		}
	//
	//		for( Light light : TradfriClient.defaultClient().lights() ) {
	//			new Thread( () -> {
	//				TradfriPlayground.triggerLightning( light );
	//				ShellyPlayground.generateLightningBolt();
	//			} ).run();
	//		}
	//	}
}