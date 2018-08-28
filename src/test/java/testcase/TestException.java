package testcase;

import com.muppet.util.ExceptionDSL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.*;
import java.util.Optional;

public class TestException {


    private Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() {

        InputStream inputStream = System.in;
        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        Reader bufferedReader = new BufferedReader(inputStreamReader);

        ExceptionDSL.unHandleExceptionSafe(() -> {
            if (bufferedReader != null) bufferedReader.close();
        });

        ExceptionDSL.unHandleExceptionSafe(() -> {
            if (inputStreamReader != null) inputStream.close();
        });

        ExceptionDSL.unHandleExceptionSafe(() -> {
            if (inputStream != null) inputStream.close();
        });


        if (null != bufferedReader) {
            try {
                bufferedReader.close();
            } catch (IOException e) {

                e.printStackTrace();
            } finally {
                if (null != inputStreamReader) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {

                        e.printStackTrace();
                    } finally {
                        if (null != inputStream) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }


    }
}
