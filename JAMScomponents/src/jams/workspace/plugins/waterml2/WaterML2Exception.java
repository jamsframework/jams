/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.workspace.plugins.waterml2;

/**
 *
 * @author chris
 */
public class WaterML2Exception extends Exception {

    static public class DuplicateEntryException extends WaterML2Exception {

        String name;

        DuplicateEntryException(String nodeName) {
            this.name = nodeName;
        }

        public String toString() {
            return "multiple entries of type " + this.name + " but only one is allowed.";
        }
    }

    static public class UnexpectedNodeException extends WaterML2Exception {

        String name;

        UnexpectedNodeException(String nodeName) {
            this.name = nodeName;
        }

        public String toString() {
            return "unexpected node of type " + this.name;
        }
    }
    String exception;

    public WaterML2Exception() {
    }

    WaterML2Exception(String exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return exception;
    }
}
