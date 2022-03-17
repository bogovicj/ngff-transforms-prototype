package org.janelia.saalfeldlab.ngff.transforms;

import java.util.Arrays;
import java.util.List;

import org.janelia.saalfeldlab.n5.metadata.axes.AxisUtils;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.RealComponentMappingTransform;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.StackedRealTransform;

public class StackedCoordinateTransform extends AbstractCoordinateTransform<RealTransform> {

	private List<CoordinateTransform<?>> transforms;
	
	private transient Spaces spaces;
	
	private transient RealTransform totalTransform;

	public StackedCoordinateTransform( 
			final String name,
			final String inputSpace, final String outputSpace,
			List<CoordinateTransform<?>> transforms )
	{
		super( "stacked", name, inputSpace, outputSpace );
		this.transforms = transforms;
	}

	public void setSpaces( Spaces spaces )
	{
		this.spaces = spaces;
	}

	public String[] inputAxesLabels()
	{
		return  transforms.stream().flatMap( t -> Arrays.stream(t.getInputAxes())).toArray( String[]::new );
	}

	public String[] outputAxesLabels()
	{
		return  transforms.stream().flatMap( t -> Arrays.stream(t.getOutputAxes())).toArray( String[]::new );
	}

	public RealTransform buildTransform()
	{
		final RealTransform[] arr = transforms.stream()
				.map( x -> (RealTransform)x.getTransform() )
				.toArray( RealTransform[]::new );

		final StackedRealTransform stackedTransform = new StackedRealTransform(arr);

		if( spaces != null )
		{
			String[] inputAxisLabels = spaces.getSpace(getInputSpace()).getAxisLabels();
			String[] tformInputAxisLabels = inputAxesLabels();

			String[] outputAxisLabels = spaces.getSpace(getOutputSpace()).getAxisLabels();
			String[] tformOutputAxisLabels = outputAxesLabels();
			
			System.out.println( Arrays.toString(inputAxisLabels));
			System.out.println( Arrays.toString(tformInputAxisLabels));
			System.out.println( "" );
			System.out.println( Arrays.toString(outputAxisLabels));
			System.out.println( Arrays.toString(tformOutputAxisLabels));
			
			RealTransform pre = null;
			if( !Arrays.equals(inputAxisLabels, tformInputAxisLabels))
			{
				final int[] inPermParams = AxisUtils.findPermutation(inputAxisLabels, tformInputAxisLabels);
//				final int[] inPermParams = AxisUtils.findPermutation(tformInputAxisLabels, inputAxisLabels );
				pre = new RealComponentMappingTransform( inPermParams.length, inPermParams);
				
				RealPoint p = new RealPoint( 1, 2, 3, 4, 5 );
				RealPoint q = new RealPoint( 0, 0, 0, 0, 0 );
				pre.apply( p, q );

				System.out.println( " " );
				System.out.println( "p: " + p );
				System.out.println( "q: " + q );
			}

			RealTransform post = null;
			if( !Arrays.equals(outputAxisLabels, tformOutputAxisLabels))
			{
				final int[] outPermParams = AxisUtils.findPermutation(tformOutputAxisLabels, outputAxisLabels );
//				final int[] outPermParams = AxisUtils.findPermutation( outputAxisLabels, tformOutputAxisLabels );
				post = new RealComponentMappingTransform( outPermParams.length, outPermParams);
				
				RealPoint p = new RealPoint( 1, 2, 3, 4, 5 );
				RealPoint q = new RealPoint( 0, 0, 0, 0, 0 );
				post.apply( p, q );
				System.out.println( " "  );
				System.out.println( "p: " + p );
				System.out.println( "q: " + q );
			}

			if( pre == null && post == null )
			{
				totalTransform = stackedTransform;
				return totalTransform;
			}
			else
			{
				final RealTransformSequence seq = new RealTransformSequence();
				if( pre != null )
					seq.add( pre );

				seq.add(stackedTransform);

				if( post != null )
					seq.add( post );

				totalTransform = seq;
				return totalTransform;
			}
		}
		else
		{
			totalTransform = stackedTransform;
			return totalTransform;
		}
	}

	@Override
	public RealTransform getTransform() {
		if( totalTransform == null )
			buildTransform();
		
		return totalTransform;
	}

}
