package org.janelia.saalfeldlab.ngff.transforms;

import java.io.IOException;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.img.cell.CellCursor;
import net.imglib2.realtransform.AffineGet;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

public abstract class AbstractLinearCoordinateTransform<T extends AffineGet,P> extends AbstractParametrizedTransform<T,P> implements LinearCoordinateTransform<T> {

	public AbstractLinearCoordinateTransform( String type, String name, 
			String inputSpace, String outputSpace ) {
		super( type, name, inputSpace, outputSpace );
	}

	public AbstractLinearCoordinateTransform( String type, String name, String parameterPath, 
			String inputSpace, String outputSpace ) {
		super( type, name, parameterPath, inputSpace, outputSpace );
	}

	@Override
	public abstract T getTransform();

	@Override
	public abstract T buildTransform( P parameters );

	protected static <T extends RealType<T> & NativeType<T>> double[] getDoubleArray(final N5Reader n5, final String path) {
		if (n5.exists(path)) {
			try {
				@SuppressWarnings("unchecked")
				CachedCellImg<T, ?> data = (CachedCellImg<T, ?>) N5Utils.open(n5, path);
				if (data.numDimensions() != 1 || !(Util.getTypeFromInterval(data) instanceof RealType))
					return null;

				double[] params = new double[(int) data.dimension(0)];
				CellCursor<T, ?> c = data.cursor();
				int i = 0;
				while (c.hasNext())
					params[i++] = c.next().getRealDouble();

				return params;
			} catch (IOException e) { }
		}
		return null;
	}

	protected static <T extends RealType<T> & NativeType<T>> double[][] getDoubleArray2(final N5Reader n5, final String path) {
		if (n5.exists(path)) {
			try {
				@SuppressWarnings("unchecked")
				CachedCellImg<T, ?> data = (CachedCellImg<T, ?>) N5Utils.open(n5, path);
				if (data.numDimensions() != 2 || !(Util.getTypeFromInterval(data) instanceof RealType))
					return null;

				double[][] params = new double[(int) data.dimension(0)] [(int) data.dimension(1)];
				CellCursor<T, ?> c = data.cursor();
				while (c.hasNext()) {
					c.fwd();
					params[c.getIntPosition(0)][c.getIntPosition(1)] = c.get().getRealDouble();
				}

				return params;
			} catch (IOException e) { }
		}
		return null;
	}

}
