
package examtimetable;

import java.util.Map;

/**
 *
 * @author Hussain
 */
public class Booking implements Comparable{

    Exam exam;

    @Override
    public String toString() {
return "Booking: " + period.toString() + " - " + exam.toString() + " - " + "Rooms=" + rooms; 
    }
    Period period;
    Map<Integer,Room> rooms;

    public Exam getExam() {
        return exam;
    }

    public Period getPeriod() {
        return period;
    }

    public Map<Integer,Room> getRooms() {
        return rooms;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public void setRooms(Map<Integer,Room> rooms) {
        this.rooms = rooms;
    }

    public Booking(Exam exam, Period period, Map<Integer,Room> rooms) {
        this.exam = exam;
        this.period = period;
        this.rooms = rooms;
    }

    @Override
    public int compareTo(Object o) {
        Booking b = (Booking) o;
        return this.period.compareTo(b.getPeriod());
    }
    
    


}
