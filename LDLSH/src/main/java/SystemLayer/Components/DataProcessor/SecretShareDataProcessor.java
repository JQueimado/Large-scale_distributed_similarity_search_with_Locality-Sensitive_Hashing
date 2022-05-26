package SystemLayer.Components.DataProcessor;

import SystemLayer.Containers.DataContainer;
import SystemLayer.Data.DataObjectsImpl.DataObject;
import SystemLayer.Data.LSHHashImpl.LSHHash;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecretShareDataProcessor extends DataProcessorImpl{

    //Adicionar chave aos codigos

    //Static
    public static final String config_seed = "IV_SEED";

    //Objects
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";

    private SecretKey key;
    private String iv_seed;
    private byte[] iv;


    /**
     * Standard Constructor
     * @param appContext application state
     */
    public SecretShareDataProcessor(DataContainer appContext){
        super(appContext);
        this.iv_seed = appContext.getConfigurator().getConfig(config_seed);
    }

    @Override
    public ProcessedData preProcessData(DataObject object) throws Exception {
        LSHHash lshHash = appContext.getLshHashFactory().getNewLSHHash();

        return null;
    }

    @Override
    public DataObject postProcess(){ return null; }

    /**
     * Configures the Cipher with the predetermined algorithm and key;
     * @param seed seed used to generate IV and cypher key
     */
    private void setup( String seed ){
        SecureRandom secureRandom = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8)); //Create pseudo random

        //Create key
        try {
            createAESKey(secureRandom);
        }catch (NoSuchAlgorithmException nsae){
            System.out.println("Invalid Cypher algorithm");
        }

        //Create init Vector
        setInitializationVector(secureRandom);
    }

    /**
     * Creates a AES cipher key using a predetermined random number generator
     * @param secureRandom random number generator
     * @throws NoSuchAlgorithmException Thrown if the set encryption algorithm does not exist or is not supported by the
     * JAVA KeyGenerator.
     */
    private void createAESKey( SecureRandom secureRandom ) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256, secureRandom);
        key = keyGenerator.generateKey();
    }

    /**
     * Returns a pseudo random number array, generated using a given random number generator
     * @param secureRandom random number generator.
     */
    private void setInitializationVector( SecureRandom secureRandom ){
        iv = new byte[16];
        secureRandom.nextBytes(iv);
    }

    private byte[] cipher( byte[] data ){
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            return cipher.doFinal(data);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] decipher( byte[] data ){
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            return cipher.doFinal(data);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
