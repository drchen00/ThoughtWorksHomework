import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeSet;

public class Court {
    private TreeSet<Schedule> schedules = new TreeSet<>();
    private TreeSet<Event> bill = new TreeSet<>();
    private String name;
    private ScheduleFactory scheduleFactory;

    public Court(String name) {
        this.name = name;
        scheduleFactory = new ScheduleFactory();
    }

    public String getName() {
        return name;
    }

    public void clearSchedules(){}

    public void clearBill(){}

    public void booking(String userId,LocalDate date,LocalTime startTime,LocalTime endTime) {
        try {
            if (startTime.getMinute() != 0 || endTime.getMinute() != 0 || startTime.compareTo(endTime) != -1)
                throw new ErrorTimeException();
            Schedule schedule = scheduleFactory.creatSchedule(date);
            if (schedules.contains(schedule))
                schedule = schedules.floor(schedule);
            else
                schedules.add(schedule);
            double earnest = schedule.schedule(startTime.getHour(), endTime.getHour());
            if (earnest < 0)
                throw new BookingConfilctsException();
            bill.add(new Event(userId,date,startTime,endTime,earnest));
            System.out.println("> Success: the booking is accepted");
        } catch (ErrorTimeException | BookingConfilctsException e) {
            System.out.println(e.getMessage());
        }
    }

    public void cancel(String userId,LocalDate date,LocalTime startTime,LocalTime endTime){
        try{
            if (startTime.getMinute() != 0 || endTime.getMinute() != 0 || startTime.compareTo(endTime) != -1)
                throw new ErrorTimeException();
            Event event = new Event(userId,date,startTime,endTime);
            if(!bill.contains(event))
                throw new BookingNotExistException();
        } catch (ErrorTimeException | BookingNotExistException e) {
            System.out.println(e.getMessage());
        }
    }

    private class Event implements Comparable<Event> {
        private String userId;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private double income;
        private double punishment;
        private boolean isPunished;
        private int punishedTimes;

        private Event(String userId, LocalDate date, LocalTime startTime, LocalTime endTime, double income) {
            this.userId = userId;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.income = income;
            switch (date.getDayOfWeek()) {
                case MONDAY:
                case TUESDAY:
                case WEDNESDAY:
                case THURSDAY:
                case FRIDAY:
                    punishment = 0.5;
                    break;
                case SATURDAY:
                case SUNDAY:
                    punishment = 0.25;
                    break;
            }
        }

        private Event(String userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
            this(userId,date,startTime,endTime,0);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Event
                    && userId.equals(((Event) obj).userId)
                    && date.equals(((Event) obj).date)
                    && startTime.equals(((Event) obj).startTime)
                    && endTime.equals(((Event) obj).endTime)
                    && isPunished==((Event)obj).isPunished;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31*result+userId.hashCode();
            result = 31*result+date.hashCode();
            result = 31*result+startTime.hashCode();
            result = 31*result+endTime.hashCode();
            return result;
        }

        @Override
        public int compareTo(Event o) {
            int tmp = date.compareTo(o.date);
            if(tmp==0){
                tmp = startTime.compareTo(o.startTime);
            }
            return tmp;
        }
    }

    private class Schedule implements Comparable<Schedule> {
        private double[] price;

        private LocalDate date;

        private boolean[] acceptTime;

        private int baseTime;

        private Schedule(double[] price, int start, int end, LocalDate date) {
            this.price = price.clone();
            baseTime = start;
            acceptTime = new boolean[end - start];
            this.date = date;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Schedule && date.equals(((Schedule) obj).date);
        }

        @Override
        public int hashCode() {
            return date.hashCode();
        }

        @Override
        public int compareTo(Schedule o) {
            return date.compareTo(o.date);
        }

        private double schedule(int startTime, int endTime) {
            double money = 0;
            for (int i = startTime - baseTime; i < endTime - baseTime; ++i) {
                if (acceptTime[i])
                    return -1;
                else {
                    acceptTime[i] = true;
                    money += price[i];
                }
            }
            return money;
        }
    }

    private class ScheduleFactory {

        private Schedule creatSchedule(LocalDate date) {
            DayOfWeek week = date.getDayOfWeek();
            switch (week) {
                case MONDAY:
                case TUESDAY:
                case WEDNESDAY:
                case THURSDAY:
                case FRIDAY:
                    return new Schedule(new double[]{30, 30, 30, 50, 50, 50, 50, 50, 50, 80, 80, 60, 60}, 9, 22, date);
                case SATURDAY:
                case SUNDAY:
                    return new Schedule(new double[]{40, 40, 40, 50, 50, 50, 50, 50, 50, 60, 60, 60, 60}, 9, 22, date);
            }
            return null;
        }
    }
}
