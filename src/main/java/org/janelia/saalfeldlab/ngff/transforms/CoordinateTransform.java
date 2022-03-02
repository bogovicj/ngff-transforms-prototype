package org.janelia.saalfeldlab.ngff.transforms;

import org.janelia.saalfeldlab.n5.N5Reader;

public interface CoordinateTransform<T> {

	public T getTransform();

	public default T getTransform( final N5Reader n5 ) {
		return getTransform();
	}

	public String getName();
	
	public String getInputSpace();

	public String getOutputSpace();
}
