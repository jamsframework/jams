/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jamsui.juice.documentation;

/**
 *
 * @author chris
 */
public class DocumentationException extends Exception {

    public enum DocumentationExceptionCause {

        docBookPathNull, modelDocumentNull, workspaceNull, documentationPathNull,
        xsltProcNotExisting, docBookXSLNotExisting, unknownError, xmlIOError,
        ApacheFOPFailed, ViewPDFError, templateNotFound, writeFailed,
        invalidXML_SeveralModelTags, zipExtractionError, invalidJarFile, FOPDependenciesIncomplete

    };
    DocumentationExceptionCause cause = null;
    String details;

    public DocumentationException(DocumentationExceptionCause cause) {
        this.cause = cause;
    }

    public DocumentationException(DocumentationExceptionCause cause, String details) {
        this.cause = cause;
        this.details = details;
    }

    @Override
    public String toString() {
        switch (cause) {
            case docBookPathNull:
                return "Please add a docbook-home entry to your JAMS property file (jap)";
            case modelDocumentNull:
                return "The model document is not valid, this should never happen!";
            case documentationPathNull:
                return "The documentation directory is either not existing or not valid";
            case workspaceNull:
                return "The workspace path is not setup correctly";
            case xsltProcNotExisting:
                return "The programm xsltproc was not found in the docbook-home directory";
            case docBookXSLNotExisting:
                return "The docbook.xsl Style is not existing in the docbook-home directory";
            case templateNotFound:
                return "The template with name " + details + " was not found";
            case writeFailed:
                return "A file could not be written. The reason for this is:" + details;
            case ApacheFOPFailed:
                return "The FOP Processing System return an error:" + details;
            case xmlIOError:
                return "The model XML file could not written. The underlying cause is:" + details;
            case ViewPDFError:
                return "The pdf File cannot be opened. Details are: " + details;
            case invalidXML_SeveralModelTags:
                return "The model document has several model tags.";
            case zipExtractionError:
                return "Unable to extract a zip entry. The underlying IOException is: " + details;
            case invalidJarFile:
                return "A jar file is not valid. The file in question is:" + details + ".";
            case unknownError:
                return "An unknown error occured. Details:" + details;
            case FOPDependenciesIncomplete:
                return "Apache FOP Dependencies are not valid. (check Batik.jar, Avalon.jar, commons.logging, fop.jar, xmlgraphics.jar)";
        }
        return "exception";
    }
}
