/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gw.ui.util.console;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

/**
 * 
 * @author od
 */
public class JConsolePanel extends JTextArea {

    static class DefaultCommandHandler implements CommandHandler {

        @Override
        public String handle(String cmd) {
            return "unknown command '" + cmd + "'";
        }
    }

    static class DefaultPrompt implements Prompt {

        @Override
        public String getPrompt() {
            return "> ";
        }
    }

    static class DefaultHisory implements History {

        List<String> cmds = new ArrayList<String>();
        int cursor;

        @Override
        public String next() {
            if (cursor < cmds.size() - 1) {
                cursor++;
            }
            return cmds.get(cursor);
        }

        @Override
        public String prev() {
            if (cursor > 0) {
                cursor--;
            }
            return cmds.get(cursor);
        }

        @Override
        public void appendCommand(String command) {
            cmds.add(command);
            cursor = cmds.size();
        }
    }
//
    private PrintWriter w = new PrintWriter(new Writer() {

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            JConsolePanel.this.append(new String(cbuf, off, len));
            setCaretPosition(getText().length());
            cp = getCaretPosition();
            JConsolePanel.this.scrollRectToVisible(new Rectangle(0, getHeight(), 1, 1));
        }
    });

    public OutputStream getOutputStream() {
        return new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                JConsolePanel.this.append(Character.toString((char) b));
                setCaretPosition(getText().length());
                cp = getCaretPosition();
                JConsolePanel.this.scrollRectToVisible(new Rectangle(0, getHeight(), 1, 1));
            }
        };
    }

    public void prompt() {
        JConsolePanel.this.append(prompt.getPrompt());
        setCaretPosition(getText().length());
        cp = getCaretPosition();
        JConsolePanel.this.scrollRectToVisible(new Rectangle(0, getHeight(), 1, 1));
    }
//
    CommandHandler cmd;
    Prompt prompt;
    History history;
    int cp;

    public JConsolePanel(Prompt p, CommandHandler cmd, History hist) {
        setPromptProvider(p);
        setCommandHandler(cmd);
        setHistory(hist);

        setText(prompt.getPrompt());
        cp = prompt.getPrompt().length();

        setEditable(true);
        setLineWrap(true);
        setRows(3);
        setColumns(20);

        addKeyListener(new KListener());
        setCaretPosition(getText().length());
        setFont(new Font("Monospaced", 1, 12));
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
//                System.out.println(getSelectedText());
                setCaretPosition(getText().length());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    public JConsolePanel() {
        this(new DefaultPrompt(), new DefaultCommandHandler(), new DefaultHisory());
    }

    public void clear() {
        setText(prompt.getPrompt());
        cp = prompt.getPrompt().length();
    }

    public void setCommandHandler(CommandHandler h) {
        cmd = h;
    }

    public void setPromptProvider(Prompt p) {
        prompt = p;
    }

    public void setHistory(History h) {
        history = h;
    }

    public PrintWriter getOut() {
        return w;
    }

    private class KListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent key) {
            switch (key.getKeyCode()) {
                case KeyEvent.VK_C:
                    if (key.isControlDown()) {
                        key.consume();
                        append("\n");
                        append(prompt.getPrompt());
                        setCaretPosition(getText().length());
                        cp = getCaretPosition();
                    }
                    break;
                case KeyEvent.VK_UP:
                    String prev = history.prev();
                    replaceRange(prev, cp, getText().length());
                    key.consume();
                    break;
                case KeyEvent.VK_DOWN:
                    String next = history.next();
                    replaceRange(next, cp, getText().length());
                    key.consume();
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_BACK_SPACE:
                    if (cp == getCaretPosition()) {
                        key.consume();
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    key.consume();
                    String t = getText();
                    append("\n");
                    if (cmd != null) {
                        String c = t.substring(cp);
                        if (!c.isEmpty()) {
                            append(cmd.handle(c));
                            append("\n");
                        }
                    }
                    history.appendCommand(t.substring(cp));
                    append(prompt.getPrompt());
                    setCaretPosition(getText().length());
                    cp = getCaretPosition();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JConsolePanel cp = new JConsolePanel();
        cp.getOut().println("test hetre");
        cp.getOut().println("test next");
        cp.prompt();

        JScrollPane sp = new JScrollPane(cp);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        f.getContentPane().add(sp);
        f.setSize(500, 500);
        f.setLocation(300, 300);
        f.setVisible(true);
    }
}