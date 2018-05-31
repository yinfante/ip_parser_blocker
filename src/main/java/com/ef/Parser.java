package com.ef;

import com.ef.domain.ParserDTO;
import com.ef.parser.ParserInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * It parses web server access log file, loads the log to MySQL and checks if a given IP makes more than a certain number of requests for the given duration.
 * <p>
 * It expects the following execution arguments (not in the same order):
 * * --accesslog location of log file
 * * --startDate start date time
 * * --duration time period. It can be "hourly" or "daily"
 * * --threshold number of requests threshold
 * </p>
 * <p>
 * ex:
 * --accesslog=/Users/yariel/Downloads/Java_MySQL_Test/access.log --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100
 * </p>
 *
 * @author yinfante
 */
@Slf4j
public class Parser {


    public static void main(String... args) {

        try {

            Map<String, String> argsMap = new HashMap<>();

            Pattern p = Pattern.compile("-{1,2}[A-Za-z]+=.+");

            for (String arg : args) {
                if (!p.matcher(arg).matches()) {
                    throw new IllegalArgumentException("arguments must follow format --{argument}={value}");
                }
                String[] split = arg.split("=");
                argsMap.put(split[0].trim(), split[1].trim());
            }

            String fileUrl = argsMap.get("--accesslog");
            String startDate = argsMap.get("--startDate");
            String duration = argsMap.get("--duration");
            int threshold;

            try {

                threshold = Integer.valueOf(argsMap.get("--threshold"));

            } catch (NumberFormatException e) {
                throw new NumberFormatException("threshold must be a number");
            }

            ParserDTO parserDTO = new ParserDTO(fileUrl, startDate, duration, threshold);

            ParserInvoker parserInvoker = new ParserInvoker();
            BatchStatus batchStatus = parserInvoker.invoke(parserDTO);

            log.info("Batch status : " + batchStatus);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
