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
//		super(p);
//		this.space = 
//		assert( p.numDimensions() == space.getAxes().length);
	}

	public RealCoordinate( RealLocalizable p, Space space )
	{
		super(p);
		this.space = space;
//		assert( p.numDimensions() == space.getAxes().length);
	}
	
	public Space getSpace()
	{
		return space;
	}

	public void setSpace( final Space space )
	{
		this.space = space;
	}

}
