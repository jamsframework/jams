package gw.ui.util.console;

public interface History {

    void appendCommand(String command);

    String next();

    String prev();
}
