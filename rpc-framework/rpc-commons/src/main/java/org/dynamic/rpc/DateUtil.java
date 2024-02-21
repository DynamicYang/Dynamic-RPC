package org.dynamic.rpc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: DynamicYang
 * @create: 2024-02-20
 * @Description:
 */
public class DateUtil {
    public static Date get(String pattern){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date parse = null;
        try {
            parse = format.parse(pattern);

        }catch (Exception e){
            e.printStackTrace();
        }


        return parse;
    }
}
