package in.sanrakshak.sanrakshak;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanks.htextview.evaporate.EvaporateTextView;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.rm.rmswitch.RMSwitch;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;

public class HomeActivity extends AppCompatActivity {
    RelativeLayout logo_div,splash_cover,sheet_pane,backoverlay,logo_div_fade,actionbar,navbar,menu_profile_edit;
    RelativeLayout camera_pane,permission_camera,galary,click_pane;
    Button allow_camera;
    CardView menupane,sheet,menu_profile_Card;
    ProgressBar proSplash,loading_profile;
    ImageView ico_splash,menu,done,menu_cover,dp_cover,dob_chooser,click,camera_flip,flash;
    TextView page_tag,sheet_title,sheet_msg,sheet_action,menu_fname,menu_lname,menu_email;
    TextView gender_tag,f_name,l_name,dob,aadhaar;
    EvaporateTextView appNameSplash;
    CircularImageView menu_profile,profile;
    CameraView cameraView;
    UCrop.Options options;
    RMSwitch gender,server;
    Animator animator;
    CardView data_div;
    String profile_url="",profile_path="";
    int sesMODE = 1;
    Bitmap profile_dp=null;
    ObjectAnimator startAnim;
    Point screenSize;
    ToolTipsManager toolTip;
    RecyclerView home;
    double diagonal;
    Boolean menuOpen=false,profileOpen=false,profile_lp=false,camOn=false,galaryOn=false,isDP_added=false;
    OkHttpClient client;
    SwipeRefreshLayout refresh;
    RequestBody postBody=null;
    SharedPreferences user, crack, app;
    SharedPreferences.Editor user_edit, crack_edit, app_edit;
    List<Cracks> cracks;
    CrackAdapter crackAdapter;
    GoogleSignInOptions gso;
    GoogleSignInClient gclient;
    GoogleSignInAccount account;
    @Override
    public void onBackPressed() {
        if(camOn)
        {
            closeCam();
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(camera_pane.getVisibility()== View.VISIBLE)
        {
            click.setVisibility(View.GONE);
            if(cameraView.isCameraOpened()){cameraView.stop();}
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(camera_pane.getVisibility()== View.VISIBLE)
        {
            if(checkPerm() && !cameraView.isCameraOpened()){cameraView.start();cameraListener();}
            new Handler().postDelayed(() -> {
                click.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.click_grow);click.startAnimation(anim);
            },500);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setLightTheme(true,true);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gclient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);

        screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        diagonal=Math.sqrt((screenSize.x*screenSize.x) + (screenSize.y*screenSize.y));
        splash_cover=findViewById(R.id.splash_cover);
        logo_div=findViewById(R.id.logo_div);
        logo_div_fade=findViewById(R.id.logo_div_fade);
        toolTip = new ToolTipsManager();
        client = new OkHttpClient.Builder()
                //.connectTimeout(40, TimeUnit.SECONDS).writeTimeout(40, TimeUnit.SECONDS)
                //.readTimeout(40, TimeUnit.SECONDS)
                .build();

        data_div=findViewById(R.id.data_div);
        actionbar=findViewById(R.id.actionbar);
        navbar=findViewById(R.id.navbar);
        scaleY(navbar,getHeightStatusNav(1),0,new OvershootInterpolator());

        appNameSplash=findViewById(R.id.appNameSplash);
        appNameSplash.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));
        setMargins(appNameSplash,0,0,0,dptopx(30) + getHeightStatusNav(1));

        proSplash = findViewById(R.id.proSplash);
        setMargins(proSplash,0,0,0,(int)(dptopx(60) + getHeightStatusNav(1)));

        ico_splash=findViewById(R.id.ico_splash);
        ico_splash.setScaleType(ImageView.ScaleType.CENTER);


        backoverlay=findViewById(R.id.backoverlay);
        backoverlay.setOnClickListener(view -> {
            scaleY(sheet,0,400,new AnticipateInterpolator());
            if(menuOpen){
                menuOpen=false;
                page_tag.setEnabled(false);
                scaleY(menupane,0,400,new AnticipateInterpolator());
                menu.animate().rotationBy(-720).withEndAction(null).setDuration(350).setInterpolator(new DecelerateInterpolator()).start();
                new Handler().postDelayed(() -> {
                    menu.setImageDrawable(getDrawable(R.drawable.menu));
                    menu.setPadding(dptopx(13),dptopx(13),dptopx(13),dptopx(13));
                    page_tag.setText(R.string.home);
                    backoverlay.setVisibility(View.GONE);
                },280);
                new Handler().postDelayed(() -> page_tag.setEnabled(true),400);
            }
            backoverlay.setVisibility(View.GONE);
        });
        menu_cover=findViewById(R.id.menu_cover);
        menupane=findViewById(R.id.menupane);
        menu=findViewById(R.id.menu);

        menu_fname=findViewById(R.id.menu_fname);
        menu_fname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2_bold.otf"));
        menu_lname=findViewById(R.id.menu_lname);
        menu_lname.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2_bold.otf"));
        menu_email=findViewById(R.id.menu_email);
        menu_email.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        page_tag=findViewById(R.id.page_tag);
        page_tag.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));
        page_tag.setOnClickListener(v -> {
            if(!menuOpen && !profileOpen){
                if(crackAdapter.current!=null) crackAdapter.current.navtrigger.performClick();
                menuOpen=true;
                page_tag.setEnabled(false);
                menu_profile_Card.setEnabled(false);
                menu_profile_edit.setVisibility(View.GONE);
                scaleY(menupane,300,400,new OvershootInterpolator());
                menu.animate().rotationBy(720).withEndAction(null).setDuration(350).setInterpolator(new DecelerateInterpolator()).start();
                new Handler().postDelayed(() -> {
                    menu.setImageDrawable(getDrawable(R.drawable.close));
                    menu.setPadding(dptopx(15),dptopx(15),dptopx(15),dptopx(15));
                    page_tag.setText(R.string.menu);
                    backoverlay.setVisibility(View.VISIBLE);
                },70);
                new Handler().postDelayed(() -> {
                    page_tag.setEnabled(true);
                    menu_profile_Card.setEnabled(true);
                },400);
            }
            else if(menuOpen && !profileOpen){
                menuOpen=false;
                page_tag.setEnabled(false);
                menu_profile_Card.setEnabled(false);
                scaleY(menupane,0,400,new AnticipateInterpolator());
                menu.animate().rotationBy(-720).withEndAction(null).setDuration(350).setInterpolator(new DecelerateInterpolator()).start();
                new Handler().postDelayed(() -> {
                    menu.setImageDrawable(getDrawable(R.drawable.menu));
                    menu.setPadding(dptopx(13),dptopx(13),dptopx(13),dptopx(13));
                    page_tag.setText(R.string.home);
                    backoverlay.setVisibility(View.GONE);
                },280);
                new Handler().postDelayed(() -> {
                    page_tag.setEnabled(true);
                    menu_profile_Card.setEnabled(true);
                },400);
            }
            if(profileOpen){
                profileSetUp();
                profileOpen=false;
                page_tag.setEnabled(false);
                menu_profile_Card.setEnabled(false);
                scaleY(menupane,350,500,new OvershootInterpolator());
                menu.animate().rotationBy(-1080).withEndAction(null).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
                AlphaAnimation anims = new AlphaAnimation(1,0);anims.setDuration(300);menu_profile_edit.startAnimation(anims);
                new Handler().postDelayed(() -> {
                    menu.setImageDrawable(getDrawable(R.drawable.close));
                    menu.setPadding(dptopx(15),dptopx(15),dptopx(15),dptopx(15));
                    done.setImageDrawable(getDrawable(R.drawable.logout));
                    done.setEnabled(true);
                    page_tag.setText(R.string.menu);
                },200);
                new Handler().postDelayed(() ->{
                    menu_profile_edit.setVisibility(View.GONE);
                } ,300);
                new Handler().postDelayed(() -> {
                    page_tag.setEnabled(true);
                    menu_profile_Card.setEnabled(true);
                },500);
            }
        });
        menu_profile=findViewById(R.id.menu_profile);
        menu_profile_Card=findViewById(R.id.menu_profile_Card);
        menu_profile_Card.setOnClickListener(view -> {
            if(!profileOpen){
                profileSetUp();
                profileOpen=true;
                page_tag.setEnabled(false);
                menu_profile_Card.setEnabled(false);
                scaleY(menupane,pxtodp(splash_cover.getHeight()),500,new AnticipateInterpolator());
                menu.animate().rotationBy(1080).withEndAction(null).setDuration(500).setInterpolator(new DecelerateInterpolator()).start();
                new Handler().postDelayed(() -> {
                    menu.setImageDrawable(getDrawable(R.drawable.back));
                    menu.setPadding(dptopx(13),dptopx(13),dptopx(13),dptopx(13));
                    done.setImageDrawable(getDrawable(R.drawable.tick));
                    done.setEnabled(false);
                    page_tag.setText(R.string.profile);
                    AlphaAnimation anims = new AlphaAnimation(0,1);anims.setDuration(300);
                    menu_profile_edit.setVisibility(View.VISIBLE);menu_profile_edit.startAnimation(anims);
                },200);
                new Handler().postDelayed(() -> {
                    page_tag.setEnabled(true);
                    menu_profile_Card.setEnabled(true);
                    },500);
            }
        });

        menu_profile_edit=findViewById(R.id.menu_profile_edit);
        AlphaAnimation anims = new AlphaAnimation(1,0);anims.setDuration(0);menu_profile_edit.startAnimation(anims);
        server=findViewById(R.id.server);
        server.addSwitchObserver((switchView, isChecked) -> {
            home.setAdapter(null);
            listRefresh();
            backoverlay.performClick();
        });

        dp_cover=findViewById(R.id.dp_cover);
        dp_cover.setOnClickListener(view -> {
            int cx=screenSize.x/2;
            int cy=screenSize.y-((int)(click.getY()));
            galaryOn=true;
            animator = ViewAnimationUtils.createCircularReveal(galary,cx,cy,0,(float) diagonal);
            animator.setInterpolator(new AccelerateInterpolator());animator.setDuration(300);galary.setVisibility(View.VISIBLE);
            galary.startAnimation(AnimationUtils.loadAnimation(HomeActivity.this, R.anim.fade_out));
            animator.start();
            Intent intent = new Intent();
            intent.setType("image/*");intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 2);
            overridePendingTransition(R.anim.fade_in,0);
            vibrate(35);
            cameraView.stop();
        });

        gender=findViewById(R.id.gender);
        gender.addSwitchObserver((switchView, isChecked) -> {
            if(isChecked){ gender_tag.setText(R.string.male); }
            else{ gender_tag.setText(R.string.female); }
        });

        gender_tag=findViewById(R.id.gender_tag);
        gender_tag.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/above.ttf"));

        f_name=findViewById(R.id.f_name);
        f_name.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        l_name=findViewById(R.id.l_name);
        l_name.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        dob=findViewById(R.id.dob);
        dob.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        aadhaar=findViewById(R.id.aadhaar);
        aadhaar.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        dob_chooser=findViewById(R.id.dob_chooser);
        dob_chooser.setOnClickListener(v -> {
            vibrate(20);
            DatePickerDialog dd = new DatePickerDialog(HomeActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        try {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy",Locale.US);
                            String dateInString = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                            Date date = formatter.parse(dateInString);
                            dob.setText(formatter.format(date));
                        } catch (Exception ignored) {}
                    }, 2000,  Calendar.getInstance().get(Calendar.MONTH),  Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            dd.show();
        });

        click_pane=findViewById(R.id.click_pane);
        click_pane.getLayoutParams().height = dptopx(140) + getHeightStatusNav(1);

        cameraView=findViewById(R.id.cam);
        camera_pane=findViewById(R.id.camera_pane);
        loading_profile= findViewById(R.id.loading_profile);
        loading_profile.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.progress), PorterDuff.Mode.MULTIPLY);
        profile=findViewById(R.id.profile);
        profile.setOnClickListener(v -> {
            setLightTheme(false,false);
            if(profile_lp) {profile_lp=false;}
            else
            {
                vibrate(20);
                camera_pane.setVisibility(View.VISIBLE);
                permission_camera.setVisibility(View.VISIBLE);
                camOn=true;
                final Animator animator = ViewAnimationUtils.createCircularReveal(camera_pane,dptopx(92),dptopx(248),profile.getWidth()/2, (float)diagonal);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());animator.setDuration(500);animator.start();
                if (checkPerm()) {
                    permission_camera.setVisibility(View.GONE);if(!cameraView.isCameraOpened()){cameraView.start();
                        cameraListener();
                    }
                }
                new Handler().postDelayed(() -> {
                    click.setVisibility(View.VISIBLE);
                    click.startAnimation(AnimationUtils.loadAnimation(HomeActivity.this, R.anim.click_grow));
                },500);
                if(checkPerm())
                {
                    new Handler().postDelayed(() -> {
                        ToolTip.Builder builder = new ToolTip.Builder(HomeActivity.this, click,camera_pane, getString(R.string.open_galary), ToolTip.POSITION_ABOVE);
                        builder.setBackgroundColor(getResources().getColor(R.color.profile));
                        builder.setTextColor(getResources().getColor(R.color.gender_back));
                        builder.setGravity(ToolTip.GRAVITY_CENTER);
                        builder.setTextSize(15);
                        toolTip.show(builder.build());
                    },1300);
                    new Handler().postDelayed(() -> toolTip.findAndDismiss(click),4000);
                }
            }
        });
        profile.setOnLongClickListener(view -> {
            vibrate(35);
            return false;
        });

        options=new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridColumnCount(0);
        options.setCropGridRowCount(0);
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.colorAccent));

        profile_url=new File(new ContextWrapper(getApplicationContext()).getDir("imageDir", Context.MODE_PRIVATE),"profile.jpg").getAbsolutePath();

        click=findViewById(R.id.click);
        permission_camera=findViewById(R.id.permission_camera);
        allow_camera =findViewById(R.id.allow_camera);
        allow_camera.setOnClickListener(view -> {
            vibrate(20);
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        });

        galary=findViewById(R.id.galary);
        flash=findViewById(R.id.flash);
        flash.setOnClickListener(view -> {
            vibrate(20);
            if(cameraView.getFlash()!=CameraView.FLASH_ON) {
                cameraView.setFlash(CameraView.FLASH_ON);
                flash.setImageResource(R.drawable.flash_on);
            }
            else {
                cameraView.setFlash(CameraView.FLASH_OFF);
                flash.setImageResource(R.drawable.flash_off);
            }
            Toast.makeText(HomeActivity.this, cameraView.getFlash()+"", Toast.LENGTH_SHORT).show();
        });
        camera_flip=findViewById(R.id.camera_flip);
        camera_flip.setOnClickListener(view -> {
            vibrate(20);
            cameraView.switchCamera();
        });
        click=findViewById(R.id.click);
        click.setOnClickListener(view -> {
            if(cameraView.isCameraOpened()){
                cameraView.takePicture();
            }
        });
        click.setOnLongClickListener(v -> {
            int cx=screenSize.x/2;
            int cy=screenSize.y-((int)(click.getY()));
            galaryOn=true;
            animator = ViewAnimationUtils.createCircularReveal(galary,cx,cy,0,(float) diagonal);
            animator.setInterpolator(new AccelerateInterpolator());animator.setDuration(300);galary.setVisibility(View.VISIBLE);
            galary.startAnimation(AnimationUtils.loadAnimation(HomeActivity.this, R.anim.fade_out));
            animator.start();
            Intent intent = new Intent();
            intent.setType("image/*");intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            overridePendingTransition(R.anim.fade_in,0);
            vibrate(35);
            cameraView.stop();
            return false;
        });

        done=findViewById(R.id.done);
        done.setOnClickListener(v -> {
            gclient.signOut();
            user_edit.clear();user_edit.apply();
            crack_edit.clear();crack_edit.apply();
            if(sesMODE == 2) {app_edit.clear();app_edit.apply();}
            new Thread(() -> Glide.get(HomeActivity.this).clearDiskCache()).start();
            Intent home=new Intent(HomeActivity.this, LoginActivity.class);
            HomeActivity.this.startActivity(home);
            finish();
            HomeActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
        refresh = findViewById(R.id.refresh);
        refresh.setProgressViewOffset(true,dptopx(50),dptopx(100));
        refresh.setNestedScrollingEnabled(true);
        refresh.setSlingshotDistance(dptopx(150));
        refresh.setOnRefreshListener(() -> {
            if(isOnline()){crackAdapter.current.navtrigger.performClick(); getCrackList(false);}
            else {Toast.makeText(HomeActivity.this, R.string.unreachable, Toast.LENGTH_SHORT).show();refresh.setRefreshing(false);}
        });

        home = findViewById(R.id.home);
        home.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        home.addItemDecoration(new GridSpacingItemDecoration(1,dptopx(10),true));
        home.setItemAnimator(new DefaultItemAnimator());

        sheet=findViewById(R.id.sheet);
        sheet_pane=findViewById(R.id.sheet_pane);
        sheet_pane.setPadding(dptopx(20),dptopx(20),dptopx(20),dptopx(20)+getHeightStatusNav(1));
        sheet_title=findViewById(R.id.sheet_title);
        sheet_title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2_bold.otf"));
        sheet_msg=findViewById(R.id.sheet_msg);
        sheet_msg.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));
        sheet_action=findViewById(R.id.sheet_action);
        sheet_action.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));
        sheet_action.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sheet_action.setBackgroundResource(R.drawable.signin_pressed);sheet_action.setTextColor(Color.parseColor("#ffffff"));
                    break;
                case MotionEvent.ACTION_UP:
                    sheet_action.setBackgroundResource(R.drawable.signin);sheet_action.setTextColor(getResources().getColor(R.color.colorAccent));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps")));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    vibrate(20);
                    break;
            }
            return true;
        });

        user = getSharedPreferences("user", MODE_PRIVATE);
        user_edit = user.edit();
        crack = getSharedPreferences("crack", MODE_PRIVATE);
        crack_edit = crack.edit();
        app = getSharedPreferences("app", MODE_PRIVATE);
        app_edit = app.edit();
        connect();
    }
    public void connect(){
        if(isOnline())
        {
            boolean cacheAvail = crack.getString("list", null) != null,
                    validApp = app.getBoolean("valid", true);
            if(cacheAvail && validApp){
                splash(true);
            }
            else appNameSplash.animateText(getString(R.string.resources));
            Log.i("backend_call", "Connecting");
            try{
                postBody = new FormBody.Builder()
                        .add("device",new CryptLib().encryptPlainTextWithRandomIV(android.os.Build.MODEL,"sanrakshak"))
                        .add("versionCode",new CryptLib().encryptPlainTextWithRandomIV(String.valueOf(BuildConfig.VERSION_CODE),"sanrakshak"))
                        .add("versionName",new CryptLib().encryptPlainTextWithRandomIV(BuildConfig.VERSION_NAME,"sanrakshak"))
                        .build();
            }
            catch (Exception e){Log.e("encrypt","Error while encryption");}
            Request request = new Request.Builder().url("https://sanrakshak.herokuapp.com/connect").post(postBody).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("backend_call", "Connection Failed - "+e);
                    call.cancel();
                    new Handler(Looper.getMainLooper()).post(() ->splash(false));
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            String[] resmsg = (Objects.requireNonNull(response.body()).string()).split("<->");
                            sesMODE = Integer.parseInt(resmsg[0]);
                            Log.i("backend_call","Server Response => "+response.message());
                            if(response.code()==503) splash(false);
                            else if(sesMODE==0){
                                //Application Rejected
                                app_edit.putBoolean("valid", false);
                                app_edit.apply();
                                if(cacheAvail && validApp){
                                    TapTargetView.showFor(HomeActivity.this,
                                            showTap(crackAdapter.current.date,false,false, 45, R.drawable.close_bold,
                                                    "We have a bad news",
                                                    "This build of sanrakshak is no longer valid \nGet a valid build or contact support \nGoodBye!!"),
                                            new TapTargetView.Listener() {
                                                @Override
                                                public void onTargetClick(TapTargetView view) {
                                                    super.onTargetClick(view);
                                                    new Handler().postDelayed(() -> finishAndRemoveTask(), 1000);
                                                }
                                            });
                                }
                                else
                                new Handler().postDelayed(() -> {
                                    proSplash.setVisibility(View.GONE);
                                    appNameSplash.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                                    appNameSplash.animateText("WE HAVE A BAD NEWS");
                                    new Handler().postDelayed(() -> {
                                        appNameSplash.animateText("THIS BUILD OF SANRAKSHAK");
                                        new Handler().postDelayed(() -> {
                                            appNameSplash.animateText("IS NO LONGER VALID");
                                            new Handler().postDelayed(() -> {
                                                appNameSplash.animateText("GET A VALID BUILD");
                                                new Handler().postDelayed(() -> {
                                                    appNameSplash.animateText("OR CONTACT SUPPORT");
                                                    new Handler().postDelayed(() -> {
                                                        appNameSplash.animateText("GOODBYE");
                                                        new Handler().postDelayed(() -> finishAndRemoveTask(), 3000);
                                                    }, 2000);
                                                }, 2000);
                                            }, 2000);
                                        }, 2000);
                                    }, 2000);
                                }, 2000);
                            }
                            else if(!(cacheAvail && validApp)){
                                app_edit.putBoolean("valid", true);
                                app_edit.apply();
                                cacheData(true);
                            }
                        } catch (IOException e) { e.printStackTrace(); }
                    });
                }
            });
        }
        else if(!app.getBoolean("valid", true)){
            new Handler().postDelayed(() -> {
                proSplash.setVisibility(View.GONE);
                appNameSplash.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                appNameSplash.animateText("WE HAVE A BAD NEWS");
                new Handler().postDelayed(() -> {
                    appNameSplash.animateText("THIS BUILD OF SANRAKSHAK");
                    new Handler().postDelayed(() -> {
                        appNameSplash.animateText("IS NO LONGER VALID");
                        new Handler().postDelayed(() -> {
                            appNameSplash.animateText("GET A VALID BUILD");
                            new Handler().postDelayed(() -> {
                                appNameSplash.animateText("OR CONTACT SUPPORT");
                                new Handler().postDelayed(() -> {
                                    appNameSplash.animateText("GOODBYE");
                                    new Handler().postDelayed(() -> finishAndRemoveTask(), 3000);
                                }, 2000);
                            }, 2000);
                        }, 2000);
                    }, 2000);
                }, 2000);
            }, 2000);
        }
        else splash(false);
    }
    public void cacheData(boolean splash){
        try{
            String enc=new CryptLib().encryptPlainTextWithRandomIV(user.getString("email", "ritik.space@gmail.com"),"sanrakshak");
            postBody = new FormBody.Builder().add("email",enc).build();
        }
        catch (Exception e){Log.e("encrypt","Error while encryption");}
        Request request = new Request.Builder().url("https://sanrakshak.herokuapp.com/getprofile").post(postBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("lora", "Failed - "+e);
                call.cancel();
                new Handler(Looper.getMainLooper()).post(() -> {
                    refresh.setRefreshing(false);
                    if(splash)splash(false);
                    Toast.makeText(HomeActivity.this, "AWS "+getString(R.string.unreachable), Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if(response.code()==503) {
                    Log.i("lora", "Failed - "+response);
                    call.cancel();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        refresh.setRefreshing(false);
                        if(splash)splash(false);
                        Toast.makeText(HomeActivity.this, "LoRa "+getString(R.string.unreachable), Toast.LENGTH_SHORT).show();
                    });
                }
                else if (response.isSuccessful()){
                    try {
                        JSONArray postsArray = new JSONArray(Objects.requireNonNull(response.body()).string());
                        for (int i = 0; i < postsArray.length(); i++) {
                            JSONObject pO = postsArray.getJSONObject(i);
                            user_edit = getSharedPreferences("user", MODE_PRIVATE).edit();
                            user_edit.putString("fname", pO.optString("fname"));
                            user_edit.putString("lname",pO.optString("lname"));
                            user_edit.putString("gender", pO.optString("gender"));
                            user_edit.putString("dob", pO.optString("dob"));
                            user_edit.putString("aadhaar", pO.optString("aadhaar"));
                            user_edit.putString("profile",pO.optString("profile"));
                            user_edit.putString("cover",pO.optString("cover"));
                            user_edit.apply();
                        }
                        new Handler(Looper.getMainLooper()).post(() -> profileSetUp());
                    }
                    catch (JSONException e) {
                        Log.w("error", e.toString());
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        getCrackList(splash);
                    });
                }
            }
        });
    }
    public void profileSetUp(){

        Log.i("errorProfile", user.getString("profile",null));

        Glide.with(home).load(user.getString("profile",null)).apply(new RequestOptions().centerCrop()).into(menu_profile);
        Glide.with(home).load(user.getString("profile",null)).apply(new RequestOptions().centerCrop()).into(profile);
        Glide.with(home).load(user.getString("cover",null)).apply(new RequestOptions().centerCrop()).into(menu_cover);
        Glide.with(home).load(user.getString("cover",null)).apply(new RequestOptions().centerCrop()).into(dp_cover);
        menu_fname.setText(user.getString("fname",""));
        menu_lname.setText(user.getString("lname",""));
        menu_email.setText(user.getString("email",""));
        f_name.setText(user.getString("fname",""));
        l_name.setText(user.getString("lname",""));
        dob.setText(user.getString("dob",""));
        aadhaar.setText(user.getString("aadhaar",""));
        if(Objects.requireNonNull(user.getString("gender", "")).toLowerCase().equals("female")){ gender.performClick(); }
    }
    public void getCrackList(boolean splash){
        if(server.isChecked()){
            Request request = new Request.Builder().url("http://168.87.87.213:8080/davc/m2m/HPE_IoT/70b3d57ed00130d6/default?ty=4&lim=5").get()
                    .addHeader("Authorization","Basic Qzk3MUQwMUMyLTIwNmVlNDQ0OlRlc3RAMTIz")
                    .addHeader("Content-Type","application/vnd.onem2m-res+json;ty=4")
                    .addHeader("X-M2M-Origin","C971D01C2-206ee444")
                    .addHeader("X-M2M-RI","9900001")
                    .addHeader("Accept","application/vnd.onem2m-res+json;")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("lora", "Failed - "+e);
                    call.cancel();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        refresh.setRefreshing(false);
                        if(splash)splash(false);
                        Toast.makeText(HomeActivity.this, "LoRa "+getString(R.string.unreachable), Toast.LENGTH_SHORT).show();
                    });
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    if(response.code()==503) {
                        Log.i("lora", "Failed - "+response);
                        call.cancel();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            refresh.setRefreshing(false);
                            if(splash)splash(false);
                            Toast.makeText(HomeActivity.this, "LoRa "+getString(R.string.unreachable), Toast.LENGTH_SHORT).show();
                        });
                    }
                    else if (response.isSuccessful()){
                        try {
                            cracks = new ArrayList<>();
                            JSONArray postsArray = new JSONArray(Objects.requireNonNull(response.body()).string());
                            for (int i = 0; i < postsArray.length(); i++) {
                                JSONObject postsObj = postsArray.getJSONObject(i);
                                postsObj = new JSONObject(postsObj.optString("m2m:cin"));
                                postsObj = new JSONObject(postsObj.optString("con"));
                                postsObj = new JSONObject(postsObj.optString("payloads_ul"));
                                String[] data = (new String(Base64.decode(postsObj.optString("dataFrame"), Base64.DEFAULT), "UTF-8")).split("-");
                                double lat=Double.parseDouble(data[1]);
                                double lng=Double.parseDouble(data[2]);
                                if(lat!=0 && lng!=0){
                                    cracks.add(new Cracks(""+lat,""+lng,getPlaceName(lat,lng,0),getPlaceName(lat,lng,1),
                                            data[3],data[4],getMapURL(lat,lng,16,data_div.getWidth())));
                                }
                                else{
                                    lat=12.8795158;
                                    lng=80.0817086;
                                    cracks.add(new Cracks(""+lat,""+lng,getPlaceName(lat,lng,0),getPlaceName(lat,lng,1),
                                            "0.65","15/12/2018",getMapURL(lat,lng,16,data_div.getWidth())));
                                }
                            }

                            crack_edit.putString("list", new Gson().toJson(cracks));
                            crack_edit.apply();
                            new Handler(Looper.getMainLooper()).post(() -> {
                                home.setAdapter(null);
                                crackAdapter = new CrackAdapter(HomeActivity.this,cracks);
                                home.setAdapter(crackAdapter);
                                refresh.setRefreshing(false);
                                if(splash) splash(false);
                            });
                        }
                        catch (JSONException e) { }
                    }
                    else{
                        new Handler(Looper.getMainLooper()).post(() -> loadCache());
                    }
                }
            });
        }
        else{
            try{
                String enc=new CryptLib().encryptPlainTextWithRandomIV(user.getString("email", "ritik.space@gmail.com"),"sanrakshak");
                postBody = new FormBody.Builder().add("email",enc).build();
            }
            catch (Exception e){Log.e("encrypt","Error while encryption");}
            Request request = new Request.Builder().url("https://sanrakshak.herokuapp.com/getcrack").post(postBody).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("backend_call", "Failed - "+e);
                    call.cancel();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(HomeActivity.this, R.string.unreachable, Toast.LENGTH_SHORT).show();
                        refresh.setRefreshing(false);
                    });
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    if(response.code()==503) {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(HomeActivity.this, R.string.unreachable, Toast.LENGTH_SHORT).show();
                            refresh.setRefreshing(false);
                        });
                    }
                    else if (response.isSuccessful()){
                        try {
                            JSONArray postsArray = new JSONArray(Objects.requireNonNull(response.body()).string());
                            Log.i("encrypt111", postsArray.toString(4));
                            cracks = new ArrayList<>();
                            for (int i = 0; i < postsArray.length(); i++) {
                                JSONObject obj = postsArray.getJSONObject(i);
                                double lat=Double.parseDouble(obj.getString("x"));
                                double lng=Double.parseDouble(obj.getString("y"));
                                //data_div.getWidth()*7/17
                                cracks.add(new Cracks(""+lat,""+lng,getPlaceName(lat,lng,0),getPlaceName(lat,lng,1),
                                        obj.getString("intensity"),obj.getString("date"),getMapURL(lat,lng,16,data_div.getWidth())));
                            }
                            crack_edit.putString("list", new Gson().toJson(cracks));
                            crack_edit.apply();
                            new Handler(Looper.getMainLooper()).post(() -> {
                                home.setAdapter(null);
                                crackAdapter = new CrackAdapter(HomeActivity.this,cracks);
                                home.setAdapter(crackAdapter);
                                refresh.setRefreshing(false);
                                if(splash)splash(false);
                            });
                        }
                        catch (JSONException e) {
                            Log.w("error", e.toString());
                        }
                    }
                    else{
                        new Handler(Looper.getMainLooper()).post(() -> loadCache());
                    }
                }
            });
        }
    }
    public void splash(final boolean loadOnline){
        loadCache();
        appNameSplash.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/vdub.ttf"));
        appNameSplash.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        appNameSplash.animateText(getString(R.string.app_name));
        proSplash.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            splash_cover.setVisibility(View.GONE);
            logo_div.setVisibility(View.VISIBLE);

            Runnable runn = () -> {
                float CurrentX = ico_splash.getX();
                float CurrentY = ico_splash.getY();
                float FinalX = 0;
                float FinalY = 35;
                Path path = new Path();
                path.moveTo(CurrentX, CurrentY);
                path.quadTo(CurrentX*4/3, (CurrentY+FinalY)/4, FinalX, FinalY);

                startAnim = ObjectAnimator.ofFloat(ico_splash, View.X, View.Y, path);
                startAnim.setDuration(700);
                startAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                startAnim.start();                   };
            runn.run();


            new Handler().postDelayed(() -> {
                ico_splash.animate().scaleX(0f).scaleY(0f).setDuration(800).start();
                AlphaAnimation anims = new AlphaAnimation(1,0);anims.setDuration(700);anims.setFillAfter(true);
                ico_splash.startAnimation(anims);appNameSplash.startAnimation(anims);
            },10);
            new Handler().postDelayed(() -> {

                Runnable runnable = () -> {
                    AlphaAnimation anims = new AlphaAnimation(0,1);anims.setDuration(1000);
                    actionbar.setVisibility(View.VISIBLE);actionbar.startAnimation(anims);
                    page_tag.setEnabled(false);
                    menu.animate().rotationBy(1080).withEndAction(null).setDuration(1100).setInterpolator(new AccelerateDecelerateInterpolator()).start();
                    new Handler().postDelayed(() -> page_tag.setEnabled(true),1100);
                };
                runnable.run();

                AlphaAnimation anims = new AlphaAnimation(1,0);anims.setDuration(1000);anims.setFillAfter(true);
                anims.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {
                        if(app.getBoolean("firstBoot", false)){
                            logo_div_fade.setClickable(false);
                            logo_div_fade.setFocusable(false);
                        }
                        else
                        new Handler().postDelayed(() -> TapTargetView.showFor(HomeActivity.this,
                                showTap(crackAdapter.current.date,false,false, 45, R.drawable.tick_mono,
                                        "Glad to see you here!",
                                        "Here is the list of all the cracks \ndetected by Sanrakshak."),
                                new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        TapTargetView.showFor(HomeActivity.this,
                                                showTap(crackAdapter.current.preview,true,true, 60, R.drawable.tick_bold,
                                                        "Map Preview",
                                                        "This is Google Maps preview of the location. \nTap for more details."),
                                                new TapTargetView.Listener() {
                                                    @Override
                                                    public void onTargetClick(TapTargetView view) {
                                                        super.onTargetClick(view);
                                                        new Handler().postDelayed(() -> {
                                                            crackAdapter.current.cardItem.performClick();
                                                            new Handler().postDelayed(() -> TapTargetView.showFor(HomeActivity.this,
                                                                    showTap(crackAdapter.current.intensity,true,true, 55, R.drawable.tick_bold,
                                                                            "Crack Intensity",
                                                                            "Intensity of crack on the scale of 100 \nMore the intensity, worse the crack"),
                                                                    new TapTargetView.Listener() {
                                                                        @Override
                                                                        public void onTargetClick(TapTargetView view) {
                                                                            super.onTargetClick(view);
                                                                            TapTargetView.showFor(HomeActivity.this,
                                                                                    showTap(crackAdapter.current.time,true,true, 55, R.drawable.tick_bold,
                                                                                            "Time Left",
                                                                                            "Here is the no. of days allotted to a crack. \nTo make sure it get fixed on time"),
                                                                                    new TapTargetView.Listener() {
                                                                                        @Override
                                                                                        public void onTargetClick(TapTargetView view) {
                                                                                            super.onTargetClick(view);
                                                                                            TapTargetView.showFor(HomeActivity.this,
                                                                                                    showTap(crackAdapter.current.locate,false,false, 40, R.drawable.map,
                                                                                                            "Locate on Google Maps",
                                                                                                            "Locate exact location of the crack \non Google Maps"),
                                                                                                    new TapTargetView.Listener() {
                                                                                                        @Override
                                                                                                        public void onTargetClick(TapTargetView view) {
                                                                                                            super.onTargetClick(view);
                                                                                                            TapTargetView.showFor(HomeActivity.this,
                                                                                                                    showTap(crackAdapter.current.navigate,false,false, 40, R.drawable.navigation,
                                                                                                                            "Navigate on Google Maps",
                                                                                                                            "Navigate to location of the crack \nUsing Google Maps Navigation"),
                                                                                                                    new TapTargetView.Listener() {
                                                                                                                        @Override
                                                                                                                        public void onTargetClick(TapTargetView view) {
                                                                                                                            super.onTargetClick(view);
                                                                                                                            crackAdapter.current.navtrigger.performClick();
                                                                                                                            TapTargetView.showFor(HomeActivity.this,
                                                                                                                                    showTap(menu,false,false, 40, R.drawable.menu,
                                                                                                                                            "Menu",
                                                                                                                                            "Checkout you profile here. \nMore features will be added later."),
                                                                                                                                    new TapTargetView.Listener() {
                                                                                                                                        @Override
                                                                                                                                        public void onTargetClick(TapTargetView view) {
                                                                                                                                            super.onTargetClick(view);
                                                                                                                                            TapTargetView.showFor(HomeActivity.this,
                                                                                                                                                    showTap(done,false,false, 40, R.drawable.logout,
                                                                                                                                                            "Logout",
                                                                                                                                                            "Want to logout or switch account? \nTap here and there you go."),
                                                                                                                                                    new TapTargetView.Listener() {
                                                                                                                                                        @Override
                                                                                                                                                        public void onTargetClick(TapTargetView view) {
                                                                                                                                                            super.onTargetClick(view);
                                                                                                                                                            TapTargetView.showFor(HomeActivity.this,
                                                                                                                                                                    showTap(crackAdapter.current.date,false,false, 45, R.drawable.heart,
                                                                                                                                                                            "Tour ends here",
                                                                                                                                                                            "Our Sanrakshak tour ends here \nHave a good day!"),
                                                                                                                                                                    new TapTargetView.Listener() {
                                                                                                                                                                        @Override
                                                                                                                                                                        public void onTargetClick(TapTargetView view) {
                                                                                                                                                                            super.onTargetClick(view);
                                                                                                                                                                            app_edit.putBoolean("firstBoot", true);
                                                                                                                                                                            app_edit.apply();
                                                                                                                                                                            logo_div_fade.setClickable(false);
                                                                                                                                                                            logo_div_fade.setFocusable(false);
                                                                                                                                                                        }
                                                                                                                                                                    });
                                                                                                                                                        }
                                                                                                                                                    });
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                        }
                                                                                                                    });
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }),1000);
                                                        },200);
                                                    }
                                                });
                                    }
                                }), 200);
                    }
                    @Override public void onAnimationRepeat(Animation animation) { }
                });
                logo_div_fade.startAnimation(anims);
                new Handler().postDelayed(() -> {
                    if(loadOnline)listRefresh();
                },500);
                setLightTheme(true,true);
            },400);
        },1000);
    }
    public TapTarget showTap(View view, boolean target, boolean targetIcon, int targetRadius, int icon, String title, String des){
        return TapTarget.forView(view, title, des)
                .outerCircleColor(R.color.home_tap)
                .outerCircleAlpha(0.95f)
                .titleTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"))
                .textTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"))
                .targetCircleColor(R.color.colorPrimary)
                .titleTextSize(22)
                .titleTextColor(R.color.colorPrimary)
                .descriptionTextSize(14)
                .descriptionTextColor(R.color.colorPrimary)
                .textColor(R.color.colorPrimary)
                .dimColor(R.color.black)
                .drawShadow(true)
                .cancelable(false)
                .tintTarget(true)
                .transparentTarget(target)
                .icon(Objects.requireNonNull(getDrawable(icon)),targetIcon)
                .targetRadius(targetRadius);
    }
    public void loadCache(){
        home.setAdapter(null);
        cracks=new Gson().fromJson(crack.getString("list", null), new TypeToken<ArrayList<Cracks>>() {}.getType());
        crackAdapter = new CrackAdapter(HomeActivity.this,cracks);
        home.setAdapter(crackAdapter);
        refresh.setRefreshing(false);
    }
    public String getPlaceName(double latitude, double longitude, int token){
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(null!=listAddresses&&listAddresses.size()>0){
                Address obj = listAddresses.get(0);
                Log.i("backend_call", obj.getPremises()+" - "+obj.getAddressLine(0));
                if(token==0){
                    String name=obj.getAddressLine(0);
                    String[] split = name.split(", ");
                    int index=getIndex(obj.getLocality(),split)-1;
                    index = (index<=0)?0:index;
                    name=split[index];
                    name=(name.toLowerCase().contains("unnamed road"))?split[index+1]:name;
                    return name;
                }
                else if(token==1){return  obj.getLocality()+", "+obj.getAdminArea();}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    public String getMapURL(double lat, double lng, int zoom, int size){
        try{
            String APIKey="3dd4wgt62K2sMwn/5rzbKmiZOMjvh7s8FvYkmezWkmNW7YzOu99TvSTcyJIBOzl457hHkyulDbBaKWdJccGfc4GajVR4gycz/iFwgBpiHi1dbbHaF9QnqKxO2jh9aaCfUKaFapjLuDoSLfEcZQgEAA==";
            APIKey = new CryptLib().decryptCipherTextWithRandomIV(new CryptLib().decryptCipherTextWithRandomIV(APIKey,"sanrakshak"),"sanrakshak");
            String keyString="46HSOIkf1Y8T9zoxbzIjmjfD+x2T7pe4Gv0MJKDJDqNJxuVsNLYuMzpQJGLUj4pAUv3q7r8PJyuVS/YTA8iktOo1fRG0iJC6oZkuhvNtjDEAQBmYOii9z4tlSsiAGmUY";
            keyString = new CryptLib().decryptCipherTextWithRandomIV(new CryptLib().decryptCipherTextWithRandomIV(keyString,"sanrakshak"),"sanrakshak");

            String resource="https://maps.googleapis.com/maps/api/staticmap?center="+lat+","+lng+"&zoom="+zoom+"&size="+size+"x"+size+"&maptype=terrain&key="+APIKey;
            URL url=new URL(resource);

            keyString = (keyString.replace('-', '+')).replace('_', '/');
            byte[] key = android.util.Base64.decode(keyString, android.util.Base64.DEFAULT);

            resource = url.getPath() + '?' + url.getQuery();
            SecretKeySpec sha1Key = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(sha1Key);
            byte[] sigBytes = mac.doFinal(resource.getBytes());
            String signature = android.util.Base64.encodeToString(sigBytes,android.util.Base64.DEFAULT);
            signature = signature.replace('+', '-');
            signature = signature.replace('/', '_');
            return "https://maps.googleapis.com" + resource + "&signature=" + signature;
        }
        catch (Exception e){Log.e("signature","Error occured - "+e);}
        return "";
    }
    public void cameraListener(){
        cameraView.setOnFocusLockedListener(() -> {
        });
        cameraView.setOnPictureTakenListener((result, rotationDegrees) -> {
            Log.e("Camera", "onPictureTaken: " );
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            result= Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            vibrate(20);
            profile_path = MediaStore.Images.Media.insertImage(HomeActivity.this.getContentResolver(), result, "Title", null);
            UCrop.of(Uri.parse(profile_path),Uri.parse(profile_url)).withOptions(options).withAspectRatio(1,1)
                    .withMaxResultSize(maxWidth, maxHeight).start(HomeActivity.this);
        });
        cameraView.setOnTurnCameraFailListener(e ->
                Toast.makeText(HomeActivity.this, "Switch Camera Failed. Does you device has a front camera?",
                        Toast.LENGTH_SHORT).show());
        cameraView.setOnCameraErrorListener(e -> Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    public void closeCam()
    {
        Animation anim = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.click_shrink);click.startAnimation(anim);
        animator = ViewAnimationUtils.createCircularReveal(camera_pane,dptopx(92),dptopx(248), (float) diagonal,profile.getWidth()/2);
        animator.setInterpolator(new DecelerateInterpolator());animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                camOn=false;
                cameraView.stop();
                click.setVisibility(View.GONE);
                setLightTheme(true,true);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                camera_pane.setVisibility(View.GONE);
                click.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        new Handler().postDelayed(() -> animator.start(),300);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPerm())
                    {
                        permission_camera.setVisibility(View.GONE);
                        new Handler().postDelayed(() -> {
                            ToolTip.Builder builder = new ToolTip.Builder(HomeActivity.this, click,camera_pane, getString(R.string.open_galary), ToolTip.POSITION_ABOVE);
                            builder.setBackgroundColor(getResources().getColor(R.color.profile));
                            builder.setTextColor(getResources().getColor(R.color.gender_back));
                            builder.setGravity(ToolTip.GRAVITY_CENTER);
                            builder.setTextSize(15);
                            toolTip.show(builder.build());
                            cameraView.setVisibility(View.GONE);
                            cameraView.setVisibility(View.VISIBLE);
                            if(!cameraView.isCameraOpened()){
                                cameraView.start();cameraListener();
                            }
                        },1300);
                        new Handler().postDelayed(() -> toolTip.findAndDismiss(click),4000);
                    }
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        if (requestCode == 1 && resultcode == RESULT_OK) {
            UCrop.of(Objects.requireNonNull(intent.getData()),Uri.parse(profile_url)).withOptions(options).withAspectRatio(1,1)
                    .withMaxResultSize(maxWidth, maxHeight).start(HomeActivity.this);
        }
        if (requestCode == 2 && resultcode == RESULT_OK) {
            Glide.with(home).load(intent.getData()).into(dp_cover);
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if(resultcode == RESULT_OK)
            {
                try {
                    final Uri resultUri = UCrop.getOutput(intent);
                    Bitmap bitmap= MediaStore.Images.Media.getBitmap(HomeActivity.this.getContentResolver(), resultUri);
                    profile.setImageBitmap(bitmap);dp_cover.setImageBitmap(bitmap);profile_dp=bitmap;isDP_added=true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }
                    getWindow().setStatusBarColor(Color.WHITE);
                    closeCam();
                    Log.i("Camera", "Delete File : "+new File(getRealPathFromURI(HomeActivity.this,Uri.parse(profile_path))).delete() );
                }
                catch (Exception ignored){}
            }
            else if (resultcode == UCrop.RESULT_ERROR) {
                Log.i("Camera", "Delete File : "+new File(getRealPathFromURI(HomeActivity.this,Uri.parse(profile_path))).delete() );
            }
        }
    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        try (Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null)) {
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }
    public int getIndex(String element, String[] arr){
        for(int i=0;i<arr.length;i++){
            if(arr[i].contains(element)){
                return i;
            }
        }
        return -1;
    }
    public void listRefresh(){
        refresh.post(() -> {
            if(isOnline()){cacheData(false);}
            else {Toast.makeText(HomeActivity.this, R.string.unreachable, Toast.LENGTH_SHORT).show();refresh.setRefreshing(false);}
            refresh.setRefreshing(true);
        });
    }
    public boolean checkPerm(){
        return (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
    public void scaleX(final View view,int x,int t, Interpolator interpolator)
    {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredWidth(),dptopx(x));anim.setInterpolator(interpolator);
        anim.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = (Integer) valueAnimator.getAnimatedValue();
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(t);anim.start();
    }
    public void scaleY(final View view,int y,int t, Interpolator interpolator)
    {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(),dptopx(y));anim.setInterpolator(interpolator);
        anim.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
            view.setLayoutParams(layoutParams);view.invalidate();
        });
        anim.setDuration(t);anim.start();
    }
    public int dptopx(float dp)
    {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    public int pxtodp(float px)
    {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    public void vibrate(int ms)
    {
        ((Vibrator) Objects.requireNonNull(this.getSystemService(Context.VIBRATOR_SERVICE))).vibrate(ms);
    }
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;
        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;
            if (includeEdge){
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
    public void showSheet(String title,String msg,String action,int size){
        sheet_title.setText(title);
        sheet_msg.setText(msg);
        sheet_action.setText(action);
        backoverlay.setVisibility(View.VISIBLE);
        scaleY(sheet,size,500,new OvershootInterpolator());
    }
    public int getHeightStatusNav(int viewid) {
        int result = 0;
        String view=(viewid==0)?"status_bar_height":"navigation_bar_height";
        int resourceId = getResources().getIdentifier(view, "dimen", "android");
        if (resourceId > 0) { result = getResources().getDimensionPixelSize(resourceId); }
        if(viewid==1){result = result* 5/8;}
        return result;
    }
    public void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
    public void setLightTheme(boolean status,boolean nav){
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        if(status && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        if(nav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        if(!status && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        if(!nav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }
}