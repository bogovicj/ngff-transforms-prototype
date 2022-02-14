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

		// Try changing one or both of these to the empty string and see what happens
		final String imgSpace = "um";
		final String cropSpace = "crop-um";

		// open and show base
		RandomAccessibleIntervalSource<?> src = Common.openSource(zarr, "img2d", graph, imgSpace );
		BdvOptions opts = BdvOptions.options().is2D();
		BdvStackSource<?> bdv = BdvFunctions.show(src, opts);

		// open and show crop
		RandomAccessibleIntervalSource<?> srcCrop = Common.openSource(zarr, "img2dcrop", graph, cropSpace );
		opts = opts.addTo(bdv);
		BdvFunctions.show(srcCrop, opts);
		
		zarr.close();
		System.out.println("done");
	}

}
