package TextDiff;

/**
 * A ReportWriter is a visitor that is passed a series of EditCommands and
 * generates a report of some kind. See DefaultReportWriter.
 */
public interface IReportWriter
{
    public void report(EditCommand aCommand);
}
