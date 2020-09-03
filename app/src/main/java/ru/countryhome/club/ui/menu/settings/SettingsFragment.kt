package ru.countryhome.club.ui.menu.settings

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.settings_fragment.view.*

import ru.countryhome.club.R
import ru.countryhome.club.ui.detail.DetailActivity
import ru.countryhome.club.ui.login.LoginActivity

class SettingsFragment : Fragment() {
    lateinit var pref: SharedPreferences

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View =  inflater.inflate(R.layout.settings_fragment, container, false)
        pref = activity!!.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        v.text_logout_login.text = pref.getString("login", "")
        v.layout_logout.setOnClickListener {
            val editor = pref.edit()
            editor.putBoolean("isLogin", false)
            editor.apply()
            val intent = Intent(v.context, LoginActivity::class.java)
            ContextCompat.startActivity(v.context, intent, null)
            activity!!.finish()
        }
        setHasOptionsMenu(true)
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.text_help_action).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}
