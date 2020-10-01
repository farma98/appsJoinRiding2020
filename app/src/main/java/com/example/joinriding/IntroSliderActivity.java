package com.example.joinriding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.joinriding.fragments.HomeFragment;

public class IntroSliderActivity extends AppCompatActivity {

    private Button btnSkip, btnBeranda;

    private static final int MAX_STEP = 3;

    private String[] title_array = {
            "Mapping", "Posting",
            "Chatting"
    };
    private String[] description_array = {
            "Fitur Yang Membantu Pengguna Ketika Membutuhkan Bantuan Dengan Membagikan Lokasi",
            "Fitur Yang Membantu Membagikan Foto / Tulisan Terkait Sebuah Informasi",
            "Fitur Yang Membantu Untuk Berkomunikasi Melalui Chat Antar User"
    };
    private int[] about_images_array = {
            R.drawable.icon_add_post_white,
            R.drawable.icon_chat_user_white,
            R.drawable.icon_maps_grey
    };
    private int[] color_array = {
            R.color.colorWhite,
            R.color.colorWhite,
            R.color.colorWhite
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_slider);

        initComponent();
    }

    private void initComponent() {
        ViewPager viewPager = findViewById(R.id.view_pager);
        btnBeranda = findViewById(R.id.btn_got_it);

        bottomProgressDots(0);

        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnBeranda.setVisibility(View.GONE);
        btnBeranda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroSliderActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        btnSkip = findViewById(R.id.btn_skip);
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntroSliderActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });
    }

    private void bottomProgressDots(int index) {
        LinearLayout dotsLayout = findViewById(R.id.layoutDots);
        ImageView[] dots = new ImageView[MAX_STEP];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            int width_height = 15;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
            params.setMargins(10, 10, 10, 10);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.dot_round_slider);
            dots[i].setColorFilter(getResources().getColor(R.color.colorBlack), PorterDuff.Mode.SRC_IN);
            dotsLayout.addView(dots[i]);
        }

        dots[index].setImageResource(R.drawable.dot_round_slider);
        dots[index].setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            bottomProgressDots(position);
            if (position == title_array.length - 1) {
                btnBeranda.setVisibility(View.VISIBLE);
            } else {
                btnBeranda.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {

        MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.item_intro_slider, container, false);
            ((TextView) view.findViewById(R.id.title)).setText(title_array[position]);
            ((TextView) view.findViewById(R.id.description)).setText(description_array[position]);
            ((ImageView) view.findViewById(R.id.image)).setImageResource(about_images_array[position]);
            view.findViewById(R.id.lyt_parent).setBackgroundColor(getResources().getColor(color_array[position]));
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return title_array.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
