import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class TasksManager {
    private String[] column = {"Task","Date","Status","Priority","Note"};
    private DefaultTableModel tableModel = new DefaultTableModel(column,0);
    private JTable jt = new JTable(tableModel);

    private static class SingletonHolder {
        private static TasksManager instance;
        static {
            try {
                instance = new TasksManager();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static TasksManager getInstance(){
        return SingletonHolder.instance;
    }

    private TasksManager() throws IOException {
        // Main Frame
        JFrame mainFrame = new JFrame("Tasks Manager");
        mainFrame.setVisible(true);
        mainFrame.setBounds(200,100,700,400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Main Panel
        JPanel mainPanel = new JPanel();
        mainFrame.add(mainPanel);
        mainPanel.setLayout(null);
        // Tasks Table
        File f = createTableFromFile();
        JScrollPane sp=new JScrollPane(jt);
        mainPanel.add(sp);
        sp.setBounds(2,0,683,320);
        // Main Buttons
        JButton addButton = addButton(mainPanel,"Add",5,325,60,30);
        addButton.addActionListener(new addTask());
        JButton editButton = addButton(mainPanel,"Edit",70,325,60,30);
        editButton.addActionListener(new editTask());
        JButton removeButton = addButton(mainPanel,"Remove",135,325,90,30);
        removeButton.addActionListener(new removeTask());
        JButton doneButton = addButton(mainPanel,"Done",230,325,65,30);
        doneButton.addActionListener(new doneTask());
        JButton saveButton = addButton(mainPanel,"Save",480,325,65,30);
        saveButton.addActionListener(new saveTask(mainFrame, f, false));
        JButton saveAndExitButton = addButton(mainPanel,"Save and Exit",550,325,130,30);
        saveAndExitButton.addActionListener(new saveTask(mainFrame, f, true));
    }

    private File createTableFromFile() throws IOException {
        File f = new File("tasks.txt");
        if(!f.exists())
            f.createNewFile();
        else {
            createTasksFromFile(f);
        }
        for (int i = 0; i < 5; i++)
            jt.getColumnModel().getColumn(i).setCellRenderer(new StatusColumnCellRenderer());
        JTableHeader tableHeader = jt.getTableHeader();
        tableHeader.setBackground(new Color(134, 84, 84));
        tableHeader.setForeground(new Color(255,255,255));
        tableHeader.setFont(new Font("Segoe UI",Font.BOLD,18));
        jt.setRowHeight(25);
        jt.setFont(new Font("Segoe UI", Font.BOLD,13));
        jt.setSelectionBackground(new Color(32,136,203));
        return f;
    }

    private void createTasksFromFile(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = reader.readLine();
        Task t;
        String[] data;
        String[] dayArray;
        String[] hourArray;
        Date d;
        while (line != null) {
            data = line.split("\\|");
            dayArray = data[1].split(", ")[0].split("\\.");
            hourArray = data[1].split(", ")[1].split(":");
            d = new Date(Integer.parseInt(dayArray[2]), Integer.parseInt(dayArray[1])-1,Integer.parseInt(dayArray[0]),Integer.parseInt(hourArray[0]),Integer.parseInt(hourArray[1]),0);
            t = new Task(data[0],d,getStatus(data[2]),getPriority(data[3]),data[4]);
            tableModel.addRow(t.toArray());
            line = reader.readLine();
        }
        reader.close();
    }

    public static Status getStatus(String st) {
        if ("Done".equals(st)) {
            return Status.Done;
        }
        return Status.Pending;
    }

    public static Priority getPriority(String st) {
        if ("High".equals(st))
            return Priority.High;
        else if ("Medium".equals(st))
            return Priority.Medium;
        return Priority.Low;
    }

    public static Priority getPriorityFromGroup(ButtonGroup buttonGroup) {
        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                if ("High".equals(button.getText()))
                    return Priority.High;
                else if ("Medium".equals(button.getText()))
                    return Priority.Medium;
                else return Priority.Low;
            }
        }
        return Priority.Low;
    }

    public static class addTask implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            AddAndEdit("Add Task", false);
        }
    }

    static class editTask implements ActionListener {
        public void actionPerformed (ActionEvent e) {
            AddAndEdit("Edit Task", true);
        }
    }

    static private void AddAndEdit(String lab, boolean edit) {
        JTable table = TasksManager.getInstance().jt;
        int row = table.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (edit && row == -1)
            return;

        JFrame frame = new JFrame("Tasks Manager");
        JPanel panel = AddAndEditPanel(frame, lab);
        UtilDateModel dateModel = new UtilDateModel();
        JDatePickerImpl datePicker = makeDatePicker(panel, dateModel);
        SpinnerDateModel timeModel = makeTimeModel(panel);
        ButtonGroup buttonGroup = makePrioButtons(panel);
        JTextField taskField = addText(panel,60,25,200,20);
        JTextField noteFeild = addText(panel,60,100,200,20);
        makeLabels(panel);
        Status status = Status.Pending;
        if (edit) {
            taskField.setText((String) table.getValueAt(row, 0));
            dateModel.setSelected(true);
            dateModel.setDate(getParams((String) table.getValueAt(row, 1), "Year"),getParams((String) table.getValueAt(row, 1), "Month"),getParams((String) table.getValueAt(row, 1), "Day"));
            timeModel.setValue(new Date(getParams((String) table.getValueAt(row, 1), "Year"),getParams((String) table.getValueAt(row, 1), "Month"),getParams((String) table.getValueAt(row, 1), "Day"),getParams((String) table.getValueAt(row, 1),"Hour") ,getParams((String) table.getValueAt(row, 1),"Minute"),0));
            setPrio(buttonGroup,(String) table.getValueAt(row, 3));
            noteFeild.setText((String) table.getValueAt(row, 4));
            status = getStatus((String)table.getValueAt(row,2));
        }
        else row = model.getRowCount();

        JButton subSaveButton = addButton(panel,"Save",5,130,65,30);
        subSaveButton.addActionListener(new subSaveTask(frame, taskField, datePicker,timeModel, buttonGroup, status, noteFeild, row));
        JButton cancelButton = addButton(panel,"Cancel",75,130,80,30);
        cancelButton.addActionListener(new cancelTask(frame));
    }

    private static void makeLabels(JPanel panel) {
        JLabel taskLabel = addLabel(panel,"Task:",5,25,50,20);
        JLabel dateLabel = addLabel(panel,"Date:",5,50,50,20);
        JLabel priorityLabel = addLabel(panel,"Priority:",5,75,50,20);
        JLabel noteLabel = addLabel(panel,"Note:",5,100,50,20);
    }

    private static void setPrio(ButtonGroup buttonGroup, String prio) {
        Enumeration<AbstractButton> buttons = buttonGroup.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton button = buttons.nextElement();
            if (button.getText().equals(prio))
                button.setSelected(true);
        }
    }

    private static ButtonGroup makePrioButtons(JPanel panel) {
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton lowButton = addPrioButton(panel, "Low",60,75,50,20);
        JRadioButton mediumButton = addPrioButton(panel, "Medium",120,75,75,20);
        JRadioButton highButton = addPrioButton(panel, "High",200,75,60,20);
        buttonGroup.add(lowButton);
        buttonGroup.add(mediumButton);
        buttonGroup.add(highButton);
        return buttonGroup;
    }

    private static JRadioButton addPrioButton(JPanel panel, String prio, int b1, int b2, int b3, int b4) {
        JRadioButton button = new JRadioButton(prio);
        panel.add(button);
        button.setVisible(true);
        button.setBounds(b1,b2,b3,b4);
        return button;
    }

    private static SpinnerDateModel makeTimeModel(JPanel panel) {
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeModel.setCalendarField(Calendar.MINUTE);
        JSpinner spinner= new JSpinner();
        spinner.setModel(timeModel);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        panel.add(spinner);
        spinner.setVisible(true);
        spinner.setBounds(180,47,60,26);
        return timeModel;
    }

    private static JDatePickerImpl makeDatePicker(JPanel panel, UtilDateModel dateModel) {
        Properties p = new Properties();
        p.put("text.day", "Day");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        p.put("text.today", "Today");
        JDatePanelImpl datePanel = new JDatePanelImpl(dateModel, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        panel.add(datePicker);
        datePicker.setVisible(true);
        datePicker.setBounds(60,47,110,26);
        return datePicker;
    }

    private static JPanel AddAndEditPanel(JFrame frame, String lab) {
        frame.setVisible(true);
        frame.setBounds(300,200,300,200);
        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(null);
        JLabel label = new JLabel(lab);
        panel.add(label);
        label.setVisible(true);
        label.setBounds(110,0,90,30);
        return panel;
    }

    private static int getParams(String date, String param) {
        String[] dayArray = date.split(", ")[0].split("\\.");
        String[] hourArray = date.split(", ")[1].split(":");
        if (param.equals("Year"))
            return Integer.parseInt(dayArray[2]);
        else if (param.equals("Month"))
            return Integer.parseInt(dayArray[1])-1;
        else if (param.equals("Day"))
            return Integer.parseInt(dayArray[0]);
        else if (param.equals("Hour"))
            return Integer.parseInt(hourArray[0]);
        else return Integer.parseInt(hourArray[1]);
    }

    private static Task getTaskFromRow(DefaultTableModel model, int row) {
        String[] dayArray = (model.getValueAt(row,1)).toString().split(", ")[0].split("\\.");
        String[] hourArray = (model.getValueAt(row,1).toString()).split(", ")[1].split(":");
        Date d = new Date(Integer.parseInt(dayArray[2]), Integer.parseInt(dayArray[1])-1,Integer.parseInt(dayArray[0]),Integer.parseInt(hourArray[0]),Integer.parseInt(hourArray[1]),0);
        return new Task(model.getValueAt(row,0).toString(),d,getStatus(model.getValueAt(row,2).toString()),getPriority(model.getValueAt(row,3).toString()),model.getValueAt(row,4).toString());
    }

    static class removeTask implements ActionListener {
        public void actionPerformed (ActionEvent e) {
            JTable table = TasksManager.getInstance().jt;
            int[] rows = table.getSelectedRows();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            for(int i=0;i<rows.length;i++)
                model.removeRow(rows[i]-i);
        }
    }

    static class doneTask implements ActionListener {
        public void actionPerformed (ActionEvent e) {
            JTable table = TasksManager.getInstance().jt;
            int[] rows = table.getSelectedRows();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            Task newTask;
            for (int i=0;i<rows.length;i++) {
                newTask = getTaskFromRow(model, rows[i]);
                if (newTask.getStatus() == Status.Done)
                    newTask.setStatus(Status.Pending);
                else newTask.setStatus(Status.Done);
                model.removeRow(rows[i]);
                model.insertRow(rows[i], newTask.toArray());
            }
        }
    }

    static class saveTask implements ActionListener {
        JFrame frame;
        File file;
        private boolean exit;

        public saveTask(JFrame frame,File file, boolean exit) {
            this.frame = frame;
            this.file = file;
            this.exit = exit;
        }

        public void actionPerformed (ActionEvent e) {
            DefaultTableModel model = TasksManager.getInstance().tableModel;
            BufferedWriter writer;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                int rowCount = model.getRowCount();
                int columnCount = model.getColumnCount();
                StringBuilder toWrite = new StringBuilder();
                for (int i = 0; i < rowCount; i++) {
                    for(int j = 0; j< columnCount; j++) {
                        toWrite.append(model.getValueAt(i, j));
                        toWrite.append("|");
                    }
                    writer.write(toWrite.toString());
                    writer.newLine();
                    toWrite = new StringBuilder();
                }
                writer.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            if (exit)
                frame.dispose();
        }
    }

    static class subSaveTask implements ActionListener {
        private JFrame frame;
        private JTextField taskField;
        private JDatePickerImpl datePicker;
        private SpinnerDateModel timeModel;
        private ButtonGroup buttonGroup;
        private Status status;
        private JTextField noteField;
        private int row;

        public subSaveTask(JFrame frame, JTextField taskField, JDatePickerImpl datePicker, SpinnerDateModel timeModel, ButtonGroup buttonGroup, Status status, JTextField noteFeild, int row) {
            this.frame = frame;
            this.taskField = taskField;
            this.datePicker = datePicker;
            this.timeModel = timeModel;
            this.buttonGroup = buttonGroup;
            this.status = status;
            this.noteField = noteFeild;
            this.row = row;
        }

        public void actionPerformed (ActionEvent e) {
            JTable table = TasksManager.getInstance().jt;
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            String[] dayArray = datePicker.getJFormattedTextField().getText().split("\\.");
            String[] hourArray = timeModel.getValue().toString().split(" ")[3].split(":");
            Date d = new Date(Integer.parseInt(dayArray[2]), Integer.parseInt(dayArray[1])-1,Integer.parseInt(dayArray[0]),Integer.parseInt(hourArray[0]),Integer.parseInt(hourArray[1]),0);
            Task t = new Task(taskField.getText(),d,status,getPriorityFromGroup(buttonGroup),noteField.getText());
            if (row != model.getRowCount()) {
                model.removeRow(row);
            }
            model.insertRow(row, t.toArray());
            frame.dispose();
        }
    }

    static class cancelTask implements ActionListener {
        private JFrame frame;

        public cancelTask(JFrame frame) {
            this.frame = frame;
        }

        public void actionPerformed (ActionEvent e) {
            frame.dispose();
        }
    }

    static private JButton addButton(JPanel panel, String buttonLabel, int bound1, int bound2, int bound3, int bound4) {
        JButton newButton = new JButton(buttonLabel);
        panel.add(newButton);
        newButton.setBounds(bound1,bound2,bound3,bound4);
        newButton.setVisible(true);
        return newButton;
    }

    static private JLabel addLabel(JPanel panel, String label, int bound1, int bound2, int bound3, int bound4) {
        JLabel lab = new JLabel(label);
        panel.add(lab);
        lab.setVisible(true);
        lab.setBounds(bound1,bound2,bound3,bound4);
        return lab;
    }

    static private JTextField addText(JPanel panel, int bound1, int bound2, int bound3, int bound4) {
        JTextField textField = new JTextField(20);
        panel.add(textField);
        textField.setVisible(true);
        textField.setBounds(bound1,bound2,bound3,bound4);
        return textField;
    }

}