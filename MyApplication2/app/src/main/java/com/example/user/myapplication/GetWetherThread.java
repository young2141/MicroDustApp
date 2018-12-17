package com.example.user.myapplication;

import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by user on 2018-12-12.
 */

class GetWetherThread extends Thread {
    static public boolean active = false;
    ArrayList<ShortWeather> shortWeathers = new ArrayList<ShortWeather>();    //측정소 이름
    int data = 0;
    int i = 0;
    boolean bHour, bDay, bTem, bWfKor, onEnd, isItemTag1, bPop, bTmx, bTmn;    //여긴 저장을 위한 플래그들
    boolean tResponse;
    Handler handler;
    String weatherUrl;
    String getInfo = "http://www.kma.go.kr/wid/queryDFS.jsp?";
    String xGrid = "gridx=", yGrid = "gridy=";

    public GetWetherThread(String gridX, String gridY) {
        handler = new Handler();
        xGrid += gridX;
        yGrid += gridY;
        Log.w("웨더스레드가받은 좌표", gridX + "," + gridY);
        weatherUrl = getInfo + xGrid + "&" + yGrid;
    }

    public void run() {
        if (active) {
            try {
              String tagName = "";
                bHour = bDay = onEnd = bTem = bWfKor = isItemTag1 = bPop = bTmx = bTmn = false;
/*

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();    //이곳이 풀파서를 사용하게 하는곳
                factory.setNamespaceAware(true);                                    //이름에 공백도 인식
                XmlPullParser xpp = factory.newPullParser();                            //풀파서 xpp라는 객체 생성
                URL url = new URL(weatherUrl);        //URL객체생성
                InputStream is = url.openStream();    //연결할 url을 inputstream에 넣어 연결을 하게된다.
                xpp.setInput(is, "UTF-8");            //이렇게 하면 연결이 된다. 포맷형식은 utf-8로

                int eventType = xpp.getEventType();    //풀파서에서 태그정보를 가져온다.*/

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(weatherUrl)
                        .build();

                Response response = null;

                response = client.newCall(request).execute();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new StringReader(response.body().string()));
                int eventType = xpp.getEventType();    //풀파서에서 태그정보를 가져온다.*/

                while (eventType != XmlPullParser.END_DOCUMENT) {    //문서의 끝이 아닐때
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = xpp.getName();
                        if (xpp.getName().equals("data")) {
                           // Log.w("---------야임마**** ", weatherUrl);
                            shortWeathers.add(new ShortWeather());
                            onEnd = false;
                            isItemTag1 = true;
                        }
                    } else if (eventType == XmlPullParser.TEXT && isItemTag1) {
                       // Log.w("here**********요기요**** ", String.valueOf(tagName.equals("hour")));

                        if (tagName.equals("hour") &&!bHour  ) {    //측정소
                            shortWeathers.get(i).setHour(xpp.getText());
                            bHour = true;
                        }
                        if (tagName.equals("day") && !bDay) {    //주소
                            shortWeathers.get(i).setDay(xpp.getText());
                            bDay = true;
                        }
                        if (tagName.equals("temp") && !bTem) {    //거리
                            shortWeathers.get(i).setTemp(xpp.getText());
                            bTem = true;
                        }
                        if (tagName.equals("tmx") && !bTmx) {    //측정소 수
                            shortWeathers.get(i).setTmx(xpp.getText());
                            bTmx = true;
                        }
                        if (tagName.equals("tmn") && !bTmn) {    //측정소 수
                            shortWeathers.get(i).setTmn(xpp.getText());
                            bTmn = true;
                        }
                        if (tagName.equals("wfKor") && !bWfKor) {    //측정소 수
                            shortWeathers.get(i).setWfKor(xpp.getText());
                            bWfKor = true;
                        }
                        if (tagName.equals("pop") && !bPop) {    //측정소 수
                            shortWeathers.get(i).setPop(xpp.getText());
                            bPop = true;
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (tagName.equals("s06") && onEnd == false) {
                            i++;
                            bHour = false;
                            bDay = false;
                            bTem = false;
                            bTmx = false;
                            bTmn = false;
                            bWfKor = false;
                            bPop = false;
                            isItemTag1 = false;
                            onEnd = true;
                            tResponse = true;
                        }
                        view_text();


                    }


                    eventType = xpp.next();    //이건 다음 이벤트로~

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void view_text() {

        handler.post(new Runnable() {    //기본 핸들러니깐 handler.post하면됨

            @Override
            public void run() {

                active = false;
                if (tResponse) {        //문서를 다 읽었다
                    tResponse = false;
                    i = 0;
                    Log.w("---------야임마**** ", weatherUrl);
                    com.example.user.myapplication.MainActivity.WeatherThreadResponse(shortWeathers);

               }


            }
        });
    }
}
