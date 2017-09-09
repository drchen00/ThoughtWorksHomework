import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Court {
    private static Set<Court> courts = new HashSet<>();
    private TreeSet<Schedule> schedules = new TreeSet<>();
    private TreeSet<Event> bill = new TreeSet<>();
    private String name;
    private ScheduleFactory scheduleFactory;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Court && name.equals(((Court) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private Court(String name) {
        this.name = name;
        scheduleFactory = new ScheduleFactory();
    }

    public String getName() {
        return name;
    }

    public static boolean createCourt(String name) {
        return courts.add(new Court(name));
    }

    private static void printAllBill() {
        System.out.println("收入汇总");
        System.out.println("---");
        Iterator<Court> iterator = courts.iterator();
        double sum=0;
        while (iterator.hasNext()) {
            sum+=iterator.next().printBill();
            if (iterator.hasNext())
                System.out.println();
        }
        System.out.println("---");
        System.out.println("总计: "+(int)sum+"元");
    }

    public static void runCommand(String s) {
        if (s.equals("")) {
            printAllBill();
            return;
        }
        String[] para = s.split(" ");
        try {
            if (para.length != 4 && para.length != 5)
                throw new ErrorInputException();
            String userId = para[0];
            LocalDate date = LocalDate.parse(para[1]);
            String[] time = para[2].split("~");
            LocalTime startTime = LocalTime.parse(time[0]);
            LocalTime endTime = LocalTime.parse(time[1]);
            Court court = getCourtByName(para[3]);
            if (court == null)
                throw new ErrorInputException();
            boolean result;
            if (para.length == 5) {
                if (para[4].equals("C"))
                    result = court.cancel(userId, date, startTime, endTime);
                else
                    throw new ErrorInputException();
            } else {
                result = court.booking(userId, date, startTime, endTime);
            }
            if (result)
                System.out.println("> Success: the booking is accepted!");
        } catch (ErrorInputException | ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Court getCourtByName(String name) {
        for (Court court : courts) {
            if (court.name.equals(name))
                return court;
        }
        return null;
    }

    public double printBill() {
        System.out.println("场地:" + name);
        double sum = 0;
        for (Event event : bill) {
            if(event.isPunished){
                sum+=event.income*event.punishment*event.punishedTimes;
            }else {
                sum+=event.income;
            }
            System.out.println(event.toString());
        }
        System.out.println("小计: "+(int)sum+"元");
        return sum;
    }

    public void clearSchedules() {
    }

    public void clearBill() {
    }

    public boolean booking(String userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            if (startTime.getMinute() != 0 || endTime.getMinute() != 0 || startTime.compareTo(endTime) != -1)
                throw new ErrorInputException();
            Schedule schedule = scheduleFactory.creatSchedule(date);
            if (schedules.contains(schedule))
                schedule = schedules.floor(schedule);
            else
                schedules.add(schedule);
            double earnest = schedule.schedule(startTime.getHour(), endTime.getHour());
            if (earnest < 0)
                throw new BookingConfilctsException();
            bill.add(new Event(userId, date, startTime, endTime, earnest));
            return true;
        } catch (ErrorInputException | BookingConfilctsException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean cancel(String userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        try {
            if (startTime.getMinute() != 0 || endTime.getMinute() != 0 || startTime.compareTo(endTime) != -1)
                throw new ErrorInputException();
            Event event = new Event(userId, date, startTime, endTime);
            if (!bill.contains(event))
                throw new BookingNotExistException();
            Schedule schedule = schedules.floor(scheduleFactory.creatSchedule(date));
            schedule.rollBack(startTime.getHour(), endTime.getHour());
            bill.remove(event);
            event.isPunished = true;
            event.punishedTimes = 1;
            if (!bill.add(event))
                bill.floor(event).punishedTimes++;
            return true;
        } catch (ErrorInputException | BookingNotExistException e) {
            System.out.println(e.getMessage());
            return false;
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
            this(userId, date, startTime, endTime, 0);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (isPunished) {
                for (int i = 0; i < punishedTimes; ++i) {
                    sb.append(date).append(" ")
                            .append(startTime).append("~").append(endTime).append(" ")
                            .append("违约金").append(" ")
                            .append((int) (income * punishment)).append("元");
                }
            } else
                sb.append(date).append(" ")
                        .append(startTime).append("~").append(endTime).append(" ")
                        .append((int)income).append("元");
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Event
                    && userId.equals(((Event) obj).userId)
                    && date.equals(((Event) obj).date)
                    && startTime.equals(((Event) obj).startTime)
                    && endTime.equals(((Event) obj).endTime)
                    && isPunished == ((Event) obj).isPunished;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + userId.hashCode();
            result = 31 * result + date.hashCode();
            result = 31 * result + startTime.hashCode();
            result = 31 * result + endTime.hashCode();
            return result;
        }

        @Override
        public int compareTo(Event o) {
            int tmp = date.compareTo(o.date);
            if (tmp == 0) {
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
            for (int i = startTime; i < endTime; ++i) {
                int ti = i - baseTime;
                if (acceptTime[ti]) {
                    rollBack(startTime, i);
                    return -1;
                } else {
                    acceptTime[ti] = true;
                    money += price[ti];
                }
            }
            return money;
        }

        private void rollBack(int startTime, int endTime) {
            for (int i = startTime; i < endTime; ++i) {
                int ti = i - baseTime;
                acceptTime[ti] = false;
            }
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
