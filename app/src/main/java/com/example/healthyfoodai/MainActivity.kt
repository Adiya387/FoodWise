package com.example.healthyfoodai

import HomeFragment
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.healthyfoodai.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        toolbar = binding.toolbar
        toolbarTitle = binding.toolbarTitle

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    tvUserName.text = document.getString("name") ?: "Пользователь"
                }
                .addOnFailureListener {
                    tvUserName.text = "Ошибка загрузки"
                }
        }

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> loadFragment(HomeFragment(), "Главная")
                R.id.menu_recipes -> loadFragment(RecipeCategoriesFragment(), "Категории рецептов")
                R.id.menu_gpt -> loadFragment(FoodWiseGPTFragment(), "FoodWise GPT-S")
                R.id.menu_profile -> {
                    startActivity(Intent(this, Profile2::class.java))
                    true
                }
                else -> false
            }
        }


        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                    true
                }
                else -> false
            }
        }

        // 👇 Первый запуск — загрузить рецепты
        val sharedPref = getSharedPreferences("first_run", MODE_PRIVATE)
        val firstLaunch = sharedPref.getBoolean("need_upload_recipes", true)
        if (firstLaunch) {
            Toast.makeText(this, "⏳ Загружаем рецепты в Firestore...", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RecipesUploaderActivity::class.java))
            sharedPref.edit().putBoolean("need_upload_recipes", false).apply()
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "Главная")
        }
    }

    fun loadFragment(fragment: Fragment, title: String): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        toolbarTitle.text = title
        return true
    }

    fun setToolbarTitle(title: String) {
        toolbarTitle.text = title
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
