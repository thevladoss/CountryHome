package ru.countryhome.club

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ColorStateListInflaterCompat.inflate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import ru.countryhome.club.ui.detail.DetailActivity
import ru.countryhome.club.ui.login.LoginActivity
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var pref: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        pref = getSharedPreferences("account", MODE_PRIVATE)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        if (!pref.getBoolean("isLogin", false)) {
            val intent = Intent(this, LoginActivity::class.java)
            ContextCompat.startActivity(this, intent, null)
            finish()
        } else {
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            val navView: NavigationView = this.findViewById(R.id.nav_view)
            val navController = findNavController(R.id.nav_host_fragment)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_movement, R.id.nav_settings
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            try {
                val url = URL("https://countryhome.club/calc/apply/request/auth.php")
                var reqParam = URLEncoder.encode("how_auth", "UTF-8") + "=" + URLEncoder.encode("hash", "UTF-8")
                reqParam += "&" + URLEncoder.encode("login", "UTF-8") + "=" + URLEncoder.encode(pref.getString("login", ""), "UTF-8")
                reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pref.getString("pass", ""), "UTF-8")
                var line = ""
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"

                    val wr = OutputStreamWriter(outputStream)
                    wr.write(reqParam)
                    wr.flush()

                    BufferedReader(InputStreamReader(inputStream)).use {
                        line = it.readLine()
                        it.close()
                    }
                }

                if (line != "error") {
                    val split = line.split("~~~")
                    val editor = pref.edit()
                    editor.putString("login", split[0])
                    editor.putString("pass", split[1])
                    editor.putString("name", split[2])
                    editor.putString("surname", split[3])
                    editor.putString("position", split[4])
                    var i = 5
                    val arr = HashSet<String>()
                    repeat (split.size-5) {
                        arr.add(split[i])
                        i++
                    }
                    println(split[5])
                    println(arr)
                    editor.putStringSet("xml", arr)
                    editor.apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            navView.getHeaderView(0).text_full_name.text = pref.getString("name", "") + " " +  pref.getString("surname", "")
            navView.getHeaderView(0).text_position.text = pref.getString("position", "")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.text_help_action) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Помощь")
            builder.setMessage("Для того, чтобы принять товар на склад, зайдите в требуемое перемещение. Проверьте фактическое соответствие товаров занесенным, затем нажмите кнопку Провести.")
            builder.setNegativeButton("ЗАКРЫТЬ") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.cancel()
            }
            builder.show()
        }
        return super.onOptionsItemSelected(item)
    }
}
