package org.janelia.saalfeldlab.ngff.transforms;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.janelia.saalfeldlab.ngff.SpacesTransforms;
import org.janelia.saalfeldlab.ngff.axes.AxisUtils;
import org.janelia.saalfeldlab.ngff.examples.Common;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.realtransform.RealTransformSequence;
import net.imglib2.realtransform.SubsetRealTransform;

public class SubspaceTransformsDemo
{
	public static void ex01234abcdefZ() throws FileNotFoundException
	{
		final String testDataF = "src/test/resources/multiLevelAxisGraph2.json";
		SpacesTransforms st = SpacesTransforms.loadFile(testDataF);

		Spaces spaces = st.buildSpaces( 5 ); // 5 indicates that 
		List< CoordinateTransform< ? > > transforms = st.buildTransformGraph().getTransforms();

		final Space inputSpace = spaces.getSpace( "" );
		final Space outputSpace = spaces.getSpace( "z" );
		SequenceCoordinateTransform ct = buildTransformFromAxes( spaces, transforms, inputSpace, outputSpace );

		testAxisPermutations( ct.getTransformations(), inputSpace, outputSpace );
		
		
//		RealTransform totalTransform = ct.getTransformSubspaces();
//		System.out.println( "tform src dims: " + totalTransform.numSourceDimensions());
//		System.out.println( "tform tgt dims: " + totalTransform.numTargetDimensions());
//		
//		RealPoint p = new RealPoint( 1.0, 1.0, 1.0, 1.0, 1.0 );
//		RealPoint q = new RealPoint( 1 );
//		totalTransform.apply( p, q );
//		System.out.println( "" );
//		System.out.println( q );
	}
	
	
	public static void ij2xy()
	{
		Spaces spaces = new Spaces();
		spaces.add( Common.makeSpace( "in", "space", "nm", "i", "j" ));
		spaces.add( Common.makeSpace( "out", "space", "nm", "x", "y" ));
		
		ArrayList<CoordinateTransform<?>> transforms = new ArrayList<>();
		transforms.add( new ScaleCoordinateTransform( "i>x", new String[] {"i"}, new String[]{"x"}, new double[]{2} ));
		transforms.add( new ScaleCoordinateTransform( "j>y", new String[] {"j"}, new String[]{"y"}, new double[]{3} ));
		spaces.updateTransforms( transforms.stream() );

		SequenceCoordinateTransform ct = buildTransformFromAxes( spaces, transforms, spaces.getSpace( "in" ), spaces.getSpace( "out" ));



//		RealTransform totalTransform = ct.getTransform();
//		System.out.println( "tform src dims: " + totalTransform.numSourceDimensions());
//		System.out.println( "tform tgt dims: " + totalTransform.numTargetDimensions());

//		RealPoint p = new RealPoint( 1.0, 1.0 );
//		RealPoint q = new RealPoint( 2 );
//		totalTransform.apply( p, q );
//		System.out.println( "" );
//		System.out.println( q ); // expect [2,3]
	}

	public static void main( String[] args ) throws FileNotFoundException
	{
//		ij2xy();
		ex01234abcdefZ();
	}
	
	public static SequenceCoordinateTransform buildTransformFromAxes( 
			Spaces spaces, List<CoordinateTransform<?>> transforms,
			final Space from, final Space to )
	{
		final List<CoordinateTransform<?>> tList = new ArrayList<>();
		
		// keep track of all input and output axes 
		final HashSet<String> transformInputAxes = new HashSet<>();
		final HashSet<String> transformOutputAxes = new HashSet<>();

		final String[] toAxes = to.getAxisLabels();
		HashSet<String> toAxesRemaining = new HashSet<>();
		toAxesRemaining.addAll( Arrays.asList( toAxes ));

		final String[] fromAxes = from.getAxisLabels();
		HashSet<String> fromAxesRemaining = new HashSet<>();
		fromAxesRemaining.addAll( Arrays.asList( fromAxes ));

		System.out.println( "fromAxesRemaining: " + fromAxesRemaining );
		System.out.println( "toAxesRemaining: " + toAxesRemaining );

		while( !fromAxesRemaining.isEmpty() )
		{
			boolean anyChanged = false;
			for( CoordinateTransform<?> t : transforms )
			{
				String[] tInputs = spaces.getInputAxes(t);
				String[] tOutputs = spaces.getOutputAxes(t);
				
				System.out.println( "t in : " + Arrays.toString(tInputs) );
				System.out.println( "t out: " + Arrays.toString(tOutputs) );
				
				if( tList.contains( t ))
					continue;

				// if 
				if( spaces.outputMatchesAny(t, toAxesRemaining))
				{
					if( AxisUtils.containsAny( transformOutputAxes, tOutputs ))
					{
						System.err.println( "warning: multiple transforms define same output axes");
						return null;
					}

					if( AxisUtils.containsAny( transformInputAxes, tInputs ))
					{
						System.err.println( "warning: multiple transforms define same output axes");
						return null;
					}

					for( String out : tOutputs )
					{
						if( !toAxesRemaining.remove(out) )
							fromAxesRemaining.add(out);
					}
					
					for( String in : tInputs )
					{
						if( !fromAxesRemaining.remove(in) )
							toAxesRemaining.add(in);
					}

					System.out.println( "fromAxesRemaining: " + fromAxesRemaining );
					System.out.println( "toAxesRemaining: " + toAxesRemaining );
					System.out.println( "" );

					anyChanged = true;
					tList.add(t);

					transformOutputAxes.addAll( Arrays.asList( tOutputs ));
					transformInputAxes.addAll( Arrays.asList( tInputs ));
				}
				System.out.println( " " );
			}

			// if anyChanged = false, it means we
			// iterated through all transformations without making any progress,
			// so no progress can be made, so terminate.
			if( !anyChanged )
				break;
		}
		
		if( !fromAxesRemaining.isEmpty() )
		{
			System.err.println( "path to some source axes has not been found, but this is fine");
		}

		// need to reverse the list because
		// we build it from output to input
		Collections.reverse(tList);
		final SequenceCoordinateTransform totalTransform = new SequenceCoordinateTransform( "total", from.getName(), to.getName(), tList );
		totalTransform.setInputSpace(from);
		totalTransform.setOutputSpace(to);

		RealCoordinate tmpPtSrc = new RealCoordinate( from.numDimensions(), from );
		tmpPtSrc.positionToIndexes();
		RealCoordinate tmpPtDst = totalTransform.applyAppend( tmpPtSrc );
		System.out.println( "dst : " + tmpPtDst.getSpace() );
		
		if( to.isSubspaceOf( tmpPtDst.getSpace() ))
		{
			final int[] p = AxisUtils.findPermutation( tmpPtDst.getSpace().getAxisLabels(), to.getAxisLabels());
			final double[] permMatrix = AxisUtils.matrixFromPermutation(p, tmpPtDst.getSpace().numDimensions());

			return totalTransform;
		}
		else
		{
			System.err.println("theres a problem");
			return null;
		}

	}
	
	public static void testAxisPermutations( CoordinateTransform< ? >[] tforms, Space in, Space out )
	{
		System.out.println( "testAxisPermutations" );


		System.out.println( "" );
		ArrayList< HashSet< String > > requiredAxes = SequenceCoordinateTransform.cumNeededAxes( tforms );
		System.out.println( "" );

		for( HashSet<String> set : requiredAxes )
		{
			System.out.println( set.toString());
		}
		
		ArrayList< List<String> > axOrders = new ArrayList<>();
		ArrayList< int[] > permutations = new ArrayList<>();

		List<String> axes = new ArrayList<>();
		Collections.addAll( axes, in.getAxisLabels() );

		// TODO what if intermediate results need to be bigger
		int N = axes.size();;
		axOrders.add( axes );
		
		int freeIndex = 0;

		System.out.println( "" );
		System.out.println( "loop" );
		System.out.println( "" );
		int k = 0;
		for( CoordinateTransform ct : tforms )
		{

			String[] tAxIn = ct.getInputAxes();
			String[] tAxOut = ct.getOutputAxes();
			String[] axArray = axes.stream().toArray( String[]::new );

			System.out.println( "ax : " + Arrays.toString( axArray ));
			System.out.println( "ti : " + Arrays.toString( tAxIn ));
			System.out.println( "to : " + Arrays.toString( tAxOut ));

			final int[] p = AxisUtils.fillPermutation( AxisUtils.findPermutation( axArray, tAxIn ), N );
			System.out.println( "p : " + Arrays.toString( p ));
			
			axes = AxisUtils.permute( axes, p );
			System.out.println( "axp: " + axes.stream().collect(Collectors.joining(" ")));
			
			for( int i = 0; i < tAxOut.length; i++ )
				axes.set( i, tAxOut[i] );

			System.out.println( "axs: " + axes.stream().collect(Collectors.joining(" ")));

			// do any future transforms need axes that we're currently storing?
			final HashSet<String> reqAxis = requiredAxes.get( k+1 );
			for( int i = 0; i < axes.size(); i++ )
			{
				if( !reqAxis.contains( axes.get( i ) ))
					axes.set( i, "FREE" + (freeIndex++) );

			}
			
			System.out.println( "axf: " + axes.stream().collect(Collectors.joining(" ")));


			System.out.println( " " );
			k++;
		}
		
	}

}
