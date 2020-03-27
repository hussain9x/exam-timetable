
package examtimetable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Hussain
 */
public class Dataset {

   private int zeroExams = 0;
   private File xmlFile;
   private String xmlFileName;
   private final int TWO_DAYS = 2;
   private final int ONE_DAY = 1;
   private final int ZERO_DAY = 0;
   private ArrayList<Booking> bookings;
   private ArrayList<Exam> sortedExams;
   private int[][] conflictMatrix;
   private Map<Integer, Period> periods;
   private Map<Integer, Room> rooms;
   private Map<Integer, Student> students;
   private ArrayList<Integer> examIDs;

    public Dataset(String xmlFileName) {
        this.xmlFileName = xmlFileName;
        xmlFile = new File(this.xmlFileName);
        if (xmlFile.exists()) {
            try {

                bookings = new ArrayList<>();

                readXMLValues();

                generateMatrices();

                bookExams(TWO_DAYS);

                bookExams(ONE_DAY);

                bookExams(ZERO_DAY);

                //adding new periods to unbooked exams
                while (bookings.size() + zeroExams < examIDs.size()) {
                    addNewPeriod();
                    bookExams(ZERO_DAY);
                }

                System.out.println("Number of Exams with zero size (ignored): " + zeroExams);
                System.out.println("Bookings made: " + bookings.size());

                writeBookingsToTextFile();
            } catch (IOException | ParseException | ParserConfigurationException | SAXException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Error: dataset.xml file doesn't exist. Please make sure it is stored under " + getCurrentPath());
        }
    }

    private String getCurrentPath() {
        return System.getProperty("user.dir");

    }

    private void writeBookingsToTextFile() throws FileNotFoundException {
        Collections.sort(bookings);
        PrintWriter out = new PrintWriter(getCurrentPath() + "\\timetable.txt");
        out.println("Number of exams booked: " + bookings.size());
        out.println("Number of zero-size exams (ignored): " + zeroExams);
        for (int i = 0; i < bookings.size(); i++) {
            out.println(bookings.get(i));
        }
        out.close();
        System.out.println("The new timetable has been generated and saved in the same folder as \"timetable.txt\"");

    }

    private void generateMatrices() {
        try {

            
            Map<Integer, Exam> examSizes = new TreeMap<>();
            int[][] m = new int[examIDs.size()][examIDs.size()];
            for (int i = 0; i < examIDs.size(); i++) {

                examSizes.put(examIDs.get(i), new Exam(examIDs.get(i), 0));
            }

            //initializing the matrix values to 0
            for (int i = 0; i < examIDs.size(); i++) {
                for (int j = 0; j < examIDs.size(); j++) {
                    m[i][j] = 0;
                }
            }

            //    building the matrix
            for (Student student : students.values()) {
                ArrayList<Exam> studentExams = student.getExams();
                for (int i = 0; i < studentExams.size(); i++) {

                    examSizes.get(studentExams.get(i).getId()).increaseSize();

                    for (int j = 0; j < studentExams.size(); j++) {
                        if (i < j) {
                            m[studentExams.get(i).getId() - 1][studentExams.get(j).getId() - 1]++;
                        }
                    }
                }
            }
            this.conflictMatrix = m;
            
            
            this.sortedExams = new ArrayList<>(examSizes.values());

            Collections.sort(this.sortedExams);
            

        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

    }

    private void readXMLValues() throws SAXException, IOException, ParserConfigurationException, ParseException {
        System.out.println("reading periods");
        periods = readPeriods();
        System.out.println("reading rooms");
        rooms = readRooms();
        System.out.println("reading students and their exams");
        students = getAllStudentsData();
        System.out.println("reading exam IDs");
        examIDs = readExamIDs();

        System.out.println("number of students: " + students.size());
        System.out.println("number of rooms: " + rooms.size());
        System.out.println("number of periods: " + periods.size());
        System.out.println("number of exams: " + examIDs.size());
    }

    private Map<Integer, Student> getAllStudentsData() throws SAXException, ParserConfigurationException, IOException {
        return readStudentExams(readStudents());
    }

    private NodeList getNodesByName(String name) throws SAXException, IOException {
        Document doc = null;
        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

        } catch ( IOException | ParserConfigurationException | SAXException e) {
            System.out.println(e.getMessage());
        }
        
        return doc.getElementsByTagName(name);

    }

    private Map<Integer, Student> readStudentExams(Map<Integer, Student> students) throws SAXException, IOException, ParserConfigurationException {
        Map<Integer, Student> allStudents = students;

        NodeList studentNodes = getNodesByName("student");
        for (int s = 0; s < studentNodes.getLength(); s++) {

            Node student = studentNodes.item(s);
            Element studentElement = (Element) student;
            int studentId = Integer.parseInt(studentElement.getAttribute("id"));
            NodeList examNodes = student.getChildNodes();
            for (int temp = 0; temp < examNodes.getLength(); temp++) {

                Node examNode = examNodes.item(temp);

                if (examNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element examElement = (Element) examNode;
                    allStudents.get(studentId).addSingleExamId(Integer.parseInt(examElement.getAttribute("id")));
                }
            }
            allStudents.get(studentId).sortExams();
        }
        return allStudents;
    }

    private int extractIntFromString(String text) {
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }

    private Map<Integer, Student> readStudents() throws SAXException, IOException {

        Map<Integer, Student> students = new HashMap<>();
        NodeList nList = getNodesByName("student");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element studentElement = (Element) node;
                int studentId = extractIntFromString(studentElement.getAttribute("id"));
                Student std = new Student(studentId);
                students.put(studentId, std);
            }
        }
        return students;
    }

    private Map<Integer, Room> readRooms() throws SAXException, IOException, ParserConfigurationException {
        Map<Integer, Room> rooms = new HashMap<>();

        NodeList roomsnode = getNodesByName("rooms");

        NodeList roomNodes = roomsnode.item(0).getChildNodes();

        for (int i = 0; i < roomNodes.getLength(); i++) {

            Node roomNode = roomNodes.item(i);

            if (roomNode.getNodeType() == Node.ELEMENT_NODE) {
                Element roomElement = (Element) roomNode;
                Node parent = roomElement.getParentNode();
                if ("rooms".equals(parent.getNodeName())) {
                    Integer roomId = Integer.parseInt(roomElement.getAttribute("id"));
                    rooms.put(roomId, new Room(roomId, Integer.parseInt(roomElement.getAttribute("size"))));
                }
            }
        }
        return rooms;
    }

    private Map<Integer, Period> readPeriods() throws SAXException, IOException, ParserConfigurationException, ParseException {
        Map<Integer, Period> periods = new HashMap<>();
        NodeList periodsNode = getNodesByName("periods");
        NodeList periodNodes = periodsNode.item(0).getChildNodes();
        for (int i = 0; i < periodNodes.getLength(); i++) {
            Node periodNode = periodNodes.item(i);
            if (periodNode.getNodeType() == Node.ELEMENT_NODE) {
                Element periodElement = (Element) periodNode;
                Integer periodId = Integer.parseInt(periodElement.getAttribute("id"));
                LocalDate date = parseLocalDate(extractPeriodTime(periodElement.getAttribute("day")));
                periods.put(periodId, new Period(periodId, date));
            }
        }
        return periods;
    }

    private LocalDate parseLocalDate(String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return LocalDate.parse(s, formatter);
    }

    public String extractPeriodTime(String time) {
        String temp = "";
        for (int i = 0; i < time.length(); i++) {
            temp += time.charAt(i);
            if (time.charAt(i) == 'a' || time.charAt(i) == 'p') {
                temp += "m";
                break;
            }
        }
        return temp;
    }

    private ArrayList<Integer> readExamIDs() throws SAXException, IOException, ParserConfigurationException {
        ArrayList<Integer> examIDs = new ArrayList<>();
        NodeList examsNode = getNodesByName("exams");
        NodeList examNodes = examsNode.item(0).getChildNodes();
        for (int i = 0; i < examNodes.getLength(); i++) {
            Node node = examNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element examElement = (Element) node;
                examIDs.add(Integer.parseInt(examElement.getAttribute("id")));
            }
        }
        return examIDs;
    }

    private void bookExams(int days) {
        /*
        for each exam
                if exam is not booked
                    if exam size > 0
                        for each period
                        if no current period conflict and no date conflict
                            if there are suitable rooms available
                            make the booking (period,exam,get suitable rooms)
         */
        zeroExams = 0;
        for (int iX = 0; iX < examIDs.size(); iX++) {
            Exam exam = sortedExams.get(iX);
            if (!isExamBooked(exam)) {
                if (exam.getSize() > 0) {
                    for (Integer key : periods.keySet()) {
                        Period period = periods.get(key);

                        if (!isDateConflict(period, exam, days)) {
                            if (!isPeriodConflict(period, exam)) {
                                if (areSuitableRoomsAvailable(exam, period)) {
                                    Booking booking = new Booking(exam, period, getSuitableRooms(exam, period));
                                    bookings.add(booking);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    zeroExams++;
                }
            }
        }
    }

    
    private Map<Integer, Room> getAvailableRoomsforPeriod(Period p) {
        Map<Integer, Room> result = new TreeMap<>();
        for (Integer key : rooms.keySet()) {
            result.put(key, rooms.get(key));
        }
        for (int i = 0; i < bookings.size(); i++) {

            if (bookings.get(i).getPeriod().getId() == p.getId()) {
                Map<Integer, Room> bookedRooms = bookings.get(i).getRooms();
                for (Integer key : bookedRooms.keySet()) {
                    result.remove(bookedRooms.get(key).getId());
                }
            }
        }
        return result;
    }

    private boolean isEnrolmentConflict(Exam x1, Exam x2) {
        Boolean result = false;
        if (x1.getId() != x2.getId()) {
            if (x1.getId() < x2.getId()) {
                if (conflictMatrix[x1.getId() - 1][x2.getId() - 1] > 0) {
                    result = true;
                }
            } else {
                if (conflictMatrix[x2.getId() - 1][x1.getId() - 1] > 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    private Map<Integer, Room> getSuitableRooms(Exam e, Period p) {
        Map<Integer, Room> availableRooms = new TreeMap(getAvailableRoomsforPeriod(p));
        Map<Integer, Room> selectedRooms = new TreeMap<>();
        if (availableRooms.size() > 0) {
            Exam exam = (Exam) e;
            int size = exam.getSize();
            for (Integer key : availableRooms.keySet()) {
                if (availableRooms.get(key).getSize() == size) {
                    selectedRooms.put(key, availableRooms.get(key));
                    return selectedRooms;

                }
            }

            Map<Integer, Integer> distances = new TreeMap<>();
            for (Integer key : availableRooms.keySet()) {
                distances.put(availableRooms.get(key).getId(), availableRooms.get(key).getSize() - size);
            }
            int nearestRoomSizeID = getNearestRoomSize(distances);
            if (nearestRoomSizeID > 0) {
                selectedRooms.put(nearestRoomSizeID, availableRooms.get(nearestRoomSizeID));
                return selectedRooms;
            }
            int achievedCapacity = 0;
            Map<Integer, Room> multipleRooms = new TreeMap<>();
            for (Integer key : availableRooms.keySet()) {
                if (achievedCapacity < size) {
                    multipleRooms.put(key, availableRooms.get(key));
                    achievedCapacity += availableRooms.get(key).getSize();
                } else {
                    return multipleRooms;

                }
            }
        } else {
            return null;
        }
        return selectedRooms;
    }

    private int getNearestRoomSize(Map<Integer, Integer> distances) {
        int min = getMaxValue(distances.values().toArray());
        int roomId = 0;
        for (Integer key : distances.keySet()) {
            if (distances.get(key) > 0 && distances.get(key) < min) {
                min = distances.get(key);
                roomId = key;
            }
        }
        return roomId;
    }

    private int getMaxValue(Object[] values) {
        int max = (int) values[0];
        for (int i = 1; i < values.length; i++) {
            if ((int) values[i] > max) {
                max = (int) values[i];
            }
        }
        return max;
    }

    private boolean isExamBooked(Exam x) {
        boolean result = false;
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).exam.getId() == x.getId()) {
                return true;
            }
        }
        return result;
    }

    private boolean areSuitableRoomsAvailable(Exam x, Period p) {
        Boolean result = false;
        if (getSuitableRooms(x, p) != null) {
            Map<Integer, Room> suitableRooms = new TreeMap<>(getSuitableRooms(x, p));
            if (suitableRooms.size() > 0) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

 
    private boolean isPeriodConflict(Period period, Exam exam) {
        Boolean result = false;
        if (bookings.size() > 0) {
            for (int i = 0; i < bookings.size(); i++) {
                if (bookings.get(i).getPeriod().equals(period)) {
                    if (isEnrolmentConflict(exam, bookings.get(i).getExam())) {
                        result = true;
                    } else {
                    }
                }
            }
        }
        return result;
    }

    private boolean isDateConflict(Period period, Exam exam, int days) {
        boolean result = false;
        if (bookings.size() > 0) {
            for (int i = 0; i < bookings.size(); i++) {
                if (!isAllowedNumberOfDays(getDifferenceInDays(bookings.get(i).getPeriod().getDate(), period.getDate()), days)
                        && isEnrolmentConflict(exam, bookings.get(i).getExam())) {
                    result = true;
                }
            }
        }
        return result;
    }

    private boolean isAllowedNumberOfDays(long days, int allowedDays) {
        boolean result;
        if (days < allowedDays && days > allowedDays * -1) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private long getDifferenceInDays(LocalDate date1, LocalDate date2) {
        return ChronoUnit.DAYS.between(date1, date2);
    }

    private void addNewPeriod() {
        int dailyPeriodsCount = Math.round(periods.size() / getNumberOfDays());
        ArrayList<LocalDate> dates = getDates();
        boolean addNewDate = true;
        for (LocalDate date : dates) {
            int periodsCount = getNumberOfPeriodsForDate(date);
            if (periodsCount < dailyPeriodsCount) {
                int newPeriodID = generateNewPeriodID();
                periods.put(newPeriodID, new Period(newPeriodID, date));
                addNewDate = false;
                break;
            }
        }
        if (addNewDate) {
            int newPeriodID = generateNewPeriodID();
            periods.put(newPeriodID, new Period(newPeriodID, dates.get(dates.size() - 1).plusDays(1)));
        }
    }

    private int getNumberOfDays() {
        return getDates().size();
    }

    private ArrayList<LocalDate> getDates() {
        ArrayList<LocalDate> dates = new ArrayList<>();
        for (Integer key : periods.keySet()) {
            if (!dates.contains(periods.get(key).getDate())) {
                dates.add(periods.get(key).getDate());
            }
        }
        return dates;
    }

    private int getNumberOfPeriodsForDate(LocalDate date) {
        int count = 0;
        for (Integer key : periods.keySet()) {
            if (periods.get(key).getDate().equals(date)) {
                count++;
            }
        }
        return count;
    }

    private ArrayList<Integer> getAllPeriodIDs() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Integer key : periods.keySet()) {
            ids.add(periods.get(key).getId());
        }
        Collections.sort(ids);
        return ids;
    }

    private int generateNewPeriodID() {
        ArrayList<Integer> ids = getAllPeriodIDs();
        return ids.get(ids.size() - 1) + 1;
    }

}
