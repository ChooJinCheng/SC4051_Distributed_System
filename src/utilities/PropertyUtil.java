package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/*
 * This utility class implements access to the properties stated in the config.properties file
 * */
public final class PropertyUtil {
    private PropertyUtil (){}
    /*
     * This method reads in a .properties file and store them in Properties class to be accessed
     * */
    public static Properties getProperty(){
        String configFilePath = "src/config/config.properties";
        try(FileInputStream propsInput = new FileInputStream(configFilePath)){
            Properties prop = new Properties();
            prop.load(propsInput);

            return prop;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
