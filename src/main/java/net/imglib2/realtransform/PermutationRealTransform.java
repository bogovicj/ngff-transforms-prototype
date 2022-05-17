package net.imglib2.realtransform;

import java.util.stream.IntStream;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealPositionable;

public class PermutationRealTransform implements RealTransform {

    protected final int numSourceDimensions;

    protected final int numTargetDimensions;

    protected final int[] inverseIndexes; // ith element stores the source index for the ith output dimension

    protected final double[] tmp;

    public PermutationRealTransform( int numSourceDimensions, int numTargetDimensions ) {
        this.numSourceDimensions = numSourceDimensions;
        this.numTargetDimensions = numTargetDimensions;
        inverseIndexes = IntStream.rangeClosed( 0, numSourceDimensions - 1 ).toArray();
		tmp = new double[ numTargetDimensions ];
    }

    public PermutationRealTransform( int numSourceDimensions, int[] inverseIndexes ) {
        this.numSourceDimensions = numSourceDimensions;
        this.numTargetDimensions = inverseIndexes.length;
        this.inverseIndexes = inverseIndexes;
        tmp = new double[ numTargetDimensions ];
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

        for( int i = 0; i < numTargetDimensions; i++ )
            tmp[i] = src[ inverseIndexes[i] ];

        if( tmp != dst )
            System.arraycopy( tmp, 0, dst, 0, tmp.length );
    }

    @Override
    public void apply(RealLocalizable src, RealPositionable dst) {

        for( int i = 0; i < numTargetDimensions; i++ )
        	tmp[i] = src.getDoublePosition( inverseIndexes[i] );

		dst.setPosition( tmp );
    }

    @Override
    public RealTransform copy() {
        return new PermutationRealTransform( numSourceDimensions, inverseIndexes );
    }

    public static void main( String[] args )
    {
        RealPoint p = new RealPoint( 1.0 );
        RealPoint q = new RealPoint( 3 );

        PermutationRealTransform xfmUp = new PermutationRealTransform( 1, new int[]{ 0, 0, 0 });
        xfmUp.apply(p,q );
        System.out.println(p);
        System.out.println(q); // should be [1,1,1]
        System.out.println("");
        
        p = new RealPoint( 1.0, 2.0, 3.0 );
        q = new RealPoint( 3 );

        PermutationRealTransform perm = new PermutationRealTransform( 3, new int[]{ 1, 2, 0 });
        perm.apply(p,q );
        System.out.println(p);
        System.out.println(q); // should be [ 2, 3, 1 ]
        System.out.println("");
    }
}
