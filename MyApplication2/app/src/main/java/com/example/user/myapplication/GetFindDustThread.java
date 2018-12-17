package com.example.user.myapplication;

import android.os.Handler;
import android.util.Log;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 대기정보를 가져오는 스레드
 *
 ** @modify Jeonghun Shin
 *
 */

/**
 * 기상청을 연결하는 스레드
 * 이곳에서 풀파서를 이용하여 기상청에서 정보를 받아와 각각의 array변수에 넣어줌
 * @author Ans
 */
class GetFindDustThread extends Thread {	//기상청 연결을 위한 스레드
	static public boolean active=false;
	//파서용 변수
	int data=0;			//이건 파싱해서 array로 넣을때 번지
	public boolean isreceiver;
	String sTotalCount;	//결과수
	String[] sDate,sSo2Value,sCoValue,sO3Value,sNo2Value,sPm10Value,sKhaiValue,sKhaiGrade,sSo2Grade,sCoGrade,sO3Grade,sNo2Grade,sPm10Grade;	//예보시간,날짜,온도,풍향,습도,날씨
	boolean bTotalCount,bDate,bSo2Value,bCoValue,bO3Value,bNo2Value,bPm10Value,bKhaiValue,bKhaiGrade,bSo2Grade,bCoGrade,bO3Grade,bNo2Grade,bPm10Grade;	//여긴 저장을 위한 플래그들
	boolean tResponse;	//이건 text로 뿌리기위한 플래그
	String dongName;
	Handler handler;	//날씨저장 핸들러
	String Servicekey="ServiceKey=%2BnC8B80bgTfYosJ7K9WyuER7K7xJ59v1Tji1dYS02%2BgGObhmGrnfs%2BhiVeQXFbKH6X%2FrvHBZdBU7HyZHte1OqQ%3D%3D";
	String getInfo="http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/";
	String getStationFindDust="getMsrstnAcctoRltmMesureDnsty?";
	String searchDate="dataTerm=daily";
	String station="stationName=";
	String infoCnt="numOfRows=50";

	public GetFindDustThread(boolean receiver,String dong){

		Log.w("스레드가 받은 측정소", dong);
		handler=new Handler();
		isreceiver=receiver;
		//dongName=dong;
		try{
			dongName = URLEncoder.encode(dong, "utf-8");
		}catch(Exception e){

		}


		bTotalCount=bDate=bSo2Value=bCoValue=bO3Value=bNo2Value=bPm10Value=bKhaiValue=bKhaiGrade=bSo2Grade=bCoGrade=bO3Grade=bNo2Grade=bPm10Grade=false;	//부울상수는 false로 초기화해주자
	}
	public void run(){

		if(active){
			try{
				sDate=new String[100];	//측정일
				sSo2Value=new String[100];	//아황산가스 농도
				sCoValue=new String[100];	//일산화탄소 농도
				sO3Value=new String[100];	//오존 농도
				sNo2Value=new String[100];	//이산화질소 농도
				sPm10Value=new String[100];	//미세먼지 농도
				sKhaiValue=new String[100];	//통합 대기환경수치
				sKhaiGrade=new String[100];	//통합 대기환경 지수
				sSo2Grade=new String[100];	//아황산가스 지수
				sCoGrade=new String[100];	//일산화 탄소 지수
				sO3Grade=new String[100];	//오존 지수
				sNo2Grade=new String[100];	//이산화질소 지수
				sPm10Grade=new String[100];	//미세먼지 지수
				data=0;
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();	//이곳이 풀파서를 사용하게 하는곳
				factory.setNamespaceAware(true);									//이름에 공백도 인식
				XmlPullParser xpp=factory.newPullParser();							//풀파서 xpp라는 객체 생성
				String dustUrl=getInfo+getStationFindDust+station+dongName+"&"+infoCnt+"&"+searchDate+"&"+Servicekey;
				Log.w("스레드가 받은 ", dustUrl);
				URL url=new URL(dustUrl);		//URL객체생성
				InputStream is=url.openStream();	//연결할 url을 inputstream에 넣어 연결을 하게된다.
				xpp.setInput(is,"UTF-8");			//이렇게 하면 연결이 된다. 포맷형식은 utf-8로

				int eventType=xpp.getEventType();	//풀파서에서 태그정보를 가져온다.

				while(eventType!= XmlPullParser.END_DOCUMENT){	//문서의 끝이 아닐때

					switch(eventType){
						case XmlPullParser.START_TAG:	//'<'시작태그를 만났을때

							if(xpp.getName().equals("dataTime")){	//측정일
								bDate=true;

							} if(xpp.getName().equals("so2Value")){		//아황산가스 농도
							bSo2Value=true;

						} if(xpp.getName().equals("coValue")){		//일산화탄소 농도
							bCoValue=true;

						} if(xpp.getName().equals("o3Value")){		//오존 농도
							bO3Value=true;

						} if(xpp.getName().equals("no2Value")){		//이산화질소 농도
							bNo2Value=true;

						} if(xpp.getName().equals("pm10Value")){	//미세먼지 농도
							bPm10Value=true;

						} if(xpp.getName().equals("khaiValue")){		//통합대기환경수치
							bKhaiValue=true;

						} if(xpp.getName().equals("khaiGrade")){	//통합대기환경지수
							bKhaiGrade=true;

						}if(xpp.getName().equals("so2Grade")){	//아황산가스 지수
							bSo2Grade=true;

						}if(xpp.getName().equals("coGrade")){	//일산화탄소 지수
							bCoGrade=true;

						}if(xpp.getName().equals("o3Grade")){	//오존 지수
							bO3Grade=true;

						}if(xpp.getName().equals("no2Grade")){	//이산화질소 지수
							bNo2Grade=true;

						}if(xpp.getName().equals("pm10Grade")){	//미세먼지 지수
							bPm10Grade=true;

						}if(xpp.getName().equals("totalCount")){	//결과 수
							bTotalCount=true;

						}

							break;

						case XmlPullParser.TEXT:	//텍스트를 만났을때
							//앞서 시작태그에서 얻을정보를 만나면 플래그를 true로 했는데 여기서 플래그를 보고
							//변수에 정보를 넣어준 후엔 플래그를 false로~
							if(bDate){				//동네이름
								sDate[data]=xpp.getText();
								bDate=false;
							} if(bSo2Value){					//발표시각
							sSo2Value[data]=xpp.getText();
							bSo2Value=false;
						}  if(bCoValue){				//예보시각
							sCoValue[data]=xpp.getText();
							bCoValue=false;
						}  if(bO3Value){				//예보날짜
							sO3Value[data]=xpp.getText();
							bO3Value=false;
						}  if(bNo2Value){				//현재온도
							sNo2Value[data]=xpp.getText();
							bNo2Value=false;
						}  if(bPm10Value){				//풍향
							sPm10Value[data]=xpp.getText();
							bPm10Value=false;
						}  if(bKhaiValue){				//습도
							sKhaiValue[data]=xpp.getText();
							bKhaiValue=false;
						} if(bKhaiGrade){				//날씨
							sKhaiGrade[data]=xpp.getText();
							bKhaiGrade=false;
						}if(bSo2Grade){				//날씨
							sSo2Grade[data]=xpp.getText();
							bSo2Grade=false;
						}if(bCoGrade){				//날씨
							sCoGrade[data]=xpp.getText();
							bCoGrade=false;
						}if(bO3Grade){				//날씨
							sO3Grade[data]=xpp.getText();
							bO3Grade=false;
						}if(bNo2Grade){				//날씨
							sNo2Grade[data]=xpp.getText();
							bNo2Grade=false;
						}if(bPm10Grade){				//날씨
							sPm10Grade[data]=xpp.getText();
							bPm10Grade=false;
						}if(bTotalCount){
							sTotalCount=xpp.getText();
							bTotalCount=false;
						}
							break;

						case XmlPullParser.END_TAG:		//'</' 엔드태그를 만나면 (이부분이 중요)

							if(xpp.getName().equals("response")){	//태그가 끝나느 시점의 태그이름이 item이면(이건 거의 문서의 끝
								tResponse=true;						//따라서 이때 모든 정보를 화면에 뿌려주면 된다.
								view_text();					//뿌려주는 곳~
							}if(xpp.getName().equals("item")){	//item 예보시각기준 예보정보가 하나씩이다.
							data++;							//즉 item == 예보 개수 그러므로 이때 array를 증가해주자
						}
							break;
					}
					eventType=xpp.next();	//이건 다음 이벤트로~
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}



	}

	/**
	 * 이 부분이 뿌려주는곳
	 * 뿌리는건 핸들러가~
	 * @author Ans
	 */
	private void view_text(){

		handler.post(new Runnable() {	//기본 핸들러니깐 handler.post하면됨

			@Override
			public void run() {

				active=false;
				if(tResponse){		//문서를 다 읽었다
					tResponse=false;
					data=0;		//
					MainActivity.FindDustThreadResponse(sTotalCount,sDate,sSo2Value,sCoValue,sO3Value,sNo2Value,sPm10Value,sKhaiValue,sKhaiGrade,sSo2Grade,sNo2Grade,sCoGrade,sO3Grade,sPm10Grade);

				}


			}
		});
	}
}