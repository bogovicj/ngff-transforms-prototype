package org.janelia.saalfeldlab.ngff.transforms;

import java.util.ArrayList;

import org.janelia.saalfeldlab.ngff.axes.AxisPoint;
import org.janelia.saalfeldlab.ngff.axes.AxisUtils;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;

import net.imglib2.realtransform.RealComponentMappingTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;

public abstract class AbstractCoordinateTransform<T extends RealTransform> implements CoordinateTransform<T> {

	protected String type;

	protected String name;
	
	protected String input_space;

	protected String output_space;

	// implement
	protected String[] input_axes;

	protected String[] output_axes;

	protected transient Space inputSpaceObj;

	protected transient Space outputSpaceObj;

	public abstract T getTransform();
	
	public AbstractCoordinateTransform( final String type, 
			final String name,
			final String inputSpace, final String outputSpace ) {
		this.type = type;
		this.name = name;
		this.input_space = inputSpace;
		this.output_space = outputSpace;
	}
	
	public AbstractCoordinateTransform( final String type, 
			final String name,
			final String[] inputAxes, final String[] outputAxes ) {
		this.type = type;
		this.name = name;
		this.input_axes = inputAxes;
		this.output_axes = outputAxes;
	}

	public AbstractCoordinateTransform( final String type, final String name ) {
		this.type = type;
		this.name = name;
	}

	public AbstractCoordinateTransform( final String type ) {
		this.type = type;
	}

	/**
	 * 
	 * If this object does not have input_space or output_space defined,
	 * attempts to infer the space name, given * input_axes or output_axes, if they are defined.
	 * 
	 * 
	 * @param spaces the spaces object
	 * @return true if input_space and output_space are defined.
	 */
	public boolean inferSpacesFromAxes( Spaces spaces )
	{
		if( input_space == null && output_axes != null )
			input_space = spaceNameFromAxesLabels( spaces, input_axes );

		if( output_space == null && output_axes != null )
			output_space = spaceNameFromAxesLabels( spaces, output_axes );

		if( input_space != null && output_space != null )
			return true;
		else
			return false;
	}

	public String[] getInputAxes()
	{
		if( input_axes != null )
			return input_axes;
		else
			return inputSpaceObj.getAxisLabels();
	}

	public String[] getOutputAxes()
	{
		if( output_axes != null )
			return output_axes;
		else
			return outputSpaceObj.getAxisLabels();
	}

	private static String spaceNameFromAxesLabels( Spaces spaces, String[] axes )
	{
		ArrayList<Space> candidateSpaces = spaces.getSpacesFromAxes(axes);
		if( candidateSpaces.size() == 1 )
			return candidateSpaces.get(0).getName();
		else
			return null;
	}

	public String getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getInputSpace() {
		return input_space;
	}

	@Override
	public String getOutputSpace() {
		return output_space;
	}
	
	public void setInputSpace( Space inputSpace ) {
		this.inputSpaceObj = inputSpace;
	}

	public void setOutputSpace( Space outputSpace ) {
		this.outputSpaceObj = outputSpace;
	}
	
	public Space getInputSpaceObj()
	{
		return inputSpaceObj;
	}

	public Space getOutputSpaceObj()
	{
		return outputSpaceObj;
	}

	public AbstractCoordinateTransform<T> setNameSpaces( String name, String in, String out )
	{
		this.name = name;
		this.input_space = in;
		this.output_space = out;
		return this;
	}
	
	public String toString()
	{
		return String.format("%s:(%s > %s)", name, input_space, output_space);
	}

	@Override
	public RealCoordinate apply( final RealCoordinate src, final RealCoordinate dst ) {

		final T t = getTransform();

//		RealCoordinate out = new RealCoordinate(t.numTargetDimensions());
		// this needs to work on subspaces correctly
		
		// the simple case
		if( src.getSpace().axesEquals( getInputSpaceObj() ))
		{
			t.apply(src, dst);
			dst.setSpace(getOutputSpaceObj());
			return dst;
		}

//		if( !src.getSpace().isSubspaceOf( getInputSpaceObj() ))
		if( ! getInputSpaceObj().isSubspaceOf( src.getSpace() ))
		{
			System.err.println("WARNING: input point's space does not match transforms space.\n" );
		}

		final int[] inPermParams = AxisUtils.findPermutation(
				src.getSpace().getAxisLabels(), getInputSpaceObj().getAxisLabels() );
		
		int nd = src.numDimensions(); // should this be a max over src, outputDims ?
		int[] perm = AxisUtils.fillPermutation(inPermParams, nd );

		RealTransformSequence totalTransform = new RealTransformSequence();
		RealComponentMappingTransform pre = new RealComponentMappingTransform( perm.length, perm );
		totalTransform.add(pre);
		totalTransform.add(t);
		totalTransform.apply(src, dst);
		
		// copy coordinate values from src for unaffected dimensions
		int j = t.numSourceDimensions();
		for( int i = t.numTargetDimensions(); i < dst.numDimensions() && j < src.numDimensions(); i++ )
			dst.setPosition( src.getDoublePosition(perm[j++]), i);

//		Space srcRem = src.getSpace().diff("", getInputSpaceObj());
//		System.out.println( "srcRem : " + Arrays.toString( srcRem.getAxisLabels() ));

		dst.setSpace(
				getOutputSpaceObj().union("", 
						src.getSpace().diff("", getInputSpaceObj())));

		return dst;
	}

	public RealCoordinate applyAppend( final RealCoordinate src ) {

		final T t = getTransform();

		final RealCoordinate dst = new RealCoordinate( t.numTargetDimensions() );
		if( src.getSpace().axesEquals( getInputSpaceObj() ))
		{
			t.apply(src, dst);
			dst.setSpace(getOutputSpaceObj());
			return src.append(dst);
		}

		if( ! getInputSpaceObj().isSubspaceOf( src.getSpace() ))
		{
			System.err.println("WARNING: input point's space does not match transforms space.\n" );
		}

		final int[] inPermParams = AxisUtils.findPermutation(
				src.getSpace().getAxisLabels(), getInputSpaceObj().getAxisLabels() );
		
		int nd = src.numDimensions(); // should this be a max over src, outputDims ?
		int[] perm = AxisUtils.fillPermutation(inPermParams, nd );

		RealTransformSequence totalTransform = new RealTransformSequence();
		RealComponentMappingTransform pre = new RealComponentMappingTransform( perm.length, perm );
		totalTransform.add(pre);
		totalTransform.add(t);
		totalTransform.apply(src, dst);
		dst.setSpace(getOutputSpaceObj());
		return src.append(dst);

//		// copy coordinate values from src for unaffected dimensions
//		int j = t.numSourceDimensions();
//		for( int i = t.numTargetDimensions(); i < dst.numDimensions() && j < src.numDimensions(); i++ )
//			dst.setPosition( src.getDoublePosition(perm[j++]), i);
//
////		Space srcRem = src.getSpace().diff("", getInputSpaceObj());
////		System.out.println( "srcRem : " + Arrays.toString( srcRem.getAxisLabels() ));
//
//		dst.setSpace(
//				getOutputSpaceObj().union("", 
//						src.getSpace().diff("", getInputSpaceObj())));
//
//		return dst;
	}

	public AxisPoint applyAxes( final AxisPoint src ) {

		final T t = getTransform();

		// check if this transform's input axes are a subspace
		// of the source point
		double[] in = new double[ t.numSourceDimensions() ];  // TODO optimize
		double[] out = new double[ t.numTargetDimensions() ];  

		if( getInputSpaceObj().isSubspaceOf( src.axisOrder() ))
		{
			src.localize( in, getInputAxes() );
		}
		else if( src.numDimensions() >= t.numSourceDimensions() )
		{
			System.err.println("WARNING: using first N dimensions of source point" );
			// if not, default to using the first N dimensions
			src.localize( in );
		}
		else
		{
			return null;
		}

		AxisPoint dst = src;
		t.apply( in, out );
		dst.setPositions( out, getOutputAxes() );
		return dst;

	}

}
