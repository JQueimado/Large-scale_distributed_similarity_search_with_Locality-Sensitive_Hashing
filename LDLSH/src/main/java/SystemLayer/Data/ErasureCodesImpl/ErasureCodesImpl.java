package SystemLayer.Data.ErasureCodesImpl;

import SystemLayer.Containers.DataContainer;
import SystemLayer.Data.DataObjectsImpl.DataObject;

import java.util.Arrays;

public abstract class ErasureCodesImpl implements ErasureCodes{

    public DataContainer appContext;
    public ErasureBlock[] erasureBlocks;
    public int number_of_blocks;
    public int total_blocks;

    public ErasureCodesImpl( DataContainer appContext, int total_blocks ){
        this.appContext = appContext;
        this.total_blocks = total_blocks;
        this.erasureBlocks = new ErasureBlock[total_blocks];
        this.number_of_blocks = 0;
    }

    @Override
    public void encodeDataObject(DataObject dataObject, int n_blocks) {

    }

    @Override
    public DataObject decodeDataObject(DataObject object) throws IncompleteBlockException {
        return null;
    }

    @Override
    public void addBlockAt(ErasureBlock erasureBlock) {
        if( erasureBlocks[erasureBlock.position()] == null )
            number_of_blocks++;
        erasureBlocks[erasureBlock.position()] = erasureBlock;
    }

    @Override
    public ErasureBlock[] getErasureBlocks() {
        return erasureBlocks;
    }

    @Override
    public ErasureBlock getBlockAt(int position) {
        return erasureBlocks[position];
    }

    @Override
    public int compareTo(ErasureCodes o) {
        for ( int i = 0; i<erasureBlocks.length; i++ ){
            ErasureBlock A_block = erasureBlocks[i];
            ErasureBlock B_block = o.getBlockAt(i);

            if( A_block == null && B_block != null )
                return -1;

            if( B_block == null && A_block != null )
                return -1;

            int r = Arrays.compare(A_block.block_data(), B_block.block_data());
            if( r != 0 )
                return -1;
        }
        return 0;
    }

    //Subclasses
    public static final record ErasureBlock( byte[] block_data, int position ) implements Comparable<ErasureBlock> {
        @Override
        public int compareTo(ErasureBlock o) {
            if ( position != o.position )
                return -1;
            return Arrays.compare(block_data, o.block_data);
        }

        @Override
        public boolean equals(Object obj) {
            if( obj.getClass() != ErasureBlock.class )
                return false;
            return this.compareTo( (ErasureBlock) obj ) == 0 ;
        }
    }

    public static class IncompleteBlockException extends Exception{
        public IncompleteBlockException( String message ){
            super(message);
        }
        public IncompleteBlockException(){
            super();
        }
    }
}
