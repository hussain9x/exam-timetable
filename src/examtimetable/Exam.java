
package examtimetable;

/**
 *
 * @author Hussain
 */
public class Exam implements Comparable{
private int id;
int size;
   
@Override
    public int compareTo(Object o) {
        
        int s = ((Exam) o).getSize();
        return this.size- s;
    }

    public int getSize() {
        return size;
    }
    
     public void increaseSize(){
        this.size++;
    }
     

    @Override
    public String toString() {
        return "Exam " + id + " Size=" +size;
    }
   
   

    
public int getId(){
    return id;
}

    
@Override
    public boolean equals(Object obj) {
      Exam o = (Exam) obj;
      return (o.getId()==this.id);
    }
   
   public Exam(int examId, int size){
       this.id = examId;
       this.size=size;
   } 
    
}
