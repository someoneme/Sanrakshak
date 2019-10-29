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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.cameraview.CameraView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.rm.rmswitch.RMSwitch;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.R.attr.maxHeight;
import static android.R.attr.maxWidth;

public class ProfileActivity extends AppCompatActivity {
    RelativeLayout logo_div,splash_cover,camera_pane,permission_camera,galary,click_pane,profile_menu_cov,form_panel;
    FrameLayout root_view;
    CardView data_div;
    ImageView dp_cover,ico_splash,done,camera_flip,click,flash,dob_chooser;
    Button allow_camera;
    Animation anim;
    TextView page_tag,gender_tag,appNameSplash;
    EditText f_name,l_name,dob,aadhaar;
    RMSwitch gender;
    Point screenSize;
    Animator animator;
    ObjectAnimator startAnim;
    CameraView cameraView;
    UCrop.Options options;
    CircularImageView profile;
    boolean profile_lp=false,camOn=false,galaryOn=false,isDP_added=false;
    String profile_url="",profile_path="";
    ProgressBar loading_profile;
    ToolTipsManager toolTip;
    Bitmap profile_dp=null;
    double diagonal;
    OkHttpClient client;
    RequestBody postBody;
    private StorageReference storageRef;
    float CurrentX,CurrentY;
    SharedPreferences.Editor user;
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
                Animation anim = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.click_grow);click.startAnimation(anim);
            },500);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        root_view=findViewById(R.id.root_view);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setLightTheme(true,true);

        screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        diagonal=Math.sqrt((screenSize.x*screenSize.x) + (screenSize.y*screenSize.y));
        splash_cover=findViewById(R.id.splash_cover);
        logo_div=findViewById(R.id.logo_div);
        data_div=findViewById(R.id.data_div);
        dp_cover=findViewById(R.id.dp_cover);
        form_panel=findViewById(R.id.form_panel);
        profile_menu_cov=findViewById(R.id.profile_menu_cov);
        toolTip = new ToolTipsManager();
        client = new OkHttpClient();

        gender_tag=findViewById(R.id.gender_tag);
        gender_tag.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/above.ttf"));

        gender=findViewById(R.id.gender);
        gender.addSwitchObserver((switchView, isChecked) -> {
            if(isChecked){
                gender_tag.setText(R.string.male);
            }
            else{
                gender_tag.setText(R.string.female);
            }
        });

        f_name=findViewById(R.id.f_name);
        f_name.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        l_name=findViewById(R.id.l_name);
        l_name.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        dob=findViewById(R.id.dob);
        dob.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        aadhaar=findViewById(R.id.aadhaar);
        aadhaar.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        page_tag=findViewById(R.id.page_tag);
        page_tag.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/exo2.ttf"));

        appNameSplash=findViewById(R.id.appNameSplash);
        appNameSplash.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/vdub.ttf"));
        setMargins(appNameSplash,0,0,0,dptopx(30) + getHeightStatusNav(1));

        ico_splash=findViewById(R.id.ico_splash);
        ico_splash.setScaleType(ImageView.ScaleType.CENTER);

        click_pane=findViewById(R.id.click_pane);
        click_pane.getLayoutParams().height = dptopx(140) + getHeightStatusNav(1);

        galary= findViewById(R.id.galary);

        camera_pane=findViewById(R.id.camera_pane);
        loading_profile= findViewById(R.id.loading_profile);
        loading_profile.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.progress), PorterDuff.Mode.MULTIPLY);

        done=findViewById(R.id.done);
        done.setOnClickListener(v -> createProfile(""));
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
                    click.startAnimation(AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.click_grow));
                },500);
                if(checkPerm())
                {
                    new Handler().postDelayed(() -> {
                        ToolTip.Builder builder = new ToolTip.Builder(ProfileActivity.this, click,camera_pane, getString(R.string.open_galary), ToolTip.POSITION_ABOVE);
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
            Toast.makeText(ProfileActivity.this, cameraView.getFlash()+"", Toast.LENGTH_SHORT).show();
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
            galary.startAnimation(AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.fade_out));
            animator.start();
            Intent intent = new Intent();
            intent.setType("image/*");intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            overridePendingTransition(R.anim.fade_in,0);
            vibrate(35);
            cameraView.stop();
            return false;
        });

        permission_camera=findViewById(R.id.permission_camera) ;
        allow_camera =findViewById(R.id.allow_camera);
        allow_camera.setOnClickListener(view -> {
            vibrate(20);
            ActivityCompat.requestPermissions(ProfileActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        });

        dob_chooser=findViewById(R.id.dob_chooser);
        dob_chooser.setOnClickListener(v -> {
            vibrate(20);
            DatePickerDialog dd = new DatePickerDialog(ProfileActivity.this,
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

        cameraView=findViewById(R.id.cam);
        account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null){
            f_name.setText(account.getGivenName());
            l_name.setText(account.getFamilyName());
            new Thread(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(Objects.requireNonNull(account.getPhotoUrl()).toString()).openConnection();
                    connection.setDoInput(true);connection.connect();
                    InputStream input = connection.getInputStream();
                    final Bitmap bmp = BitmapFactory.decodeStream(input);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        profile.setImageBitmap(bmp);
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        new Handler().postDelayed(() -> {
            // SignUp Animation

            splash_cover.setVisibility(View.GONE);
            logo_div.setVisibility(View.VISIBLE);

            CurrentX = ico_splash.getX();
            CurrentY = ico_splash.getY();
            float FinalX = -dptopx(25);
            float FinalY = getHeightStatusNav(0)-dptopx(30);
            Path path = new Path();
            path.moveTo(CurrentX, CurrentY);
            path.quadTo(CurrentX*4/3, (CurrentY+FinalY)/4, FinalX, FinalY);

            startAnim = ObjectAnimator.ofFloat(ico_splash, View.X, View.Y, path);
            startAnim.setDuration(800);
            startAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            startAnim.start();
            ico_splash.animate().scaleX(0.25f).scaleY(0.25f).setDuration(1000).start();
            new Handler().postDelayed(() ->
                    scaleY(data_div,pxtodp(splash_cover.getHeight()-getHeightStatusNav(0))-65,800,new AccelerateDecelerateInterpolator()),10);
            new Handler().postDelayed(() -> {
                AlphaAnimation anims = new AlphaAnimation(0,1);
                anims.setDuration(600);
                page_tag.setVisibility(View.VISIBLE);page_tag.startAnimation(anims);
                done.setVisibility(View.VISIBLE);done.startAnimation(anims);
            },500);

        },1500);
    }
    public void createProfile(String dp){
        if(dp.equals("")){
            float FinalX = CurrentX-dptopx(15);
            float FinalY = CurrentY-dptopx(70)-getHeightStatusNav(0);
            CurrentX = profile_menu_cov.getX();
            CurrentY = profile_menu_cov.getY();
            Path path = new Path();
            path.moveTo(CurrentX, CurrentY);
            path.quadTo(FinalX, (CurrentY+FinalY)/2, FinalX, FinalY);
            startAnim = ObjectAnimator.ofFloat(profile_menu_cov, View.X, View.Y, path);
            startAnim.setDuration(600);
            startAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            startAnim.start();

            data_div.setBackground(null);
            anim = new AlphaAnimation(1,0);
            anim.setDuration(500);anim.setFillAfter(true);
            data_div.setFocusable(false);
            form_panel.startAnimation(anim);
            gender.startAnimation(anim);
            gender_tag.startAnimation(anim);
            dp_cover.startAnimation(anim);
            ico_splash.startAnimation(anim);
            done.startAnimation(anim);
            page_tag.startAnimation(anim);
            new Handler().postDelayed(() -> {
                loading_profile.setVisibility(View.VISIBLE);
            },800);
        }

        if(isDP_added && dp.equals(""))
        {
            storageRef = FirebaseStorage.getInstance().getReference();
            storageRef = storageRef.child(getIntent().getStringExtra("email")+"/profile.jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            profile_dp=Bitmap.createScaledBitmap(profile_dp, screenSize.x, screenSize.x, false);
            profile_dp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            storageRef.putBytes(baos.toByteArray())
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return storageRef.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            createProfile(Objects.requireNonNull(uri).toString());
                            Log.i("upload", "Upload Success - "+uri);
                        }
                        else {
                            Log.i("upload", "Upload Failed - "+task);
                            loading_profile.setVisibility(View.INVISIBLE);
                        }
                    });
        }
        else {
            try {
                postBody = new FormBody.Builder()
                        .add("email",new CryptLib().encryptPlainTextWithRandomIV(getIntent().getStringExtra("email"),"sanrakshak"))
                        .add("fname",new CryptLib().encryptPlainTextWithRandomIV(f_name.getText().toString(),"sanrakshak"))
                        .add("lname",new CryptLib().encryptPlainTextWithRandomIV(l_name.getText().toString(),"sanrakshak"))
                        .add("gender",new CryptLib().encryptPlainTextWithRandomIV(gender_tag.getText().toString().toLowerCase(),"sanrakshak"))
                        .add("dob",new CryptLib().encryptPlainTextWithRandomIV(dob.getText().toString(),"sanrakshak"))
                        .add("aadhaar",new CryptLib().encryptPlainTextWithRandomIV(aadhaar.getText().toString(),"sanrakshak"))
                        .add("profile",new CryptLib().encryptPlainTextWithRandomIV(dp,"sanrakshak"))
                        .add("cover",new CryptLib().encryptPlainTextWithRandomIV(dp,"sanrakshak")).build();

            } catch (Exception e) { e.printStackTrace(); }
            Request request = new Request.Builder().url("https://sanrakshak.herokuapp.com/profile").post(postBody).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.i("backend_call", "Verification Failed - "+e);
                    loading_profile.setVisibility(View.INVISIBLE);
                    call.cancel();
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    if(Integer.parseInt(Objects.requireNonNull(response.body()).string())==1 && response.isSuccessful()){
                        new Handler(Looper.getMainLooper()).post(() -> {
                            user = getSharedPreferences("user", MODE_PRIVATE).edit();
                            user.putString("email",ProfileActivity.this.getIntent().getStringExtra("email"));
                            user.putString("fname", f_name.getText().toString());
                            user.putString("lname",l_name.getText().toString());
                            user.putString("gender",gender_tag.getText().toString());
                            user.putString("dob",dob.getText().toString());
                            user.putString("aadhaar", aadhaar.getText().toString());
                            user.putString("profile",dp);
                            user.putString("cover",dp);
                            user.apply();
                            new Handler().postDelayed(() -> {
                                loading_profile.setVisibility(View.INVISIBLE);
                                anim = new AlphaAnimation(1,0);
                                anim.setDuration(500);anim.setFillAfter(true);
                                profile_menu_cov.startAnimation(anim);
                                new Handler().postDelayed(() -> {
                                    Intent home=new Intent(ProfileActivity.this,HomeActivity.class);
                                    ProfileActivity.this.startActivity(home);
                                    ProfileActivity.this.overridePendingTransition(0,R.anim.fade_out);
                                    finish();},420);
                                },1000);
                        });
                    }
                    else{
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(ProfileActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                            loading_profile.setVisibility(View.INVISIBLE);
                        });
                    }
                }
            });
        }

    }
    public void cameraListener(){
        cameraView.setOnFocusLockedListener(() -> {
        });
        cameraView.setOnPictureTakenListener((result, rotationDegrees) -> {
            Log.e("Camera", "onPictureTaken: " +result.getByteCount());
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            result= Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            vibrate(20);
            UCrop.of(getImageUri(this,result),Uri.parse(profile_url)).withOptions(options).withAspectRatio(1,1)
                    .withMaxResultSize(maxWidth, maxHeight).start(ProfileActivity.this);
        });
        cameraView.setOnTurnCameraFailListener(e ->
                Toast.makeText(ProfileActivity.this, "Switch Camera Failed. Does you device has a front camera?",
                        Toast.LENGTH_SHORT).show());
        cameraView.setOnCameraErrorListener(e -> Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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

    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent intent) {
        super.onActivityResult(requestCode, resultcode, intent);
        if (requestCode == 1 && resultcode == RESULT_OK) {
            Log.e("Camera", "onPictureTaken: " + maxWidth+" - "+maxHeight+"\n"+profile_url);
            UCrop.of(Objects.requireNonNull(intent.getData()),Uri.parse(profile_url)).withOptions(options).withAspectRatio(1,1)
                    .withMaxResultSize(maxWidth, maxHeight).start(ProfileActivity.this);
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            if(resultcode == RESULT_OK)
            {
                try {
                    final Uri resultUri = UCrop.getOutput(intent);
                    Bitmap bitmap= MediaStore.Images.Media.getBitmap(ProfileActivity.this.getContentResolver(), resultUri);
                    profile.setImageBitmap(bitmap);dp_cover.setImageBitmap(bitmap);profile_dp=bitmap;isDP_added=true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }
                    getWindow().setStatusBarColor(Color.WHITE);
                    closeCam();
                    //new File(getRealPathFromURI(ProfileActivity.this,Uri.parse(profile_path))).delete();
                }
                catch (Exception ignored){}
            }
            else if (resultcode == UCrop.RESULT_ERROR) {
                //new File(getRealPathFromURI(ProfileActivity.this,Uri.parse(profile_path))).delete();
            }
        }
    }
    public void closeCam()
    {
        Animation anim = AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.click_shrink);click.startAnimation(anim);
        animator = ViewAnimationUtils.createCircularReveal(camera_pane,dptopx(92),dptopx(248), (float) diagonal,profile.getWidth()/2);
        animator.setInterpolator(new DecelerateInterpolator());animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                camOn=false;
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
    public String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        try (Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null)) {
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPerm())
                    {
                        permission_camera.setVisibility(View.GONE);
                        new Handler().postDelayed(() -> {
                            ToolTip.Builder builder = new ToolTip.Builder(ProfileActivity.this, click,camera_pane, getString(R.string.open_galary), ToolTip.POSITION_ABOVE);
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
    public boolean checkPerm(){
        return (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
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
