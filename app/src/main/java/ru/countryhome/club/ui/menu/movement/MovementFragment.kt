package ru.countryhome.club.ui.menu.movement

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.movement_fragment.view.*

import ru.countryhome.club.R
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.gson.GsonBuilder
import java.net.URL
import android.os.StrictMode
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.os.AsyncTask
import android.view.*
import kotlinx.android.synthetic.main.movement_fragment.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_transactions.view.*
import ru.countryhome.club.MainActivity
import ru.countryhome.club.ui.detail.DetailActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.UnknownHostException


class MovementFragment : Fragment() {
    private lateinit var viewModel: MovementViewModel
    var movesJson: List<MovesJson>? = null
    lateinit var pref: SharedPreferences
    lateinit var prefR: SharedPreferences
    lateinit var v: View


    @SuppressLint("SimpleDateFormat", "CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v =  inflater.inflate(R.layout.movement_fragment, container, false)
        pref = activity!!.getSharedPreferences("account", AppCompatActivity.MODE_PRIVATE)
        prefR = activity!!.getSharedPreferences("data", AppCompatActivity.MODE_PRIVATE)

        v.refresh_layout.setOnRefreshListener{
            doAsync {try {
                var xml = ""
                if (pref.getStringSet("xml", null)?.size!! > 1) {
                    pref.getStringSet("xml", null)?.forEach { item ->
                        xml = "$xml$item~"
                    }
                } else {
                    pref.getStringSet("xml", null)?.forEach { item ->
                        xml += item
                    }
                }
                val line = URL("https://countryhome.club/calc/apply/request/get_list.php?xml=$xml").readText()
                yourTask(v, line, ArrayList(), ArrayList())
            } catch (ex: UnknownHostException) {
                doAssync(v, true).execute()
                ex.printStackTrace()
            } catch (e: Exception) {
                doAssync(v, false).execute()
                e.printStackTrace()
            }
            }.execute()
        }
        v.refresh_layout.setColorSchemeResources(R.color.color_update_first, R.color.color_update_second, R.color.color_update_third, R.color.color_update_fourth)

        doAsync {try {
            var xml = ""
            if (pref.getStringSet("xml", null)?.size!! > 1) {
                pref.getStringSet("xml", null)?.forEach { item ->
                    xml = "$xml$item~"
                }
            } else {
                pref.getStringSet("xml", null)?.forEach { item ->
                    xml += item
                }
            }
            println(pref.getStringSet("xml", null)!! +"lol")
            val line = URL("https://countryhome.club/calc/apply/request/get_list.php?xml=$xml").readText()
            println(line + "lol")
            yourTask(v, line, ArrayList(), ArrayList())
        } catch (ex: UnknownHostException) {
            doAssync(v, true).execute()
            ex.printStackTrace()
        } catch (e: Exception) {
            doAssync(v, false).execute()
            e.printStackTrace()
        }
        }.execute()
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        return v
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MovementViewModel::class.java)
    }

    override fun onResume() {
        if (prefR.getBoolean("isUpdate", false)) {
            prefR.edit().putBoolean("isUpdate", false).apply()
            activity!!.recreate()
        }
        super.onResume()
    }

    @SuppressLint("SimpleDateFormat")
    private fun yourTask(v: View, line: String, transactionsToday: List<Transactions>, transactionsEarly: List<Transactions>) {
        val gson = GsonBuilder().create()
        movesJson = gson.fromJson(line,Array<MovesJson>::class.java).toList()
        var transT = transactionsToday
        var transE = transactionsEarly
        movesJson!!.forEach { item ->
            var dates: List<String>?
            dates = if (item.moment == "") {
                item.created.split(" ")
            } else {
                item.moment.split(" ")
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val date = dates[0].split("-")
            val time = dates[1].split(":")
            val datetime = date[2]+"."+date[1]+"."+date[0]+" "+time[0]+":"+time[1]
            var cost: String = try {
                item.sum.dropLast(2) + "," + item.sum.substring(item.sum.length - 2)
            } catch (e: Exception) {
                item.sum + "," + "00"
            }
            val postCost = cost.split(",")
            if (postCost[0].length in 4..6) {
                cost = postCost[0].dropLast(3) + " " + postCost[0].substring(postCost[0].dropLast(3).length, cost.length - 3) + "," + postCost[1]
            } else if (postCost[0].length in 7..9) {
                cost = postCost[0].dropLast(6) + " " + postCost[0].dropLast(3) + " " + postCost[0].substring(postCost[0].dropLast(3).length, cost.length - 3) + "," + postCost[1]
            }
            if (item.applicable == "true") {
                if (sdf.format(Date()) == dates[0]) {
                    transT = transT + Transactions(R.drawable.ic_check_green_24dp, "№"+item.name, datetime, item.sourceStore, item.targetStore, "$cost \u20BD")
                } else {
                    transE = transE + Transactions(R.drawable.ic_check_green_24dp, "№"+item.name, datetime, item.sourceStore, item.targetStore, "$cost \u20BD")
                }
            } else {
                if (sdf.format(Date()) == dates[0]) {
                    transT = transT + Transactions(R.drawable.ic_close_red_24dp, "№"+item.name, datetime, item.sourceStore, item.targetStore, "$cost \u20BD")
                } else {
                    transE = transE + Transactions(R.drawable.ic_close_red_24dp, "№"+item.name, datetime, item.sourceStore, item.targetStore, "$cost \u20BD")
                }
            }
        }
        doAsyncs (movesJson, transT, transE, v).execute()
    }

    class doAsync(val handler: () -> Unit) : AsyncTask<Void, View, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

    class doAsyncs(val movesJson: List<MovesJson>?, private val transactionsToday: List<Transactions>, private val transactionsEarly: List<Transactions>, val v: View) : AsyncTask<Void, View, Void>(), TransactionsAdapter.ItemClickListener {
        override fun doInBackground(vararg params: Void?): Void? {
            return null
        }

        override fun onItemClick(view: View, position: Int) {
            val intent = Intent(view.context, DetailActivity::class.java)
            movesJson?.forEach { item ->
                if (item.name == view.text_trans_id.text.drop(1)) {
                    intent.putExtra("id", item.id)
                    intent.putExtra("owner", item.owner)
                    intent.putExtra("moment", item.moment)
                    intent.putExtra("name", item.name)
                    intent.putExtra("sum", item.sum)
                    intent.putExtra("applicable", item.applicable)
                    intent.putExtra("created", item.created)
                    intent.putExtra("sourceStore", item.sourceStore)
                    intent.putExtra("targetStore", item.targetStore)
                    intent.putExtra("nameApply", item.nameApply)
                    intent.putExtra("dateApply", item.dateApply)
                }
            }
            ContextCompat.startActivity(view.context, intent, null)
        }

        override fun onPostExecute(result: Void?) {
            v.recycler_today.layoutManager = LinearLayoutManager(v.context)
            v.recycler_today.isNestedScrollingEnabled = false
            val adapter = TransactionsAdapter(v.context, transactionsToday)
            adapter.setOnClickListener(this)
            v.recycler_today.adapter = adapter
            v.recycler_early.layoutManager = LinearLayoutManager(v.context)
            v.recycler_early.isNestedScrollingEnabled = false
            val adapter1 = TransactionsAdapter(v.context, transactionsEarly)
            adapter1.setOnClickListener (this)
            v.recycler_early.adapter = adapter1
            v.layout_load.visibility = View.GONE
            v.refresh_layout.visibility = View.VISIBLE
            v.refresh_layout.isRefreshing = false
            if (transactionsToday.isEmpty()) {
                v.text_today.visibility = View.GONE
                v.recycler_today.visibility = View.GONE
            }
            if (transactionsEarly.isEmpty()) {
                v.text_early.visibility = View.GONE
                v.recycler_early.visibility = View.GONE
            }
        }
    }

    class doAssync(private val v: View, private val isInternet: Boolean) : AsyncTask<Void, View, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            return null
        }

        override fun onPostExecute(result: Void?) {
            v.load_bar.visibility = View.GONE
            v.layout_error.visibility = View.VISIBLE
            if (isInternet) {
                v.layout_error.text_error.text = "Проверьте подключение к интернету"
            }
        }
    }

}
