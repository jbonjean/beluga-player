package info.bonjean.beluga;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 
 * @author Julien Bonjean
 * 
 *         I don't understand the choice of Apache Pivot to use JSON format for message bundles. I like JSON but I don't see any advantage in this
 *         case. I find it a lot hardier to maintain and less flexible.
 * 
 */
public class ResourceBundleToResources
{
	private static final String RESOURCES_PATH = "/home/jb/dev/beluga/src/main/resources/i18n/";
	private static final String[] RESOURCES = { "messages" };

	public static void main(String[] args)
	{
		for (String resourceName : RESOURCES)
		{
			try
			{
				String inputFile = RESOURCES_PATH + resourceName + ".properties";
				String outputFile = RESOURCES_PATH + resourceName + ".json";

				FileInputStream input = new FileInputStream(inputFile);
				PrintWriter output = new PrintWriter(outputFile);

				Properties properties = new Properties();
				properties.load(input);
				
				output.println("{");
				for (Object key : properties.keySet())
				{
					StringBuffer sb = new StringBuffer();
					sb.append(key);
					sb.append(":\"");
					sb.append(properties.getProperty((String)key));
					sb.append("\",");

					output.println(sb.toString());
				}
				output.println("}");
				output.flush();
				output.close();
				input.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
