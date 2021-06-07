/*
今回使ったAPI　　

Tokyo乗り換えAPI
https://api.trip2.jp/

OpenWeatherMap
https://openweathermap.org/

HeartRails Express | 路線／駅名／最寄駅データサービス　　
http://express.heartrails.com/　　

Copyright (C) 2006 - 2019 HeartRails Inc. Some Rights Reserved.

*/

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrainChange_Weather {
    public static void main(String[] args) {
        TrainChange_Weather tcw = new TrainChange_Weather();
        //検索語の入力
        Scanner scanner = new Scanner(System.in);

        System.out.println("出発駅と到着駅を入力してください");
        ArrayList<String> tr = new ArrayList<>();
        String dat = "";
        while ((!(dat = scanner.nextLine()).equals(""))) {
            tr.add(dat);
        }
        System.out.println("出発駅は:" + tr.get(0)+"駅");
        System.out.println("到着駅は:" + tr.get(1)+"駅");
        System.out.println("");
        try {
            //電車の乗り換えのデータを取得
            Train_change tc = new Train_change();
            tc.url_information(tr);
            //System.out.println(tc.getDate());

            //到着駅の緯度、経度のデータ取得
            Tr_information ti = new Tr_information();
            ti.url_information(tr, tcw);

            //到着駅の緯度、経度のデータを活用して、到着駅の天気のデータを取得
            We_information wi = new We_information();
            wi.url_information(tcw, ti.getX(), ti.getY());

            tcw.date_calculation(tc.getDate(), wi.tt, wi.ft, wi.tmi, wi.tma, wi.we);

            if (tcw.date_calculation_calender(tc.getDate(), wi.tt) <= 80) {

                System.out.println(tr.get(1) + "の天気は以下の通りです。");
                System.out.println("");
                for (int i = 0; i < 2; i++) {
                    System.out.println(wi.ft.get(i) + "から" + wi.tt.get(i) + "までの天気予報");
                    System.out.println("");
                    System.out.println("最低温度:" + wi.tmi.get(i));
                    System.out.println("最高温度:" + wi.tma.get(i));
                    System.out.println("天気:" + wi.we.get(i));
                    System.out.println("");
                }
            } else {
                System.out.println(tr.get(1) + "の天気は以下の通りです。");
                System.out.println("");
                System.out.println(wi.ft.get(0) + "から" + wi.tt.get(0) + "までの天気予報");
                System.out.println("最低温度:" + wi.tmi.get(0));
                System.out.println("最高温度:" + wi.tma.get(0));
                System.out.println("天気:" + wi.we.get(0));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOM Tree の構築
     */
    public Document buildDocument(InputStream inputStream, String encoding) {
        Document document = null;
        try {
            // DOM実装(implementation)の用意 (Load and Save用)
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS implementation = (DOMImplementationLS) registry.getDOMImplementation("XML 1.0");
            // 読み込み対象の用意
            LSInput input = implementation.createLSInput();
            input.setByteStream(inputStream);
            input.setEncoding(encoding);
            // 構文解析器(parser)の用意
            LSParser parser = implementation.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
            parser.getDomConfig().setParameter("namespaces", false);
            // DOMの構築
            document = parser.parse(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    public void date_calculation(Date date, ArrayList<Date> tt, ArrayList<Date> ft, ArrayList<String> tmi, ArrayList<String> tma, ArrayList<String> we) {
        int result;
        for (int i = 0; i < tt.size(); i++) {
            result = date.compareTo(tt.get(i));
            if (result == 1) {
                tt.remove(i);
                ft.remove(i);
                tmi.remove(i);
                tma.remove(i);
                we.remove(i);
            }
        }
    }

    public Long date_calculation_calender(Date date, ArrayList<Date> tt) {
        //Date型をlong型に変換して差を求める準備

        long date_arrive = date.getTime();
        long date_totime = tt.get(0).getTime();

        //差分を表示する
        long date_diff = (date_totime - date_arrive) / (1000 * 60);

        return date_diff;
    }
}

class Train_change {
    private Date date;

    private int num_all;

    private int hour;

    private int minute;

    public Date getDate() {
        return this.date;
    }

    public void url_information(ArrayList<String> tr) {
        try {
            URL url = new URL("https://api.trip2.jp/ex/tokyo/v1.0/json?src=" + URLEncoder.encode(tr.get(0), "UTF-8") + "&dst=" + URLEncoder.encode(tr.get(1), "UTF-8") + "&key=114.184.216.100");
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;


            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            //jsonから時刻を抜き出す

            String sub = responseStrBuilder.substring(74);
            String min = "(min\":)([0-9]*)";
            ArrayList<Integer> mins = new ArrayList<>();
            Pattern pt = Pattern.compile(min);
            Matcher mt = pt.matcher(sub);
            while (true) {
                if (mt.find()) {
                    String number = mt.group(2);
                    Integer numbers = Integer.parseInt(number);
                    mins.add(numbers);
                } else {
                    break;
                }
            }

            //時刻をデータ型に直す
            for (int i = 0; i < mins.size(); i++) {
                if (mins.get(i) != null) {
                    num_all += mins.get(i);
                }
            }
            if (num_all >= 60) {
                hour = num_all / 60;
                minute = num_all - (60 * hour);
            } else {
                minute = num_all;
            }
            date = new Date(System.currentTimeMillis() + 1000 * 60 * num_all);
            System.out.println("到着予定時刻:" + date);
            System.out.println("");
            System.out.println("移動時間は" + hour + "時間" + minute + "分です");
            System.out.println("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Tr_information {
    private String x;
    private String y;

    public String getX() {
        return this.x;
    }

    public String getY() {
        return this.y;
    }

    public void url_information(ArrayList<String> tr, TrainChange_Weather tcw) {
        try {
            URL url1 = new URL("http://express.heartrails.com/api/xml?method=getStations&name=" + URLEncoder.encode(tr.get(1), "utf-8"));
            URLConnection connection1 = url1.openConnection();
            connection1.connect();
            InputStream inputStream1 = connection1.getInputStream();

            Document document1 = tcw.buildDocument(inputStream1, "utf-8");

            XPath xPath1 = XPathFactory.newInstance().newXPath();

            //緯度経度を取得
            x = xPath1.evaluate("/response/station/x", document1);
            y = xPath1.evaluate("/response/station/y", document1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class We_information {
    private String from_time;
    private String to_time;
    private String temperature_min;
    private String temperature_max;
    private String weather;
    private String weather_change;

    public ArrayList<String> tmi = new ArrayList<>();

    public ArrayList<String> tma = new ArrayList<>();

    public ArrayList<String> we = new ArrayList<>();

    public ArrayList<Date> ft = new ArrayList<>();

    public ArrayList<Date> tt = new ArrayList<>();


    public void url_information(TrainChange_Weather tcw, String x, String y) {
        try {
            URL url2 = new URL("http://api.openweathermap.org/data/2.5/forecast?lon=" + x + "&lat=" + y + "&APPID=83b8112a5777a3bca1207d0b9a21dc3d&units=metric&mode=xml");
            URLConnection connection2 = url2.openConnection();
            connection2.connect();
            InputStream inputStream2 = connection2.getInputStream();

            Document document2 = tcw.buildDocument(inputStream2, "utf-8");

            XPath xPath2 = XPathFactory.newInstance().newXPath();

            NodeList weatherList = (NodeList) xPath2.evaluate("/weatherdata/forecast/time",
                    document2, XPathConstants.NODESET);
            for (int i = 0; i < weatherList.getLength(); i++) {
                Node nameNode = weatherList.item(i);

                //fromの時間を表示
                from_time = xPath2.evaluate("@from", nameNode);
                SimpleDateFormat sdfIso8601ExtendedFormatUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.JAPAN);
                Date date = sdfIso8601ExtendedFormatUtc.parse(from_time);
                //Date型の日時をCalender型に変換
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                //日時を加算する
                calendar.add(Calendar.HOUR, 9);

                //Calender型の日時をDate型に戻す
                Date d1 = calendar.getTime();
                ft.add(d1);

                //toの時間を表示
                to_time = xPath2.evaluate("@to", nameNode);
                SimpleDateFormat sdfIso8601ExtendedFormatUtc1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.JAPAN);
                Date date1 = sdfIso8601ExtendedFormatUtc1.parse(to_time);
                //Date型の日時をCalender型に変換
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(date1);
                //日時を加算する
                calendar1.add(Calendar.HOUR, 9);

                //Calender型の日時をDate型に戻す
                Date d2 = calendar1.getTime();
                tt.add(d2);

                //最低温度を表示
                temperature_min = xPath2.evaluate("temperature/@min", nameNode);
                tmi.add(temperature_min);

                //最高温度を表示
                temperature_max = xPath2.evaluate("temperature/@max", nameNode);
                tma.add(temperature_max);

                //天気を表示
                weather = xPath2.evaluate("symbol/@name", nameNode);
                if (weather.equals("clear sky") || weather.equals(" few clouds")) {
                    weather_change = "☀";
                }
                if (weather.equals("scattered clouds") || weather.equals("broken clouds")) {
                    weather_change = "☁";
                }
                if (weather.equals(" shower rain") || weather.equals("rain")) {
                    weather_change = "☔";
                }
                if (weather.equals("thunderstorm")) {
                    weather_change = "⚡";
                }
                if (weather.equals("snow")) {
                    weather_change = "☃";
                }
                we.add(weather_change);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
