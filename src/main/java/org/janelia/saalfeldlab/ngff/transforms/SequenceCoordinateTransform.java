package org.janelia.saalfeldlab.ngff.transforms;

import java.util.List;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;

import net.imglib2.realtransform.RealTransformSequence;

public class SequenceCoordinateTransform extends AbstractCoordinateTransform<RealTransformSequence> implements RealCoordinateTransform<RealTransformSequence> {

	private final CoordinateTransform<?>[] transformations;

	public SequenceCoordinateTransform( final String name, 
			final String inputSpace, final String outputSpace,
			final CoordinateTransform<?>[] transformations) {
		super("sequence", name, inputSpace, outputSpace );
		this.transformations = transformations;
	}
	
	public SequenceCoordinateTransform( final String name, 
			final String inputSpace, final String outputSpace,
			final List<CoordinateTransform<?>> transformationList ) {
		super("sequence", name, inputSpace, outputSpace );
		this.transformations = new CoordinateTransform[ transformationList.size() ];
		for( int i = 0; i < transformationList.size(); i++ )
			this.transformations[i] = transformationList.get( i );
	}

	@Override
	public RealTransformSequence getTransform()
	{
		RealTransformSequence transform = new RealTransformSequence();
		for( CoordinateTransform<?> t : getTransformations() )
			transform.add( t.getTransform() );

		return transform;
	}

	@Override
	public RealTransformSequence getTransform(final N5Reader n5 )
	{
		RealTransformSequence transform = new RealTransformSequence();
		for( CoordinateTransform<?> t : getTransformations() )
			transform.add( t.getTransform(n5) );

		return transform;
	}

	public CoordinateTransform<?>[] getTransformations() {
		return transformations;
	}
	
	@Override
	public RealCoordinate apply( final RealCoordinate src, final RealCoordinate dst )
	{

		int N = dst.numDimensions() > src.numDimensions() ? 
				dst.numDimensions() : src.numDimensions();

		// this already is not enough since the
		// number of dimensions may increase at an intermediate step
		// TODO make a test for this and fix
		final RealCoordinate tmp1 = new RealCoordinate( N );
		final RealCoordinate tmp2 = new RealCoordinate( N );
		RealCoordinate tmp = tmp1;
		RealCoordinate other = tmp2;

		int i = 0;
		for( CoordinateTransform<?> t : transformations )
		{
			if( i == 0 )
				t.apply(src, tmp);
			else if( i == transformations.length - 1 )
				t.apply(tmp, dst);
			else
			{
				t.apply(tmp, other);

				if( tmp == tmp1 )
				{
					other = tmp1;
					tmp = tmp2;
				}
				else
				{
					other = tmp2;
					tmp = tmp1;
				}
			}
			i++;
		}

		return dst;
	}

}
