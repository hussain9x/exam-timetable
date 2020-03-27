
package examtimetable;

import java.time.LocalDate;

/**
 *
 * @author Hussain
 */
public class Period implements Comparable{
    private int id;
    
    private LocalDate date;
        

    @Override
    public String toString() {
        return " Date " + date + " - Period " + id;
    }

    public Period(int id, LocalDate date) {
        this.id = id;
        this.date = date;
        
    }
    
    

    public int getId() {
        return id;
       
    }

    @Override
    public int compareTo(Object o) {
        Period p = (Period) o;
        return this.date.compareTo(p.getDate());
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object obj) {
        Period p = (Period) obj;
        //System.out.println((this.id==p.getId()));
        return (this.id==p.getId());
    }
    
       
   
}
