package org.janelia.saalfeldlab.ngff.axes;

public class ArrayAxis extends Axis {

	public ArrayAxis( final int i )
	{
		super( String.format("dim_%d", i), "array", "none", true );
	}

}
