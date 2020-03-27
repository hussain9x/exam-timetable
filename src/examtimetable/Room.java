
package examtimetable;

/**
 *
 * @author Hussain
 */
public class Room {
   private int id;
    private int size;

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "Room " + id + ", size=" + size + "\n";
    }
    
    public Room(int id, int size) {
        this.id = id;
        this.size = size;
    }
  
}
