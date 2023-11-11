package app;

import lidamot.lights.components.LMLightsPage;
import ng.appserver.NGApplication;

public class Application extends NGApplication {

	public static void main( String[] args ) {
		NGApplication.run( args, Application.class );
	}

	public Application() {
		routeTable().mapComponent( "/lights", LMLightsPage.class );
	}
}
