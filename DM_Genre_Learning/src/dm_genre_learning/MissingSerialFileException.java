
package dm_genre_learning;

import java.io.FileNotFoundException;


class MissingSerialFileException extends FileNotFoundException{

    public MissingSerialFileException(Exception e) {
        super();
        addSuppressed(e);
    }

}