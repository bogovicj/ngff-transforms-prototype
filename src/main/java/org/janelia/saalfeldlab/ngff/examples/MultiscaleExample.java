package org.janelia.saalfeldlab.ngff.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.janelia.saalfeldlab.n5.metadata.MultiscaleDatasetsTemplate;
import org.janelia.saalfeldlab.n5.zarr.N5ZarrWriter;
import org.janelia.saalfeldlab.ngff.multiscales.DatasetTransform;
import org.janelia.saalfeldlab.ngff.multiscales.Multiscale;
import org.janelia.saalfeldlab.ngff.spaces.Space;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.CoordinateTransformAdapter;
import org.janelia.saalfeldlab.ngff.transforms.RealCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.ScaleCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.SequenceCoordinateTransform;
import org.janelia.saalfeldlab.ngff.transforms.TranslationCoordinateTransform;

import com.google.gson.GsonBuilder;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.realtransform.Scale;
import net.imglib2.realtransform.Translation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Intervals;
import net.imglib2.view.SubsampleIntervalView;


public class MultiscaleExample {

	public static void main(String[] args) throws IOException {

		final String root = "/home/john/projects/ngff/transformsExamples/data.zarr";
		final String baseDataset = "/multiscales";

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(CoordinateTransform.class, new CoordinateTransformAdapter(null));
		final N5ZarrWriter zarr = new N5ZarrWriter(root, gsonBuilder );

		double r = 128;
		ArrayImg<DoubleType, DoubleArray> img = ArrayImgs.doubles(1028, 1028 );
		Stream<RealPoint> pts = Common.circleSamples( new RealPoint( 256, 256 ), r, r/4 );

		System.out.println( "render" );
		Common.render(img, pts.limit(80), r );

		buildMultiscales(img, zarr, baseDataset + "/sample", 3, false);
		buildMultiscales(img, zarr, baseDataset + "/avg", 3, true);

//		SubsampleIntervalView<DoubleType> imgD = Common.downsample(img, 2, 2);
//		Img<DoubleType> imgDavg = Common.downsampleAvg(img, 2 );
		
//		BdvStackSource<DoubleType> bdv = BdvFunctions.show(img,"circs");
//		BdvOptions opts = BdvOptions.options().addTo(bdv);
//		BdvFunctions.show( imgD, "circs Down", opts);
//
//		BdvFunctions.show( imgDavg, "circs Down Avg", opts);
	}

	public static <T extends RealType<T> & NativeType<T>> void buildMultiscales( Img<T> imgBase, N5Writer n5, String base, int nScales, boolean avg ) throws IOException
	{
		final DatasetTransform[] datasets = new DatasetTransform[ nScales ];

		final String spaceName = "physical";
		Space[] spaces = new Space[]{
			Common.makeSpace(spaceName, "space", "um", "x", "y")
		};

		RandomAccessibleInterval<T> img = imgBase;
		final GzipCompression compression = new GzipCompression();

		final int[] blkSize = new int[ imgBase.numDimensions()];
		Arrays.fill(blkSize, 64);

		Scale df = new Scale( 2, 2 );	
		double sx = 2.2;
		double sy = 3.3;
		Scale s = new Scale( sx, sy ); // s0 to physical
//		Translation xlation = new Translation( 0, 0 );

		for (int i = 0; i < nScales; i++) {
			String si = String.format("s%d", i);
			String dset = String.format("%s/%s", base, si);
			System.out.println(dset + " " + Intervals.toString(img));

			N5Utils.save(img, n5, dset, blkSize, compression);
			
			CoordinateTransform<?> t = null;
			ScaleCoordinateTransform scale = new ScaleCoordinateTransform(si+"-to-physical", "", spaceName, s.getScaleCopy());
			if( avg )
			{
//				xlation = new Translation( sx * ((df.getScale(0) - 1)/ 2), sy * (df.getScale(1) - 1)/ 2);
				double[] xlation = new double[] { sx * ((df.getScale(0) - 1)/ 2), sy * (df.getScale(1) - 1)/ 2 };
				TranslationCoordinateTransform xlationct = new TranslationCoordinateTransform("", "", "", xlation);

				t = new SequenceCoordinateTransform(si + "-to-physical", "", spaceName,
						new RealCoordinateTransform[]{ scale, xlationct});
			}
			else {
				t = scale;
			}

			datasets[i] = new DatasetTransform(dset, t );
					
			s.preConcatenate(df);
			
			if( spaces != null )
				n5.setAttribute(dset, "spaces", spaces);

			if( datasets[i] != null )
				n5.setAttribute(dset, "transformations", datasets[i].coordinateTransformations );

			if (avg)
				img = Common.downsampleAvg(img, 2);
			else
				img = Common.downsample(img, 2, 2);
		}
		
//		if( spaces != null )
//			n5.setAttribute(base, "spaces", spaces);

		Multiscale ms = new Multiscale();
		ms.version = "0.5-prototype";
		ms.name = avg ? "ms_avg" : "ms_sample";
		ms.type = avg ? "averaging" : "sampling";
		ms.spaces = spaces;
		ms.datasets = datasets;

		n5.setAttribute(base, "multiscales", new Multiscale[]{ ms });

	}

}
