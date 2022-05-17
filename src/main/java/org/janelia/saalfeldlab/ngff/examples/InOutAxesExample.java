package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.RealCoordinate;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.spaces.Spaces;
import org.janelia.saalfeldlab.ngff.transforms.AffineCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.SequenceCoordinateTransform;
import org.janelia.saalfeldlab.ngff.vis.Vis;

import com.google.gson.GsonBuilder;

import net.imglib2.FinalInterval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.realtransform.RealTransform;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class InOutAxesExample
{
	
	public static void makeData( final N5ZarrWriter zarr, final String baseDataset ) throws IOException
	{
		final Spaces spaces = makeSpaces();
		final ArrayList<CoordinateTransform<?>> transforms = new ArrayList<>();


		final AffineCoordinateTransform xyt = 
			new AffineCoordinateTransform( "xy", new String[]{"dim_0", "dim_1"}, new String[]{"x", "y"}, 
					new double[]{0.8, 0.1, -3,  0.22, 0.7, 99} );

		final ScaleCoordinateTransform zt =
			new ScaleCoordinateTransform( "z", new String[]{"dim_2"}, new String[]{"z"}, new double[]{2.2} );
		
		transforms.add( new SequenceCoordinateTransform( "toPhysical", baseDataset, "physical", 
				new CoordinateTransform[]{ xyt, zt }));

		N5Utils.save( makeImg(), zarr, baseDataset, new int[] {32,32,32}, new GzipCompression() );
		zarr.setAttribute(baseDataset, "spaces", spaces.spaces().toArray(Space[]::new));
		zarr.setAttribute(baseDataset, "transformations", transforms);
	}

	public static Spaces makeSpaces()
	{
		final Spaces spaces = new Spaces();
		spaces.add( Common.makeSpace("physical", "space", "pixel", "x", "y", "z"));
		return spaces;
	}
	
	public static IntervalView< FloatType > makeImg()
	{
		return Views.interval( 
					new FunctionRandomAccessible<FloatType>( 3, 
						(p,v) -> {
							v.setReal(
								Math.sin( p.getDoublePosition(0) / 2) *
								Math.sin( p.getDoublePosition(1) / 4 ) *
								Math.sin( p.getDoublePosition(2) / 8 ) );
						}, 
						FloatType::new ),
					new FinalInterval(64,64,64));
	}

	public static void visData( final N5ZarrWriter zarr, final String baseDataset ) throws IOException
	{
		Vis vis = new Vis(zarr).dataset( baseDataset ).space("physical");
		TransformGraph g = vis.getGraph();
//		g.getSpaces().updateTransforms( g.getTransforms().stream() );

		TransformPath p = g.path(baseDataset, "physical" ).get();
		System.out.println( p );
		
//		RealTransform totalXfm = p.totalTransform();
		CoordinateTransform< ? > t = p.flatTransforms().get( 0 );

		RealCoordinate srcPt = new RealCoordinate( 3, g.getSpaces().getSpace( baseDataset ));
		srcPt.positionToIndexes();
		RealCoordinate tmpPtDst = t.applyAppend( srcPt );
		System.out.println( "dst : " + tmpPtDst.getSpace() );

//		System.out.println( g );
//		List< CoordinateTransform< ? > > tforms = g.getTransforms();
//		System.out.println( tforms );
//
//		vis.show();
	}

	public static void main( String[] args ) throws IOException
	{
		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
//		final String root = "/groups/saalfeld/home/bogovicj/projects/ngff/transformsExamples/data.zarr";
		final String baseDataset = "/inputOutputAxes";

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

//		makeData(zarr, baseDataset);
		visData( zarr, baseDataset );
	}

}
