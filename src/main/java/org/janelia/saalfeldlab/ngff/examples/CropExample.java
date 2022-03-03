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

	private static final String baseDataset = "/crop";
	private static final String imgDataset = "/crop/img2d";
	private static final String imgCropDataset = "/crop/img2dcrop";
	
	public static void main( String[] args ) throws IOException
	{
		final String root = args[ 0 ];
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

		makeData( zarr );

		// Try changing one or both of these to the empty string and see what happens
		final String imgSpace = "";
		final String cropSpace = "crop-offset";
		show( zarr, imgSpace, cropSpace );

		zarr.close();
	}
	
	public static void makeData( final N5ZarrWriter zarr ) throws IOException
	{
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

		if( ! zarr.datasetExists( imgDataset ) || ! zarr.datasetExists( imgCropDataset ))
		{
			makeDatasets( zarr, imgDataset, itvl, null, null );
			makeDatasets( zarr, imgCropDataset, cropitvl, null, null );

			zarr.setAttribute(baseDataset, "spaces", spaces);
			zarr.setAttribute(baseDataset, "transformations", transforms);
		}
	}

	public static void show( N5ZarrWriter zarr, String imgSpace, String cropSpace ) throws IOException 
	{
		// get the transforms and spaces
		final TransformGraph graph = Common.buildGraph( zarr, baseDataset );

		RandomAccessibleIntervalSource<?> src = Common.openSource(zarr, imgDataset, graph, imgSpace );
		BdvOptions opts = BdvOptions.options().is2D();
		BdvStackSource<?> bdv = BdvFunctions.show(src, opts);

		// open and show crop
		RandomAccessibleIntervalSource<?> srcCrop = Common.openSource(zarr, imgCropDataset, graph, cropSpace );
		opts = opts.addTo(bdv);
		BdvFunctions.show(srcCrop, opts);	
		bdv.setDisplayRangeBounds(0, 255);
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
