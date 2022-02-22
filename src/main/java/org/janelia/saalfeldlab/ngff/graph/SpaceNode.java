package org.janelia.saalfeldlab.ngff.graph;

import java.util.ArrayList;
import java.util.List;

import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;

/**
 * A node in a {@link TransformGraph}.
 * 
 * Edges are directed with this node as their base.
 * 
 * @author John Bogovic
 */
public class SpaceNode
{
	private final Space space;

	private final List< CoordinateTransform<?> > edges;

	public SpaceNode( final Space space )
	{
		this( space, new ArrayList< CoordinateTransform<?> >() );
	}

	public SpaceNode( final Space space, List< CoordinateTransform<?> > edges )
	{
		this.space = space;
		this.edges = edges;
	}

	public Space space()
	{
		return space;
	}
	
	public List<CoordinateTransform<?>> edges()
	{
		return edges;
	}
	
	@Override
	public boolean equals( Object other )
	{
		return space.equals(other);
	}

}
