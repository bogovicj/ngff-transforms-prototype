package org.janelia.saalfeldlab.ngff.transforms;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;

public interface CoordinateTransform<T> {

	public T getTransform();

	public default T getTransform( final N5Reader n5 ) {
		return getTransform();
	}

	public String getName();
	
	public String getInputSpace();

	public String getOutputSpace();

	public String[] getInputAxes();

	public String[] getOutputAxes();
	
	public RealCoordinate apply( RealCoordinate src, RealCoordinate dst );

}
