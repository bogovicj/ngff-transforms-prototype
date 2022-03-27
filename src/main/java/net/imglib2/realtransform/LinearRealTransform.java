package net.imglib2.realtransform;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;

public class LinearRealTransform implements RealTransform {

    protected final int numSourceDimensions;

    protected final int numTargetDimensions;

    protected final double[] matrix; // stored row major

    public LinearRealTransform( int numSourceDimensions, int numTargetDimensions ) {
        this.numSourceDimensions = numSourceDimensions;
        this.numTargetDimensions = numTargetDimensions;
        matrix = new double[ numSourceDimensions * numTargetDimensions ];
    }

    public LinearRealTransform( int numSourceDimensions, int numTargetDimensions, double[] matrix ) {
        this.numSourceDimensions = numSourceDimensions;
        this.numTargetDimensions = numTargetDimensions;
        // TODO check size
        this.matrix = matrix;
    }

    @Override
    public int numSourceDimensions() {
        return numSourceDimensions;
    }

    @Override
    public int numTargetDimensions() {
        return numTargetDimensions;
    }

    @Override
    public void apply(double[] src, double[] dst) {

        double[] tgt;
        if( src == dst )
            tgt = new double[ numTargetDimensions ];
        else
            tgt = dst;

        int k = 0;
        for( int j = 0; j < numTargetDimensions; j++ )
        {
            tgt[j] = 0;
            for( int i = 0; i < numSourceDimensions; i++ )
                tgt[j] += matrix[k++] * src[i];

            System.out.println( tgt[j]);

        }

        if( tgt != dst )
            System.arraycopy( tgt, 0, dst, 0, tgt.length );

    }

    @Override
    public void apply(RealLocalizable src, RealPositionable dst) {

        RealPositionable tgt;
        if( src == dst )
            tgt = new RealPoint( numTargetDimensions );
        else
            tgt = dst;

        int k = 0;
        double val = 0;
        for( int j = 0; j < numTargetDimensions; j++ )
        {
            val = 0;
            for( int i = 0; i < numSourceDimensions; i++ ) {
                val += matrix[k++] * src.getDoublePosition(i);
            }
            tgt.setPosition( val, j );
        }

        if( tgt != dst )
            dst.setPosition( (RealPoint) tgt);
    }

    @Override
    public RealTransform copy() {
        // TODO copy matrix data
        return new LinearRealTransform( numSourceDimensions, numTargetDimensions, matrix );
    }

    public static void main( String[] args )
    {
        RealPoint p = new RealPoint( 1.0 );
        RealPoint q = new RealPoint( 3 );

        LinearRealTransform xfmUp = new LinearRealTransform( 1, 3, new double[]{ 2, 3, 4 });
        xfmUp.apply(p,q );
        System.out.println(p);
        System.out.println(q); // should be [2,3,4]
        System.out.println(""); // should be [2,3,4]

        LinearRealTransform xfmDown = new LinearRealTransform( 3, 1, new double[]{ 2, 3, 4 });
        xfmDown.apply(q,p );
        System.out.println(q);
        System.out.println(p);// should be [2*2 + 3*3 + 4*4 ] = [29]
    }
}
