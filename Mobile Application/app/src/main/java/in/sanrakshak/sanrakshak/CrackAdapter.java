package in.sanrakshak.sanrakshak;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;

public class CrackAdapter extends RecyclerView.Adapter<CrackAdapter.MyViewHolder> {
    private List<Cracks> cracks;
    private HomeActivity home;
    private int cardHeight=0, imgHeight=0, expandMain=0;
    MyViewHolder current;
    private Interpolator interpolator;
    private int speed;
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name,city,date,name_details,city_details,date_details,intensity_tag,time_tag;
        ImageView preview,locate,navigate;
        RelativeLayout navtrigger,cardItem,cardthumb;
        ProgressBar glidepro;
        int expand=1;
        CircularProgressIndicator intensity,time;
        CardView root_view;
        MyViewHolder(View view) {
            super(view);

            name = view.findViewById(R.id.name);name_details = view.findViewById(R.id.name_details);
            name.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2_bold.otf"));
            name_details.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2.ttf"));

            city = view.findViewById(R.id.city);city_details = view.findViewById(R.id.city_details);
            city.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2.ttf"));
            city_details.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2.ttf"));

            date = view.findViewById(R.id.date);date_details = view.findViewById(R.id.date_details);
            date.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2.ttf"));
            date_details.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2.ttf"));

            intensity_tag = view.findViewById(R.id.intensity_tag);
            intensity_tag.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2.ttf"));
            time_tag = view.findViewById(R.id.time_tag);
            time_tag.setTypeface(Typeface.createFromAsset(home.getAssets(), "fonts/exo2.ttf"));

            preview = view.findViewById(R.id.thumbnail);
            navtrigger = view.findViewById(R.id.navtrigger);
            locate = view.findViewById(R.id.locate);
            navigate = view.findViewById(R.id.navigate);
            cardItem = view.findViewById(R.id.cardItem);
            glidepro = view.findViewById(R.id.glidepro);
            root_view = view.findViewById(R.id.root_view);
            cardthumb = view.findViewById(R.id.cardthumb);
            intensity = view.findViewById(R.id.intensity);
            time = view.findViewById(R.id.time);
        }
    }
    CrackAdapter(HomeActivity home, List<Cracks> cracks) {
        this.cracks = cracks;
        this.home = home;
        this.interpolator = new DecelerateInterpolator();
        this.speed = 250;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_crack, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Cracks item = cracks.get(position);
        if(position<=1) current = holder;
        holder.name.setText(item.getName());holder.name_details.setText(item.getName());
        holder.city.setText(item.getCity());holder.city_details.setText(item.getCity());
        holder.date.setText(item.getDate());holder.date_details.setText(item.getDate());

        try {
            int date1=Integer.parseInt((item.getDate()).split("/")[1])+15;
            int date2=Integer.parseInt(new SimpleDateFormat("dd",Locale.US).format(new Date()));
            holder.time.setProgress(((date1-date2)),15);
        }
        catch (Exception e) {
            Toast.makeText(home, "Error", Toast.LENGTH_SHORT).show();
        }

        Glide.with(home).load(item.getPreview())
                .apply(new RequestOptions()
                        .centerCrop()
                        .override(Target.SIZE_ORIGINAL))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.glidepro.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.preview);
        holder.locate.setOnClickListener(view -> startMap(holder,Uri.parse("geo:"+item.getLatitude()+","+item.getLongitude()+"?q="+item.getLatitude()+","+item.getLongitude() )));
        holder.navigate.setOnClickListener(view -> startMap(holder,Uri.parse("google.navigation:q="+item.getLatitude()+","+item.getLongitude())));

        holder.cardItem.setOnClickListener(view -> {
            if(current!=null && holder!=current) current.navtrigger.performClick();
            if(holder.expand==1 && expandMain==0){
                holder.expand=-1;
                expandMain=1;
                current=holder;

//                if(position==0) home.home.scrollToPosition(0);
//                else home.home.scrollToPosition(3);

                cardHeight=holder.root_view.getHeight();
                imgHeight=holder.cardthumb.getWidth();

                holder.name.setVisibility(View.INVISIBLE);
                holder.city.setVisibility(View.INVISIBLE);
                holder.date.setVisibility(View.INVISIBLE);

                scaleY(holder.root_view,holder.root_view.getWidth(),speed,interpolator);
                scaleY(holder.cardthumb,holder.root_view.getWidth(),speed,interpolator);
                scaleX(holder.cardthumb,holder.root_view.getWidth(),speed,interpolator);

                AlphaAnimation anims = new AlphaAnimation(0,1);anims.setDuration(speed);anims.setInterpolator(interpolator);
                holder.navtrigger.setVisibility(View.VISIBLE);holder.navtrigger.requestFocus();
                anims.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        holder.intensity.setProgress(Double.parseDouble(item.getIntensity()),100);holder.expand=0;expandMain=0;
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });holder.navtrigger.startAnimation(anims);
            }
        });
        holder.navtrigger.setOnClickListener(view -> {
            if(holder.expand==0){
                holder.expand=-1;
                holder.name.setVisibility(View.VISIBLE);
                holder.city.setVisibility(View.VISIBLE);
                holder.date.setVisibility(View.VISIBLE);

                scaleY(holder.root_view,cardHeight,speed,interpolator);
                scaleY(holder.cardthumb,imgHeight,speed,interpolator);
                scaleX(holder.cardthumb,imgHeight,speed,interpolator);

                AlphaAnimation anims = new AlphaAnimation(1,0);anims.setDuration(speed);anims.setInterpolator(interpolator);
                anims.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) { holder.navtrigger.setVisibility(View.GONE);holder.expand=1;}
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });holder.navtrigger.startAnimation(anims);
            }
        });
    }
    private void startMap(@NonNull final MyViewHolder holder, Uri address){
        holder.navtrigger.performClick();
        new Handler().postDelayed(() -> {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, address);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(home.getPackageManager()) != null) {
                home.startActivity(mapIntent);
                home.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
            else{
                home.showSheet("Google Maps Required", "In order to use navigation feature, Google maps should to be installed on this device.","DOWNLOAD",200);
            }
        },speed);
    }
    @Override
    public int getItemCount() {
        if(cracks==null){return 0;}
        return cracks.size();
    }
    private void scaleY(final View view, int y, int t, Interpolator interpolator)
    {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredHeight(),y);anim.setInterpolator(interpolator);
        anim.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
            view.setLayoutParams(layoutParams);view.invalidate();
        });
        anim.setDuration(t);anim.start();
    }
    private void scaleX(final View view, int x, int t, Interpolator interpolator)
    {
        ValueAnimator anim = ValueAnimator.ofInt(view.getMeasuredWidth(),x);anim.setInterpolator(interpolator);
        anim.addUpdateListener(valueAnimator -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = (Integer) valueAnimator.getAnimatedValue();
            view.setLayoutParams(layoutParams);
        });
        anim.setDuration(t);anim.start();
    }
}