package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.graph.TransformGraph;
import org.janelia.saalfeldlab.ngff.graph.TransformPath;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.RandomAccessibleIntervalSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class BuildGraph {

	public static void main(String[] args) throws IOException {

		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));

		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );
		
		// get the transforms and spaces
		final TransformGraph graph = Common.buildGraph( zarr );
		

		// open and show base
//		RandomAccessibleIntervalSource<?> src = openSource(zarr, "img2d", graph, "" ); //array space
		RandomAccessibleIntervalSource<?> src = openSource(zarr, "img2d", graph, "um" );
		BdvOptions opts = BdvOptions.options().is2D();
		BdvStackSource<?> bdv = BdvFunctions.show(src, opts);
		
		// open and show crop
//		RandomAccessibleIntervalSource<?> srcCrop = openSource(zarr, "img2dcrop", graph, "" ); // array space
		RandomAccessibleIntervalSource<?> srcCrop = openSource(zarr, "img2dcrop", graph, "crop-um" );
		opts = opts.addTo(bdv);
		BdvFunctions.show(srcCrop, opts);
		
		zarr.close();
		System.out.println("done");
	}
	
	public static <T extends NumericType<T> & NativeType<T>> RandomAccessibleIntervalSource<T> openSource( N5Reader n5, String dataset, TransformGraph graph, String space ) throws IOException 
	{
		final CachedCellImg<T, ?> imgRaw = (CachedCellImg<T, ?>) N5Utils.open(n5, dataset);
		final RandomAccessibleInterval<T> img = Common.open( n5 , dataset);

		final AffineTransform3D xfm = graph.path("", space).get().totalAffine3D();
		return new RandomAccessibleIntervalSource<T>(img, Util.getTypeFromInterval(img), xfm, "img2d");	
	}

	public static void backups()
	{
//		for( RegistrationPath p : graph.allPaths("")) {	
//			System.out.println( p );
//		}

//		Space cropUm = Arrays.stream( spaces ).filter( s -> s.getName().equals("crop-um")).findFirst().get();
//		System.out.println( cropUm );
//		
//		HashMap<String, Space> namesToSpaces = graph.getNamesToSpaces();
//		graph.allPaths( "" ).stream().forEach( p -> {
//			RegistrationPath path = p;
//			Space spc = namesToSpaces.get( path.getEnd() );
//			System.out.println( spc );
//			System.out.println( spc.equals( cropUm ));
//			System.out.println( "" );
//		});

//		.filter( p -> namesToSpaces.get(p.getEnd()).equals(to)).findAny();
		
	}

}
