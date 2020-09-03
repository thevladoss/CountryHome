package ru.countryhome.club.ui.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ru.countryhome.club.R
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Color
import android.os.AsyncTask
import android.view.MenuItem
import android.widget.ProgressBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_detail.*
import ru.countryhome.club.ui.menu.movement.PositionsJson
import java.net.URL
import android.widget.TextView
import java.net.HttpURLConnection
import android.util.*
import android.widget.Button
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.movement_fragment.view.*
import ru.countryhome.club.MainActivity
import ru.countryhome.club.ui.menu.movement.MovementFragment
import ru.countryhome.club.ui.menu.movement.MovesJson
import ru.countryhome.club.ui.menu.movement.Transactions
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DetailActivity : AppCompatActivity() {
    var goodsJson: List<PositionsJson>? = null
    lateinit var pref: SharedPreferences
    lateinit var prefR: SharedPreferences
    var movesJson: List<MovesJson>? = null

    @SuppressLint("PrivateResource", "SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val recyclerView = findViewById<View>(R.id.recycler_goods) as RecyclerView
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "№" + intent.getStringExtra("name")
        pref = getSharedPreferences("account", MODE_PRIVATE)
        prefR = getSharedPreferences("data", MODE_PRIVATE)

        refresh_detail.setOnRefreshListener{
            doAsync {
                try {
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
                    myTask(line)
                } catch (e: UnknownHostException) {
                    e.printStackTrace()
                    val contextView = findViewById<View>(R.id.layout_all_good)
                    Snackbar.make(contextView, "Проверьте подключение к интернету", Snackbar.LENGTH_LONG)
                        .show()
                }
                yourTask(recyclerView, ArrayList())
            }.execute()
        }
        refresh_detail.setColorSchemeResources(R.color.color_update_first, R.color.color_update_second, R.color.color_update_third, R.color.color_update_fourth)


        if (intent.getStringExtra("applicable") == "true") {
            img_val.setImageResource(R.drawable.ic_check_green_24dp)
            btn_create.text = "Проведено"
            btn_create.isEnabled = false
        } else {
            img_val.setImageResource(R.drawable.ic_close_red_24dp)
            btn_create.setBackgroundColor(resources.getColor(R.color.colorAccent))
        }

        text_number_transaction.text = "№" + intent.getStringExtra("name")
        var dates: List<String>? = if (intent.getStringExtra("moment") == "") {
            intent.getStringExtra("created").split(" ")
        } else {
            intent.getStringExtra("moment").split(" ")
        }
        var date = dates!![0].split("-")
        var time = dates!![1].split(":")
        var datetime = date[2]+"."+date[1]+"."+date[0]+" "+time[0]+":"+time[1]

        text_date_transaction.text = datetime
        text_main_from.text = intent.getStringExtra("sourceStore")
        text_main_to.text = intent.getStringExtra("targetStore")
        text_main_author.text = intent.getStringExtra("owner")
        var cost: String = try {
            intent.getStringExtra("sum").dropLast(2) + "," + intent.getStringExtra("sum").substring(intent.getStringExtra("sum").length - 2)
        } catch (e: Exception) {
            intent.getStringExtra("sum") + "," + "00"
        }
        val postCost = cost.split(",")
        if (postCost[0].length in 4..6) {
            cost = postCost[0].dropLast(3) + " " + postCost[0].substring(postCost[0].dropLast(3).length, cost.length - 3) + "," + postCost[1]
        } else if (postCost[0].length in 7..9) {
            cost = postCost[0].dropLast(6) + " " + postCost[0].dropLast(3) + " " + postCost[0].substring(postCost[0].dropLast(3).length, cost.length - 3) + "," + postCost[1]
        }
        text_main_cost.text = "$cost \u20BD"
        text_sum.text = "$cost \u20BD"

        text_name_apply.text = intent.getStringExtra("nameApply")

        if (intent.getStringExtra("dateApply") != "") {
            dates = intent.getStringExtra("dateApply").split(" ")
            date = dates!![0].split("-")
            time = dates!![1].split(":")

            text_date_apply.text = date[2]+"."+date[1]+"."+date[0]+" "+time[0]+":"+time[1]
        }


        doAsync {
            yourTask(recyclerView, ArrayList())
        }.execute()

        btn_create.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_MaterialComponents)
            builder.setMessage("Вы подтверждаете проводку перемещения?")
            builder.setPositiveButton("Да") { _: DialogInterface, _: Int ->
                try {
//                    println("https://countryhome.club/calc/apply/request/apply.php?move_id="+intent.getStringExtra("id")+"&login="+pref.getString("login", "")+"&password="+pref.getString("pass", ""))
                    val line = URL("https://countryhome.club/calc/apply/request/apply.php?move_id="+intent.getStringExtra("id")+"&login="+pref.getString("login", "")+"&password="+pref.getString("pass", "")).readText()
                    println(line)
                    val contextView = findViewById<View>(R.id.layout_all_good)
                    if (line == "ok") {
                        Snackbar.make(contextView, "Операция проведена успешно", Snackbar.LENGTH_LONG)
                            .show()
                        btn_create.text = "Проведено"
                        btn_create.setBackgroundColor(Color.WHITE)
                        btn_create.isEnabled = false
                        img_val.setImageResource(R.drawable.ic_check_green_24dp)
                        text_name_apply.text = pref.getString("name", "") + " " + pref.getString("surname", "")
                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm")
                        val currentDate = sdf.format(Date())
                        text_date_apply.text = currentDate
                        prefR.edit().putBoolean("isUpdate", true).apply()
                    } else if (line == "error") {
                        Snackbar.make(contextView, "Ошибка. Попробуйте позже", Snackbar.LENGTH_LONG)
                            .show()
                    }
                } catch (e: UnknownHostException) {
                    val contextView = findViewById<View>(R.id.layout_all_good)
                    Snackbar.make(contextView, "Проверьте подключение к интернету", Snackbar.LENGTH_LONG)
                        .show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            builder.setNegativeButton("Нет") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.cancel()
            }
            builder.show()

        }
    }

    private fun myTask(line: String) {
        val gson = GsonBuilder().create()
        movesJson = gson.fromJson(line,Array<MovesJson>::class.java).toList()
        movesJson!!.forEach { item ->
            if (intent.getStringExtra("id") == item.id) {
                doAssyncs(item, img_val, btn_create, resources, text_number_transaction, text_date_transaction, text_main_from, text_main_to, text_main_author, text_main_cost, text_sum).execute()
            }
        }
    }

    private fun yourTask(recyclerView: RecyclerView, goods: List<Goods>) {
        var goods = goods
        try {
            val line = URL("https://countryhome.club/calc/apply/request/get_positions.php?xml="+intent.getStringExtra("id")).readText()
            val gson = GsonBuilder().create()
            goodsJson = gson.fromJson(line,Array<PositionsJson>::class.java).toList()
            goodsJson!!.forEach { item ->
                val cost: String = try {
                    item.price.dropLast(2) + "," + item.price.substring(item.price.length - 2) + " \u20BD"
                } catch (e: Exception) {
                    item.price+",00 \u20BD"
                }
                var postCost: String = try {
                    (item.quantity.toFloat() * (item.price.dropLast(2) + "." + item.price.substring(item.price.length - 2)).toFloat()).toString()
                } catch (e: Exception) {
                    "0.00"
                }
                val ppstCost = postCost.split(".")
                if (ppstCost[1].length == 1) {
                    postCost += "0"
                }
                goods = goods + Goods(
                    item.name,
                    item.quantity + "×" + cost,
                    item.img,
                    postCost.replace(".", ",") + " \u20BD"
                )
            }
            doAsyncs(goods, this, recyclerView, progress_goods, text_goods, goodsJson!!.size, refresh_detail).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            doAsyncs(goods, this, recyclerView, progress_goods, text_goods, 0, refresh_detail).execute()
        }

    }

    class doAsync(val handler: () -> Unit) : AsyncTask<Void, View, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }

    class doAsyncs(val goods: List<Goods>, val context: Context, val recyclerView: RecyclerView, val progress_goods: ProgressBar, val text_goods: TextView, val size: Int, val refreshLayout: SwipeRefreshLayout) : AsyncTask<Void, View, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            return null
        }

        override fun onPostExecute(result: Void?) {
            text_goods.text = "ТОВАРЫ ("+ size +")"
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = GoodsAdapter(context, goods)
            progress_goods.visibility = View.GONE
            refreshLayout.isRefreshing = false
        }
    }

    class doAssyncs(val item: MovesJson,  val img_val: ImageView, val btn_create: Button, val resources: Resources, val text_number_transaction: TextView, val text_date_transaction: TextView, val text_main_from: TextView, val text_main_to: TextView, val text_main_author: TextView, val text_main_cost: TextView, val text_sum: TextView) : AsyncTask<Void, View, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            return null
        }

        override fun onPostExecute(result: Void?) {
            if (item.applicable == "true") {
                img_val.setImageResource(R.drawable.ic_check_green_24dp)
                btn_create.text = "Проведено"
                btn_create.setBackgroundColor(Color.WHITE)
                btn_create.isEnabled = false
            } else {
                img_val.setImageResource(R.drawable.ic_close_red_24dp)
                btn_create.isEnabled = true
                btn_create.text = "Провести"
                btn_create.setBackgroundColor(resources.getColor(R.color.colorAccent))
            }

            text_number_transaction.text = "№" + item.name
            var dates: List<String>?
            dates = item.moment.split(" ")
            val date = dates!![0].split("-")
            val time = dates!![1].split(":")
            val datetime = time[0]+":"+time[1]+" "+date[2]+"."+date[1]+"."+date[0]

            text_date_transaction.text = datetime
            text_main_from.text = item.sourceStore
            text_main_to.text = item.targetStore
            text_main_author.text = item.owner
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
            text_main_cost.text = "$cost \u20BD"
            text_sum.text = "$cost \u20BD"
        }
    }


    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                this.finish()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

}
