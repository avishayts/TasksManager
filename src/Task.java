import java.util.Date;

public class Task {
    private String name;
    private Date date;
    private Status status;
    private Priority prio;
    private String note;

    public Task(String name, Date date, Status status, Priority prio, String note) {
        this.name = name;
        this.date = date;
        this.status = status;
        this.prio = prio;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDateTime(Date date) {
        this.date = date;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public Priority getPrio() {
        return prio;
    }

    public void setPrio(Priority prio) {
        this.prio = prio;
    }

    public String getNote() { return note; }

    public void setNote(String note) { this.note = note; }

    public String[] toArray() {
        return new String[]{name, dateToString(), String.valueOf(status), String.valueOf(prio), note};
    }
    public String dateToString() {
        int month = date.getMonth() + 1;
        String minutes;
        if (date.getMinutes() < 10)
            minutes = "0" + date.getMinutes();
        else minutes = date.getMinutes()+"";
        return date.getDate() + "." +
                month + "." +
                date.getYear() + ", " +
                date.getHours() + ":" +
                minutes;
    }

    @Override
    public String toString() {
        return "Task: '" + name + '\'' +
                "\nDate: " + dateToString() +
                "\nStatus: " + status +
                "\nPriority: " + prio +
                "\nNote: " + note;
    }
}
