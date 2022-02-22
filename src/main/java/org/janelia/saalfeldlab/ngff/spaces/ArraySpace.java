package org.janelia.saalfeldlab.ngff.spaces;

import java.util.stream.IntStream;

import org.janelia.saalfeldlab.ngff.axes.Axis;

/**
 * A Space is a set of axes.
 *
 * @author John Bogovic
 */
public class ArraySpace extends Space {

	public ArraySpace() {
		this( 5 );
	}

	public ArraySpace( int nd ) {
		super( "", arrayAxes( nd ));
	}

	public String toString()
	{
		return "<array space "+ numDimensions() +">";
	}
	
	public static Axis[] arrayAxes( int nd )
	{
		return IntStream.range(0, nd).mapToObj( i -> Axis.arrayAxis(i) ).toArray( Axis[]::new );
	}
	
}
