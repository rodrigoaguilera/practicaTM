
import java.util.Comparator;

public class CompareFilename implements Comparator<Imagen>{

    
    @Override
    public int compare(Imagen t, Imagen t1) {
        return t.getFilename().compareTo(t1.getFilename());
    }

    }