package org.janelia.saalfeldlab.ngff.graph;

import java.util.LinkedList;
import java.util.List;

import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.IdentityCoordinateTransform;

import net.imglib2.realtransform.AffineGet;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.ScaleAndTranslation;

public class TransformPath {
	
	private final String start;
	
	private final TransformPath parentPath;
	
	private final CoordinateTransform transform;

	private final String end;

	public TransformPath( final CoordinateTransform transform ) {

		this.start = transform.getInputSpace();
		this.transform = transform;
		this.end = transform.getOutputSpace();
		this.parentPath = null;
	}
	
	public TransformPath( final TransformPath parentPath, final CoordinateTransform transform ) {

		this.start = parentPath.getStart();
		this.parentPath = parentPath;
		this.transform = transform;
		this.end = transform.getOutputSpace();
	}

//	public RegistrationPath( final Space start,
//			final RegistrationPath parentPath,
//			final Transform transform,
//			final Space end ) {
//
//		this.start = start;
//		this.parentPath = parentPath;
//		this.transform = transform;
//		this.end = end;
//	}
	
	public String getStart()
	{
		return start;
	}
	
	public String getEnd()
	{
		return end;
	}

//	public double getCost()
//	{
//		return flatTransforms().stream().mapToDouble( Transform::getCost ).sum();
//	}

	/**
	 * Does this path run through the given space.
	 * 
	 * @param space the space
	 * @return true if this path contains space
	 */
	public boolean hasSpace( final Space space )
	{
		if ( start.equals( space ) || end.equals( space ))
			return true;

		if( parentPath != null )
			return parentPath.hasSpace( space );

		return false;
	}
	
	public List<CoordinateTransform> flatTransforms()
	{
		LinkedList<CoordinateTransform> flatTransforms = new LinkedList<>();
		flatTransforms( flatTransforms );
		return flatTransforms;
	}
	
	public RealTransform totalTransorm()
	{
		final RealTransformSequence total = new RealTransformSequence();
		flatTransforms().forEach( t -> total.add( ((RealTransform)t.getTransform())));
		return total;
	}
	
	private static void preConcatenate( AffineTransform3D tgt, AffineGet concatenate )
	{
		if( concatenate.numTargetDimensions() > 3 )
			tgt.preConcatenate(concatenate);
		else if( concatenate.numTargetDimensions() == 2 )
		{
			AffineTransform3D c = new AffineTransform3D();
			c.set(
					concatenate.get(0, 0), concatenate.get(0, 1), 0, concatenate.get(0, 2),
					concatenate.get(1, 0), concatenate.get(1, 1), 0, concatenate.get(1, 2),
					0, 0, 1, 0);

			tgt.preConcatenate(c);
		}
		else if( concatenate.numTargetDimensions() == 1 )
		{
			ScaleAndTranslation c = new ScaleAndTranslation(
					new double[]{ 1, 1, 1 },
					new double[]{ 0, 0, 0});
			tgt.preConcatenate(c);
		}
	}
	
	
	public AffineTransform3D totalAffine3D()
	{
		final AffineTransform3D total = new AffineTransform3D();
		for( CoordinateTransform ct : flatTransforms() )
		{
			if( ct instanceof IdentityCoordinateTransform )
				continue;
			else {
				Object t = ct.getTransform();
				if( t instanceof AffineGet )
				{
					preConcatenate( total, (AffineGet) t  );
	//				total.preConcatenate((AffineGet) t );
				}
				else
					return null;
			}
		}
		return total;
	}

	private void flatTransforms( LinkedList<CoordinateTransform> queue )
	{
		if( transform != null )
			queue.addFirst( transform );

		if( parentPath != null )
			parentPath.flatTransforms( queue );
	}

	public List<String> flatSpace()
	{
		LinkedList<String> flatSpace = new LinkedList<>();
		flatSpace( flatSpace );
//		if( parentPath == null )
		flatSpace.addFirst( start );
		return flatSpace;
	}

	private void flatSpace( LinkedList<String> queue )
	{
		if( end != null )
			queue.addFirst( end );

		if( parentPath != null )
			parentPath.flatSpace( queue );
	}
	
	public String toString()
	{
		List<String> spaceList = flatSpace();
		List<CoordinateTransform> transformList = flatTransforms();

		if( transformList.size() < 1 )
			return "(" + spaceList.get(0) + ")";

		StringBuffer out = new StringBuffer();
		for( int i = 0; i < transformList.size(); i++ )
		{
			out.append( "("+spaceList.get(i));
			out.append( ") --" );
			out.append( transformList.get(i));
			out.append( "-> " );
		}
		out.append( "(" + end  + ")");

		return out.toString();
	}

//	public void add(Space s, Transform t) {
//		spaces.add( s );
//		transforms.add( t );
//	}

}
