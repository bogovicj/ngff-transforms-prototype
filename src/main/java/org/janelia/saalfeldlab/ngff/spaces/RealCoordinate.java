package org.janelia.saalfeldlab.ngff.spaces;

import org.janelia.saalfeldlab.ngff.axes.Axis;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;

public class RealCoordinate extends RealPoint {

	protected Space space;

	public RealCoordinate( int numDimensions )
	{
		super( numDimensions );
		space = new ArraySpace( numDimensions );
	}

	public RealCoordinate( int numDimensions, Space space )
	{
		super( numDimensions );
		this.space = space;
	}

	public RealCoordinate( int numDimensions, Axis[] axes )
	{
		super( numDimensions );
		this.space = new Space( "", axes );
	}

	public RealCoordinate( RealLocalizable p )
	{
		this( p, new ArraySpace( p.numDimensions() ));
	}
	
	public RealCoordinate( RealLocalizable p, Axis[] axes )
	{
		this( p, new Space( "", axes ));
//		assert( p.numDimensions() == space.getAxes().length);
	}

	public RealCoordinate( RealLocalizable p, Space space )
	{
		super(p);
		this.space = space;
//		assert( p.numDimensions() == space.getAxes().length);
	}
	
	public double getDoublePosition( String axis )
	{
		final int i = getSpace().indexOf(axis);
		return i >= 0 ? getDoublePosition( i ) : Double.NaN;
	}
	
	public Space getSpace()
	{
		return space;
	}

	public void setSpace( final Space space )
	{
		this.space = space;
	}

	/*
	for debugging
	 */
	public void positionToIndexes() {
	for( int i = 0; i < numDimensions(); i++ )
		setPosition(i, i);
	}
	
	public RealCoordinate append( RealCoordinate other )
	{
		final Space axisDiff = other.getSpace().diff(null, getSpace());
		final int N = numDimensions() + axisDiff.numDimensions();

		final Axis[] resultAxes = new Axis[N];
		final double[] pos = new double[N];
		int j = 0;
		for( int i = 0; i < N; i++ )
		{
			if( i < numDimensions() )
			{
				resultAxes[i] = getSpace().getAxis(i);
				pos[i] = getDoublePosition(i);
			}
			else
			{
				resultAxes[i] = other.getSpace().getAxis(j);
				pos[i] = other.getDoublePosition(j);
				j++;
			}
		}

		final RealCoordinate result = new RealCoordinate( N );
		result.setSpace(new Space("", resultAxes));
		result.setPosition(pos);

		return result;
	}

	public RealCoordinate getSubset( Space subspace )
	{
		final RealCoordinate result = new RealCoordinate( subspace.numDimensions() );
		for( int i = 0; i < result.numDimensions(); i++ )
			result.setPosition( getDoublePosition( subspace.getAxis(i).getLabel()), i);

		return result;
	}

}
