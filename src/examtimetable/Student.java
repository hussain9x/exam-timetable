
package examtimetable;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Hussain
 */
public class Student {
   private int id;
    
    private ArrayList<Exam> exams;
    
@Override
    public String toString(){
        String output = "Student ID: " + id + "\nExams: ";
        for (int i = 0; i<exams.size();i++){
            output+=exams.get(i).getId() + " ";
        }
        return output+"\n";
        
    }


    public int getId() {
        return id;
    }

    public ArrayList<Exam> getExams() {
        return exams;
    }
    
public void setId(int id){
    this.id=id;
    
}
    
    public Student(int id){
        this.id=id;
        this.exams= new ArrayList<Exam>();
        
    }
    public Student(int id, ArrayList<Exam> exams){
        this.id=id;
        this.exams = exams;
    }
    public Student(int id, String name, ArrayList<Exam> exams) {
        this.id = id;
        //this.name = name;
        this.exams = exams;
    }
    public void addSingleExam(Exam exam){
        exams.add(exam);
    }
    
    public void addSingleExamId(int examId){
        exams.add(new Exam(examId,0));
    }

    void sortExams() {
        Collections.sort(exams);
           
        
        
    }
    
}
