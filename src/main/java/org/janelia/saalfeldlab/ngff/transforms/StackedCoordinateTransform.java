package org.janelia.saalfeldlab.ngff.transforms;

import java.util.List;

import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.StackedRealTransform;

public class StackedCoordinateTransform extends AbstractCoordinateTransform<StackedRealTransform> {

	List<CoordinateTransform<?>> transforms;

	public StackedCoordinateTransform( 
			final String name,
			final String inputSpace, final String outputSpace,
			List<CoordinateTransform<?>> transforms )
	{
		super( "stacked", name, inputSpace, outputSpace );
		this.transforms = transforms;
	}

	@Override
	public StackedRealTransform getTransform() {
		RealTransform[] arr = transforms.stream()
				.map( x -> (RealTransform)x.getTransform() )
				.toArray( RealTransform[]::new );

		return new StackedRealTransform(arr);
	}

}
