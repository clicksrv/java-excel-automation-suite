package frameworkcore.reportutilities;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import frameworkcore.FrameworkStatics.FrameworkEntities;
import frameworkcore.errormanagers.ErrorHandler;
import frameworkcore.errormanagers.ErrorHandler.ErrLvl;

/**
 * @author Vedhanth Reddy (v.reddy.monajigari)
 */
public class SendEmail {

	public static void sendMails(String file, String to, String cc) {
		Runtime runtime = Runtime.getRuntime();
		String relativePath = new File(System.getProperty("user.dir")).getAbsolutePath()
				+ FrameworkEntities.fileSeparator + "Utilities" + FrameworkEntities.fileSeparator
				+ "testing_auto_mail.ps1";

		String command = null;
		if ((StringUtils.isNotEmpty(to))) {
			command = "powershell " + relativePath + " '" + file + "' '" + to + "' '" + cc + "'";
			if (null == cc) {
				command = "powershell " + relativePath + " '" + file + "' '" + to + "'";
			}
			try {

				runtime.exec(command);

				Thread.sleep(100000);
				System.out.println("Mail sent to :" + to);
			} catch (Exception e) {

				// C:/Users/VRI/Desktop
				ErrorHandler.frameworkError(ErrLvl.ERROR, e, "Unable to Send Email");
			}
		}

		/*
		 * String reportFolder = null; ; try { reportFolder = "\\\\" +
		 * InetAddress.getLocalHost().getHostAddress() + "\\c$" + file.substring(2,
		 * file.lastIndexOf("Report.xlsx") - 1) .replace("/", "\\"); } catch
		 * (UnknownHostException e1) { // Auto-generated catch block
		 * e1.printStackTrace(); } System.out.print(reportFolder);
		 */

	}
}
