package org.janelia.saalfeldlab.ngff.transforms;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.ngff.axes.AxisPoint;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;

import net.imglib2.realtransform.RealTransform;

public interface CoordinateTransform<T extends RealTransform> {

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

	public RealCoordinate applyAppend( RealCoordinate src );

	public AxisPoint applyAxes( AxisPoint src );

}
