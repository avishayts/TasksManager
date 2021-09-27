import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StatusColumnCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        //Cells are by default rendered as a JLabel.
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        //Get the status for the current row.
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        if (tableModel.getValueAt(row, col) == "High") {
            l.setForeground(new Color(232,57,95));
        } else if (tableModel.getValueAt(row, col) == "Medium") {
            l.setForeground(new Color(255, 156, 0));
        } else if (tableModel.getValueAt(row, col) == "Low") {
            l.setForeground(new Color(84, 208, 79, 255));
        } else l.setForeground(Color.BLACK);
        if (tableModel.getValueAt(row, 2).equals("Done")) {
            l.setForeground(Color.LIGHT_GRAY);
        }
        return l;
    }
}