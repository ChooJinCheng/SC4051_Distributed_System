package utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class PropertyUtil {
    private PropertyUtil (){}
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
