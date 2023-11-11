package lidamot.lights.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;

public class LField extends ERXStatelessComponent {

	public LField( WOContext context ) {
		super( context );
	}

	public String value() {
		return stringValueForBinding( "value" ).toString();
	}

	public void setValue( String value ) {
		int intValue;

		try {
			intValue = Integer.parseInt( value );
		}
		catch( Exception e ) {
			intValue = 0;
		}

		if( intValue > 255 ) {
			intValue = 255;
		}

		setValueForBinding( intValue, "value" );
	}
}