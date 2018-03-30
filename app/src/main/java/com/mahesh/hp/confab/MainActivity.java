package com.mahesh.hp.confab;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserReference;
    private FirebaseUser mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        if (mCurrentUser != null) {
            String online_user_id = mAuth.getCurrentUser().getUid();

            mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);

        }

        //Firebase Initialization




        // Android Initialization
        mToolbar =findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Confab");
        mViewPager = findViewById(R.id.main_tabPager);
        mTabLayout= findViewById(R.id.main_tabs);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        //Android Initialization


    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
            startActivity(startIntent);
            finish();
        }
        else{
            mUserReference.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser mCurrent  = mAuth.getCurrentUser();
        if(mCurrent !=null) {

            mUserReference.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



    if(item.getItemId() == R.id.main_logout_btn){
        logout();
       }
       if(item.getItemId() == R.id.main_settings_btn){
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
       }
       if(item.getItemId() == R.id.main_users_btn){
           Intent allUsersIntent = new Intent(MainActivity.this, AllUsers.class);
           startActivity(allUsersIntent);
       }




        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }
}
