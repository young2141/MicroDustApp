package answerofgod.com.genie_find_dust;


import android.content.pm.Signature;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.security.MessageDigest;

/**
 *
 * 
 *  @modify Jeonghun Shin
 *
 */

public class MainActivity extends Activity implements OnClickListener,AdapterView.OnItemSelectedListener,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {

	GoogleApiClient mGoogleApiClient;
	Location mLastLocation;
	Button getBtn,getNearStation;
	static EditText where;
	String from="WGS84";
	String to="TM";
	static Spinner sido,station;	//스피너
	static String sidolist[]={"서울","부산","대전","대구","광주","울산","경기","강원","충북","충남","경북","경남","전북","전남","제주"};
	static String stationlist[];	//측정소목록(이건 api로 가져올꺼라 몇개인지 모른다)
	static ArrayAdapter<String> spinnerSido,spinnerStation;	//spinner에 붙일 array adapter
	static TextView totalcnt,date,so2value,covalue,o3value,no2value,pm10value,khaivalue,so2grade,cograde,o3grade,no2grade,pm10grade,khaigrade;
	static int stationCnt=0;
	static Context mContext;	//static에서 context를 쓰기위해

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getAppKeyHash(); //다음 카카오 api사용을 위한 해쉬키 get을 위한 함수.
		setContentView(R.layout.activity_main);
		init();
   }
   /*
	private void getAppKeyHash() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md;
				md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				String something = new String(Base64.encode(md.digest(), 0));
				Log.d("Hash key", something);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("name not found", e.toString());
		}
	}*/
	public void init() {
		mContext=getApplicationContext();	//static에서 context를 쓰기위해~
		totalcnt=(TextView)findViewById(R.id.totalcnt);
		date=(TextView)findViewById(R.id.date);
		so2value=(TextView)findViewById(R.id.so2value);
		covalue=(TextView)findViewById(R.id.covalue);
		o3value=(TextView)findViewById(R.id.o3value);
		no2value=(TextView)findViewById(R.id.no2value);
		pm10value=(TextView)findViewById(R.id.pm10value);
		khaivalue=(TextView)findViewById(R.id.khaivalue);
		so2grade=(TextView)findViewById(R.id.so2grade);
		cograde=(TextView)findViewById(R.id.cograde);
		o3grade=(TextView)findViewById(R.id.o3grade);
		no2grade=(TextView)findViewById(R.id.no2grade);
		pm10grade=(TextView)findViewById(R.id.pm10grade);
		khaigrade=(TextView)findViewById(R.id.khaigrade);

		where=(EditText)findViewById(R.id.where);
		getNearStation=(Button) findViewById(R.id.getNearStation);		//측정소버튼 객체생성
		getBtn=(Button) findViewById(R.id.getBtn);		//대기정보버튼 객체생성

		sido=(Spinner)findViewById(R.id.sido);	//시도 스피너
		station=(Spinner)findViewById(R.id.station);	//측정소 스피너
		sido.setOnItemSelectedListener(this);	//스피너 선택할때 작동시킬 리스너등록
		station.setOnItemSelectedListener(this);

		getNearStation.setOnClickListener(this); //gps로 가까운 측정소 가져오는 버튼 리스너
		getBtn.setOnClickListener(this);	//대기정보 가져오는 버튼 리스너
		spinnerSido=new ArrayAdapter<>(getApplication(), R.layout.spinner_text,sidolist);	//array adapter에 시도 리스트를 넣어줌
		sido.setAdapter(spinnerSido);	//스피너에 adapter를 연결
		mGoogleApiClient = new GoogleApiClient.Builder(this)	//google service
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();



	}



	public static void getFindDust(String name){	//대기정보를 가져오는 스레드

		GetFindDustThread.active=true;
		GetFindDustThread getweatherthread=new GetFindDustThread(false,name);		//스레드생성(UI 스레드사용시 system 뻗는다)
		getweatherthread.start();	//스레드 시작
		
	}
	
	public static void  FindDustThreadResponse(String getCnt,String[] sDate,String[] sSo2Value,String[] sCoValue,String[] sO3Value,String[] sNo2Value,String[] sPm10Value,String[] sKhaiValue,String[] sKhaiGrade,String[] sSo2Grade,String[] sNo2Grade,String[] sCoGrade,String[] sO3Grade,String[] sPm10Grade){	//대기정보 가져온 결과값
		stationCnt=0;	//측정개수정보(여기선 1개만 가져온다
		stationCnt=Integer.parseInt(getCnt);

		Log.w("stationcnt", String.valueOf(stationCnt));

		 if(stationCnt==0) {	//만약 측정정보가 없다면
			 totalcnt.setText("측정소 정보가 없거나 측정정보가 없습니다.");
			 date.setText("");	//
			 so2value.setText("");
			 covalue.setText("");
			 no2value.setText("");
			 o3value.setText("");
			 pm10value.setText("");
			 khaivalue.setText("");
			 khaigrade.setText("");
			 so2grade.setText("");
			 no2grade.setText("");
			 cograde.setText("");
			 o3grade.setText("");
			 no2grade.setText("");
			 pm10grade.setText("");
		 }else{	//측정정보있으면
			 totalcnt.setText(sDate[0] + "에 대기정보가 업데이트 되었습니다.");

			 date.setText(sDate[0]);	//
			 so2value.setText(sSo2Value[0]+"ppm");
			 covalue.setText(sCoValue[0]+"ppm");
			 no2value.setText(sNo2Value[0]+"ppm");
			 o3value.setText(sO3Value[0]+"ppm");
			 pm10value.setText(sPm10Value[0]+"μg/m³");
			 khaivalue.setText(sKhaiValue[0]);
			 khaigrade.setText(transGrade(sKhaiGrade[0]));
			 so2grade.setText(transGrade(sSo2Grade[0]));
			 no2grade.setText(transGrade(sNo2Grade[0]));
			 cograde.setText(transGrade(sCoGrade[0]));
			 o3grade.setText(transGrade(sO3Grade[0]));
			 no2grade.setText(transGrade(sNo2Grade[0]));
			 pm10grade.setText(transGrade(sPm10Grade[0]));

		 }

		GetFindDustThread.active=false;
		GetFindDustThread.interrupted();
	}
	public static void getStationList(String name){	//이건 측정소 정보가져올 스레드

		GetStationListThread.active=true;
		GetStationListThread getstationthread=new GetStationListThread(false,name);		//스레드생성(UI 스레드사용시 system 뻗는다)
		getstationthread.start();	//스레드 시작

	}

	public static void  StationListThreadResponse(String cnt,String[] sStation){	//측정소 정보를 가져온 결과
		stationCnt=0;
		stationCnt=Integer.parseInt(cnt);
		stationlist=new String[stationCnt];
		for(int i=0;i<stationCnt;i++){
			stationlist[i]=sStation[i];
			Log.e("station",stationlist[i]);
		}
		//stationlist=sStation;
		//if(stationCnt!=0){
			spinnerStation=new ArrayAdapter<>(mContext,R.layout.spinner_text,stationlist);
			station.setAdapter(spinnerStation);
		//}


		GetFindDustThread.active=false;
		GetFindDustThread.interrupted();



	}
	public static void getNearStation(String yGrid,String xGrid){	//이건 측정소 정보가져올 스레드


		GetStationListThread.active=true;
		GetStationListThread getstationthread=new GetStationListThread(false,yGrid,xGrid);		//스레드생성(UI 스레드사용시 system 뻗는다)
		getstationthread.start();	//스레드 시작

	}
	public static void  NearStationThreadResponse(String[] sStation,String[] sAddr,String[] sTm){	//측정소 정보를 가져온 결과
		where.setText(sStation[0]);
		totalcnt.setText("가까운 측정소 :" + sStation[0] + "\r\n측정소 주소 :" +sAddr[0]+"\r\n측정소까지 거리 :"+sTm[0]+"km");
		GetFindDustThread.active=false;
		GetFindDustThread.interrupted();
	}

	void getStation(String yGrid,String xGrid){
		double dx,dy;
		if(xGrid!=null&&yGrid!=null){
			dx=Double.parseDouble(xGrid);
			dy=Double.parseDouble(yGrid);
			//GetTransCoordThread.active=true;
			//GetTransCoordThread getCoordthread=new GetTransCoordThread(false,xGrid,yGrid,from,to);		//스레드생성(UI 스레드사용시 system 뻗는다)
			//getCoordthread.start();	//스레드 시작
			GeoPoint in_pt = new GeoPoint(dx, dy);
			GeoPoint tm_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
			xGrid=Double.toString(tm_pt.getX());
			yGrid=Double.toString(tm_pt.getY());
			getNearStation(yGrid,xGrid);
		}else{
			Toast.makeText(getApplication(), "좌표값 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
		}

	}
	/*
	public static void  TransCoordThreadResponse(String x,String y) {    //대기정보 가져온 결과값

		if (x.equals("NaN")||y.equals("NaN")){
			totalcnt.setText("좌표값이 잘못 입력되었거나 해당값이 없습니다.");
		}else{
			//totalcnt.append("\r\n변환된 좌표값은 " + x + "," + y + "입니다.");

			getNearStation(y, x);
		}
		GetTransCoordThread.active=false;
	}*/

	static public String transGrade(String intGrade){
		String trans=null;
		switch (intGrade){
			case "1":
				trans="좋음";
				break;
			case "2":
				trans="보통";
				break;
			case "3":
				trans="나쁨";
				break;
			case "4":
				trans="매우나쁨";
				break;
			default:
				break;

		}
		return trans;
	}

	/**
	 * 버튼에 대한 처리
	 */

	public void onClick(View v) {
		
		switch(v.getId()){

		case R.id.getBtn:	//대기정보 가져오는 버튼
			String stationName;
			stationName=where.getText().toString();
			getFindDust(stationName);

			break;
		case R.id.getNearStation:
			mGoogleApiClient.connect();
			break;
		default:
			break;
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

		switch(parent.getId()){

			case R.id.sido:		//시도 변경 스피너
				getStationList(sidolist[position]);


				break;
			case R.id.station:	//측정소 변경 스피너
				try{
					Log.e("station name", stationlist[position]);
				}catch (Exception e){
					Log.e("exception",""+e);
				}

				where.setText(stationlist[position]);	//측정소이름을 바로 입력해 준다.

				break;


			default:
				break;

		}


	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}


	@Override
	public void onConnected(Bundle bundle) {
		//String x="128.6096169";
		//String y="35.8900695";
		mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		Log.d("mLastLocation",String.valueOf(mLastLocation.getLatitude()) + "," + mLastLocation.getLongitude());
		if (mLastLocation != null) {

			//totalcnt.setText(String.valueOf(mLastLocation.getLatitude()) + "," + mLastLocation.getLongitude());
			getStation(String.valueOf(mLastLocation.getLatitude()),String.valueOf(mLastLocation.getLongitude()));
			//getStation(y,x);
			//35.8900695,128.6096169
		}else{
			totalcnt.setText("위치를 알 수 없습니다.");
		}
		mGoogleApiClient.disconnect();

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}
}



