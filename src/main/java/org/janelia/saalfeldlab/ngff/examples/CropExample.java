package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.TranslationCoordinateTransform;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class CropExample {
	
	public static void main( String[] args ) throws IOException
	{
		final String root = args[ 0 ];
		FinalInterval itvl = new FinalInterval( 128, 128 );
		FinalInterval cropitvl = Intervals.createMinMax( 10, 12, 73, 75 );

		final Space[] spaces = new Space[] {
				Common.makeSpace("um", "space", "micrometer", "y", "z"),
				Common.makeSpace("crop-offset", "space", "pixels", "cj", "ci"),
				Common.makeSpace("crop-um", "space", "micrometer", "cy", "cz")
		};

		final CoordinateTransform[] transforms = new CoordinateTransform[] {
				new ScaleCoordinateTransform( "to-um", "", "um", new double[]{2.2, 1.1}),
				new ScaleCoordinateTransform( "crop-to-um", "crop-offset", "crop-um", new double[]{2.2, 1.1}),
				new TranslationCoordinateTransform( "offset", "", "crop-offset", new double[]{10, 12})
		};

		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

		if( ! zarr.datasetExists("/img2d") || ! zarr.datasetExists("/img2dcrop"))
		{
			makeDatasets( zarr, "/img2d", itvl, null, null );
			makeDatasets( zarr, "/img2dcrop", cropitvl, null, null );

			zarr.setAttribute("/", "spaces", spaces);
			zarr.setAttribute("/", "transformations", transforms);
		}

		// Try changing one or both of these to the empty string and see what happens
		final String imgSpace = "um";
		final String cropSpace = "crop-um";
		show( zarr, imgSpace, cropSpace );

		zarr.close();
	}

	public static void show( N5ZarrWriter zarr, String imgSpace, String cropSpace ) throws IOException 
	{
		// get the transforms and spaces
		final TransformGraph graph = Common.buildGraph( zarr );

		RandomAccessibleIntervalSource<?> src = Common.openSource(zarr, "img2d", graph, imgSpace );
		BdvOptions opts = BdvOptions.options().is2D();
		BdvStackSource<?> bdv = BdvFunctions.show(src, opts);

		// open and show crop
		RandomAccessibleIntervalSource<?> srcCrop = Common.openSource(zarr, "img2dcrop", graph, cropSpace );
		opts = opts.addTo(bdv);
		BdvFunctions.show(srcCrop, opts);	
	}
	
	public static void makeDatasets( N5ZarrWriter zarr, String dataset, Interval itvl,
			Space[] spaces, CoordinateTransform[] transforms ) throws IOException
	{
		final int[] blkSz = new int[]{64, 64};
		FunctionRandomAccessible<DoubleType> fimg = new FunctionRandomAccessible<>( 2,
				(p,v) -> {
					v.set( Math.sqrt(
						p.getDoublePosition(0) * p.getDoublePosition(0) + 
						p.getDoublePosition(1) * p.getDoublePosition(1)) );
				},
				DoubleType::new );

		IntervalView<DoubleType> img = Views.interval( fimg, itvl );
		N5Utils.save(img, zarr, dataset, blkSz, new GzipCompression());

		if( spaces != null )
			zarr.setAttribute(dataset, "spaces", spaces);

		if( transforms != null )
			zarr.setAttribute(dataset, "transforms", transforms);
	}

}
