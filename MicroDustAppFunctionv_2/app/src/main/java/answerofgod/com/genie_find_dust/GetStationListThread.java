package answerofgod.com.genie_find_dust;

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
 *  @modify Jeonghun Shin
 *
 */

/**
 * 기상청을 연결하는 스레드
 * 이곳에서 풀파서를 이용하여 기상청에서 정보를 받아와 각각의 array변수에 넣어줌
 * @author Ans
 */
class GetStationListThread extends Thread {	//기상청 연결을 위한 스레드
	static public boolean active=false;
	//파서용 변수
	int data=0;			//이건 파싱해서 array로 넣을때 번지
	public boolean isreceiver;
	String sTotalCount;	//결과수
	String[] sStationName,sAddr,sTm;	//측정소 이름

	boolean bStationName,bTotalCount,bAddr,bTm;	//여긴 저장을 위한 플래그들
	boolean tResponse;	//이건 text로 뿌리기위한 플래그

	Handler handler;	//날씨저장 핸들러
	String stationUrl;
	String Servicekey="ServiceKey=%2BnC8B80bgTfYosJ7K9WyuER7K7xJ59v1Tji1dYS02%2BgGObhmGrnfs%2BhiVeQXFbKH6X%2FrvHBZdBU7HyZHte1OqQ%3D%3D";
	String getInfo="http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/";
	String getStationFindDust="getMsrstnList?";
	String getNearStationFindDust="getNearbyMsrstnList?";
	String addr="addr=";
	String infoCnt="numOfRows=200";
	String xGrid="tmX=",yGrid="tmY=";
	int getAPI=0;

	public GetStationListThread(boolean receiver, String sido){

		Log.w("스레드가 시이름", sido);
		handler=new Handler();
		isreceiver=receiver;
		 try{
			 addr+=URLEncoder.encode(sido, "utf-8");
		 }catch(Exception e){

		 }

		getAPI=1;	//사용할 API 구분용
		stationUrl=getInfo+getStationFindDust+addr+"&"+infoCnt+"&"+Servicekey;

	}
	public GetStationListThread(boolean receiver, String gridY,String gridX){

		Log.w("받은 TM좌표", gridY+","+gridX);
		handler=new Handler();
		isreceiver=receiver;
		xGrid+=gridX;
		yGrid+=gridY;
		getAPI=2;	//사용할 API 구분용
		stationUrl=getInfo+getNearStationFindDust+xGrid+"&"+yGrid+"&"+infoCnt+"&"+Servicekey;

	}
	public void run(){
		
		if(active){
			try{
				bStationName=bAddr=bTm=false;
				sStationName=new String[100];	//측정소
				sAddr=new String[100];	//주소
				sTm=new String[100];	//거리
				data=0;
				XmlPullParserFactory factory= XmlPullParserFactory.newInstance();	//이곳이 풀파서를 사용하게 하는곳
				factory.setNamespaceAware(true);									//이름에 공백도 인식
				XmlPullParser xpp=factory.newPullParser();							//풀파서 xpp라는 객체 생성
				//String dustUrl=getInfo+getStationFindDust+addr+sidoName+"&"+infoCnt+"&"+Servicekey;
				Log.w("스레드가 받은 ", stationUrl);
				URL url=new URL(stationUrl);		//URL객체생성
				InputStream is=url.openStream();	//연결할 url을 inputstream에 넣어 연결을 하게된다.
				xpp.setInput(is,"UTF-8");			//이렇게 하면 연결이 된다. 포맷형식은 utf-8로

				int eventType=xpp.getEventType();	//풀파서에서 태그정보를 가져온다.

				while(eventType!= XmlPullParser.END_DOCUMENT){	//문서의 끝이 아닐때

					switch(eventType){
						case XmlPullParser.START_TAG:	//'<'시작태그를 만났을때

							if(xpp.getName().equals("stationName")){	//측정소
								bStationName=true;
							}if(xpp.getName().equals("addr")){	//주소
								bAddr=true;
							}if(xpp.getName().equals("tm")){	//거리
								bTm=true;
							}if(xpp.getName().equals("totalCount")){	//측정소 수
								bTotalCount=true;
							}

							break;

						case XmlPullParser.TEXT:	//텍스트를 만났을때
							//앞서 시작태그에서 얻을정보를 만나면 플래그를 true로 했는데 여기서 플래그를 보고
							//변수에 정보를 넣어준 후엔 플래그를 false로~
							if(bStationName){				//동네이름
								sStationName[data]=xpp.getText();
								bStationName=false;
							}if(bAddr){
								sAddr[data]=xpp.getText();
								bAddr=false;
							}if(bTm){
								sTm[data]=xpp.getText();
								bTm=false;
							}if(bTotalCount){
								sTotalCount=xpp.getText();
								bTotalCount=false;
							}
							break;

						case XmlPullParser.END_TAG:		//'</' 엔드태그를 만나면 (이부분이 중요)

							if(xpp.getName().equals("response")){	//respose는 문서의 끝이므로
								tResponse=true;						//따라서 이때 모든 정보를 화면에 뿌려주면 된다.
								view_text();					//뿌려주는 곳~
							}if(xpp.getName().equals("dmY")){	//측정소 리스트의 경우 item태그가 2개이므로
								data++;							//dmY로 구분
							}if(xpp.getName().equals("tm")){	//가까운 측정소 구분은 tm으로 구분
							data++;
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
					Log.e("station cnt",""+sTotalCount);
					data=0;		//
					/*if(getAPI==1)
						MainActivity.StationListThreadResponse(sTotalCount, sStationName);*/
					 if(getAPI==2)
						MainActivity.NearStationThreadResponse(sStationName,sAddr,sTm);


				}
				
				
			}
		});
	}
}
