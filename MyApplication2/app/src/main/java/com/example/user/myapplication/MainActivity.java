package com.example.user.myapplication;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsoluteLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks {
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Button getBtn,getNearStation;
    static TextView where;
    String from="WGS84";
    String to="TM";
    static Spinner sido,station;	//스피너
    static String sidolist[]={"서울","부산","대전","대구","광주","울산","경기","강원","충북","충남","경북","경남","전북","전남","제주"};
    static String stationlist[];	//측정소목록(이건 api로 가져올꺼라 몇개인지 모른다)
    static ArrayAdapter<String> spinnerSido,spinnerStation;	//spinner에 붙일 array adapter
    static TextView totalcnt,totalcnt2,date,so2value,covalue,o3value,no2value,pm10value,khaivalue,so2grade,cograde,o3grade,no2grade,pm10grade,khaigrade;
    static int stationCnt=0;
    static Context mContext;	//static에서 context를 쓰기위해
    ImageButton tempImage;
    public static ImageButton imageButton = null;
    public static RelativeLayout layout;
    public static AbsoluteLayout layout2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dust_monitor_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init2();
    }

    public void init2(){
        layout =   (RelativeLayout)findViewById(R.id.dustmonitor_relativelayout);
        layout2 =  (AbsoluteLayout)findViewById(R.id.dustmonitor_abslayout);
        imageButton = (ImageButton)findViewById(R.id.dustmonitor_vectorDust);
        tempImage = new ImageButton(this);
        mContext=getApplicationContext();	//static에서 context를 쓰기위해~
        pm10value=(TextView)findViewById(R.id.dustmonitor_density);
        pm10grade=(TextView)findViewById(R.id.dustmoniotr_textlevel);
        where=(TextView)findViewById(R.id.dustmonitor_location);
        imageButton.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)	//google service
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.personal_statics) {
            // Handle the camera action
        } else if (id == R.id.server_statics) {

        } else if (id == R.id.dust_map) {

        } else if (id == R.id.peronal_log) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
            // totalcnt.setText("측정소 정보가 없거나 측정정보가 없습니다.");
            // date.setText("");	//
			/* so2value.setText("");
			 covalue.setText("");
			 no2value.setText("");
			 o3value.setText("");*/
            pm10value.setText("측정불가");
			/* khaivalue.setText("");
			 khaigrade.setText("");
			 so2grade.setText("");
			 no2grade.setText("");
			 cograde.setText("");
			 o3grade.setText("");
			 no2grade.setText("");*/
            pm10grade.setText("측정불가");
        }else{	//측정정보있으면
            // totalcnt.setText(sDate[0] + "에 대기정보가 업데이트 되었습니다.");

            // date.setText(sDate[0]);	//
			 /*so2value.setText(sSo2Value[0]+"ppm");
			 covalue.setText(sCoValue[0]+"ppm");
			 no2value.setText(sNo2Value[0]+"ppm");
			 o3value.setText(sO3Value[0]+"ppm");
			*/
            pm10value.setText(sPm10Value[0]+"μg/m³");
            float densitytemp = Float.parseFloat( String.format(sPm10Value[0]) );
            if(densitytemp< 0 || densitytemp> 500) {
                pm10value.setText("에러");
            }
            else if (densitytemp > 0 && densitytemp < 50 ){
                pm10grade.setText("좋음");
                pm10value.setText(sPm10Value[0]+"μg/m³");
                layout.setBackgroundResource(R.color.good);
                layout2.setBackgroundResource(R.color.goodunder);
                imageButton.setImageResource(R.drawable.good);
            }
            //농도 : 보통
            else if ( densitytemp >= 50 && densitytemp < 100 ) {
                pm10grade.setText("보통");
                layout.setBackgroundResource(R.color.soso);
                layout2.setBackgroundResource(R.color.sosounder);
                imageButton.setImageResource(R.drawable.soso);
            }
            //농도 : 나쁨
            else if ( densitytemp >= 100 && densitytemp < 250 ) {
                pm10grade.setText("나쁨");
                layout.setBackgroundResource(R.color.bad);
                layout2.setBackgroundResource(R.color.badunder);
                imageButton.setImageResource(R.drawable.bad);
            }
            //농도 : 매우 나쁨
            else if ( densitytemp >= 250 ) {
                pm10grade.setText("매우나쁨");
                layout.setBackgroundResource(R.color.verybad);
                layout2.setBackgroundResource(R.color.verybadunder);
                imageButton.setImageResource(R.drawable.verybad);
            }

			/* khaivalue.setText(sKhaiValue[0]);
			 khaigrade.setText(transGrade(sKhaiGrade[0]));
			 so2grade.setText(transGrade(sSo2Grade[0]));
			 no2grade.setText(transGrade(sNo2Grade[0]));
			 cograde.setText(transGrade(sCoGrade[0]));
			 o3grade.setText(transGrade(sO3Grade[0]));
			 no2grade.setText(transGrade(sNo2Grade[0]));*/


        }

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
     
        GetFindDustThread.active=false;
        GetFindDustThread.interrupted();
    }
    void getStation(String yGrid,String xGrid){
        double dx,dy;
        if(xGrid!=null&&yGrid!=null){
            dx=Double.parseDouble(xGrid);
            dy=Double.parseDouble(yGrid);

            GeoPoint in_pt = new GeoPoint(dx, dy);
            GeoPoint tm_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.TM, in_pt);
            xGrid=Double.toString(tm_pt.getX());
            yGrid=Double.toString(tm_pt.getY());
            getNearStation(yGrid,xGrid);
        }else{
            Toast.makeText(getApplication(), "좌표값 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.dustmonitor_vectorDust:
            //    pm10value.setText("측정불가");
                mGoogleApiClient.connect();
                String stationName;
                stationName=where.getText().toString();
                getFindDust(stationName);
                break;
            default:
                break;
        }
    }
    public void onConnected(Bundle bundle) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {

            getStation(String.valueOf(mLastLocation.getLatitude()),String.valueOf(mLastLocation.getLongitude()));

        }else{
            where.setText("위치를 알 수 없습니다.");
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
