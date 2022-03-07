package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.ArraySpace;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.TranslationCoordinateTransform;
import org.janelia.saalfeldlab.ngff.vis.Vis;

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

//		makeData( zarr );

		// Try changing one or both of these to the empty string and see what happens
		final String imgSpace = "physical";
		final String cropSpace = "physical";

		Vis vis = new Vis(zarr).bdvOptions(BdvOptions.options().is2D());

		// show whole image
		BdvStackSource<?> bdv = vis.dataset(imgDataset).space(imgSpace).show();
		bdv.setDisplayRangeBounds(0, 255);
		
		// show crop
		vis.bdvOptions( x -> x.addTo(bdv)).dataset(imgCropDataset).space(cropSpace).show()
			.setDisplayRange(0, 255);

		zarr.close();
	}
	
	public static void makeData( final N5ZarrWriter zarr ) throws IOException
	{
		FinalInterval itvl = new FinalInterval( 128, 128 );
		FinalInterval cropitvl = Intervals.createMinMax( 10, 12, 73, 75 );

		Space si = new ArraySpace(imgDataset, 2);
		Space sci = new ArraySpace(imgCropDataset, 2);
		Space cp = Common.makeSpace("physical", "space", "micrometer", "x", "y");
		Space scp = Common.makeSpace("crop-physical", "space", "micrometer", "cx", "cy");

		ScaleCoordinateTransform tp = new ScaleCoordinateTransform( "to-physical", imgDataset, "physical", new double[]{2.2, 1.1});
		ScaleCoordinateTransform tcp = new ScaleCoordinateTransform( "to-crop-physical", imgCropDataset, "crop-physical", new double[]{2.2, 1.1});
		TranslationCoordinateTransform to = new TranslationCoordinateTransform( "offset", imgCropDataset, imgDataset, new double[]{10, 12});
		
		Space[] cSystems = new Space[]{si, sci, cp, scp };
		CoordinateTransform<?>[] cTransforms = new CoordinateTransform[]{ tp, tcp, to };

		makeDatasets( zarr, imgDataset, itvl, cSystems, cTransforms );
		makeDatasets( zarr, imgCropDataset, cropitvl, cSystems, cTransforms );


//		zarr.setAttribute(baseDataset, "coordinateSystems", cSystems);
//		zarr.setAttribute(baseDataset, "coordinateTransformations", cTransforms);
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
//		return BdvFunctions.show(srcCrop, opts);	
		bdv.setDisplayRangeBounds(0, 255);
	}
	
	public static void makeDatasets( N5ZarrWriter zarr, String dataset, Interval itvl,
			Space[] cSystems, CoordinateTransform<?>[] cTransforms ) throws IOException
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

		if( cSystems != null )
			zarr.setAttribute(dataset, "coordinateSystems", cSystems);

		if( cTransforms != null )
			zarr.setAttribute(dataset, "coordinateTransformations", cTransforms);
	}

}
