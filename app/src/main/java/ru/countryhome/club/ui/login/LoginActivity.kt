package ru.countryhome.club.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*
import ru.countryhome.club.R
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.StrictMode
import androidx.core.widget.addTextChangedListener
import java.net.URL
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_detail.*
import ru.countryhome.club.MainActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.UnknownHostException


class LoginActivity : AppCompatActivity() {
    lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        pref = getSharedPreferences("account", MODE_PRIVATE)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

        btn_forgot_pass.setOnTouchListener { _, _ ->
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://countryhome.club/auth/index.php?forgot_password=yes")
            )
            startActivity(browserIntent)
            true
        }
        btn_reg.setOnTouchListener { _, _ ->
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://countryhome.club/auth/index.php?register=yes")
            )
            startActivity(browserIntent)
            true
        }

        text_login_edit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.contains("@")) {
                    text_login_layout.isErrorEnabled = true
                    btn_login.isEnabled = false
                    text_login_layout.error = "Недопустимый символ @. Для входа используйте логин, а не адрес электронной почты."
                } else {
                    text_login_layout.isErrorEnabled = false
                    btn_login.isEnabled = true
                    btn_login.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                }
            }
        })


        btn_login.setOnClickListener {
            try {
                val email: String = text_login_edit.text.toString()
                val pass: String = text_pass_edit.text.toString()
                val url = URL("https://countryhome.club/calc/apply/request/auth.php")
                var reqParam = URLEncoder.encode("how_auth", "UTF-8") + "=" + URLEncoder.encode("original", "UTF-8")
                reqParam += "&" + URLEncoder.encode("login", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
                reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8")
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
                    editor.putBoolean("isLogin", true)
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
                    println(arr)
                    editor.putStringSet("xml", arr)
                    editor.apply()
                    val intent = Intent(this, MainActivity::class.java)
                    ContextCompat.startActivity(this, intent, null)
                    finish()
                } else {
                    text_login_layout.isErrorEnabled = true
                    text_pass_layout.isErrorEnabled = true
                    text_login_layout.error = "Неправильный логин или пароль"
                    text_pass_layout.error = "Неправильный логин или пароль"
                    text_pass_edit.addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            text_pass_layout.isErrorEnabled = false
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                        }
                    })
                    text_login_edit.addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            text_login_layout.isErrorEnabled = false
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {
                        }
                    })
                }
            } catch (e: UnknownHostException) {
                val contextView = findViewById<View>(R.id.layout_login)
                Snackbar.make(contextView, "Проверьте подключение к интернету", Snackbar.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            } catch (e: Exception) {
                val contextView = findViewById<View>(R.id.layout_login)
                Snackbar.make(contextView, "Ошибка. Попробуйте позже", Snackbar.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }
    }
}
